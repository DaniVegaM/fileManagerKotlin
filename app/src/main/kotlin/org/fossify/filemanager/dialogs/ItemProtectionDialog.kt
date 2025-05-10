package org.fossify.filemanager.dialogs

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.dialogs.ConfirmationDialog
import org.fossify.commons.extensions.getAlertDialogBuilder
import org.fossify.commons.extensions.setupDialogStuff
import org.fossify.commons.extensions.showErrorToast
import org.fossify.commons.extensions.toast
import org.fossify.commons.extensions.value
import org.fossify.commons.helpers.PROTECTION_FINGERPRINT
import org.fossify.commons.helpers.PROTECTION_PIN
import org.fossify.filemanager.R
import org.fossify.filemanager.databinding.DialogItemProtectionBinding
import org.fossify.filemanager.extensions.config

class ItemProtectionDialog(private val activity: BaseSimpleActivity, private val callback: (success: Boolean) -> Unit) {
    private val binding = DialogItemProtectionBinding.inflate(activity.layoutInflater)
    private var dialog: AlertDialog? = null

    init {
        binding.apply {
            // Configurar los RadioButtons
            when (activity.config.protectionType) {
                PROTECTION_FINGERPRINT -> itemProtectionRadioFingerprint.isChecked = true
                PROTECTION_PIN -> itemProtectionRadioPin.isChecked = true
            }

            // Mostrar/Ocultar la sección de PIN según selección
            itemProtectionRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                itemProtectionPinLayout.visibility = 
                    if (checkedId == R.id.item_protection_radio_pin) View.VISIBLE else View.GONE
            }

            // Inicialmente oculto si no está seleccionado PIN
            itemProtectionPinLayout.visibility = 
                if (itemProtectionRadioPin.isChecked) View.VISIBLE else View.GONE
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.item_protection_setup) { alertDialog ->
                    dialog = alertDialog
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        processPassword(alertDialog)
                    }
                }
            }
    }

    private fun processPassword(alertDialog: AlertDialog) {
        val protectionType = if (binding.itemProtectionRadioFingerprint.isChecked) {
            PROTECTION_FINGERPRINT
        } else {
            PROTECTION_PIN
        }

        activity.config.protectionType = protectionType

        if (protectionType == PROTECTION_FINGERPRINT) {
            if (isDeviceSecure()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    authenticateWithBiometrics()
                } else {
                    callback(true)
                    alertDialog.dismiss()
                }
            } else {
                activity.toast(R.string.no_fingerprints_registered)
            }
        } else {
            val pin = binding.itemProtectionPin.value
            val confirmPin = binding.itemProtectionConfirmPin.value

//            if (pin.length != 4 || confirmPin.length != 4) {
//                activity.toast(R.string.pin_must_be_4_digits)
//                return
//            }

            if (pin != confirmPin) {
                activity.toast(R.string.pin_does_not_match)
                return
            }

            activity.config.protectionPin = pin
            callback(true)
            alertDialog.dismiss()
        }
    }

    private fun isDeviceSecure(): Boolean {
        val keyguardManager = activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguardManager.isDeviceSecure
        } else {
            keyguardManager.isKeyguardSecure
        }
    }    @RequiresApi(Build.VERSION_CODES.P)
    private fun authenticateWithBiometrics() {
        val executor: Executor = ContextCompat.getMainExecutor(activity)
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.fingerprint_authentication))
            .setDescription(activity.getString(R.string.authenticate_to_access))
            .setNegativeButtonText(activity.getString(R.string.cancel))
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    callback(true)
                    dialog?.dismiss()
                    val confirmationTextId = R.string.fingerprint_setup_successfully
                    ConfirmationDialog(activity, "", confirmationTextId, R.string.ok, 0) { }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    activity.showErrorToast(errString.toString())
                    callback(false)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    activity.toast(R.string.authentication_failed)
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }
}
