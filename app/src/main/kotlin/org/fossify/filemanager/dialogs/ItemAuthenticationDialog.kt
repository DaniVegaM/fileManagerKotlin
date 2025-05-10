package org.fossify.filemanager.dialogs

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.extensions.getAlertDialogBuilder
import org.fossify.commons.extensions.setupDialogStuff
import org.fossify.commons.extensions.showErrorToast
import org.fossify.commons.extensions.toast
import org.fossify.commons.extensions.value
import org.fossify.commons.helpers.PROTECTION_FINGERPRINT
import org.fossify.commons.helpers.PROTECTION_PIN
import org.fossify.filemanager.R
import org.fossify.filemanager.databinding.DialogPinAuthenticationBinding
import org.fossify.filemanager.extensions.config

class ItemAuthenticationDialog(
    private val activity: BaseSimpleActivity,
    private val callback: (success: Boolean) -> Unit
) {
    private val binding = DialogPinAuthenticationBinding.inflate(activity.layoutInflater)
    private var dialog: AlertDialog? = null

    init {
        val protectionType = activity.config.protectionType
        
        if (protectionType == PROTECTION_FINGERPRINT) {
            // Si es huella digital, mostramos directamente el diálogo biométrico
            if (isDeviceSecure()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    authenticateWithBiometrics()
                } else {
                    // No hay soporte biométrico en esta versión, fallback a PIN si existe
                    if (activity.config.protectionPin.isNotEmpty()) {
                        showPinDialog()
                    } else {
                        activity.toast(R.string.no_fingerprints_registered)
                        callback(false)
                    }
                }
            } else {
                activity.toast(R.string.no_fingerprints_registered)
                callback(false)
            }
        } else {
            // Si es PIN, mostramos el diálogo de PIN
            showPinDialog()
        }
    }

    private fun showPinDialog() {
        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel) { _, _ -> callback(false) }
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.access_item) { alertDialog ->
                    dialog = alertDialog
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        validatePin()
                    }
                }
            }
    }

    private fun validatePin() {
        val enteredPin = binding.pinAuthenticationInput.value
        val storedPin = activity.config.protectionPin

        if (enteredPin == storedPin) {
            callback(true)
            dialog?.dismiss()
        } else {
            activity.toast(R.string.wrong_pin)
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
