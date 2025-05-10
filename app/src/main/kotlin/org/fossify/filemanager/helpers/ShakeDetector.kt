package org.fossify.filemanager.helpers

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector : SensorEventListener {
    private var mListener: OnShakeListener? = null
    private var mShakeTimestamp: Long = 0
    private var mShakeCount = 0

    fun setOnShakeListener(listener: OnShakeListener) {
        mListener = listener
    }

    interface OnShakeListener {
        fun onShake()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // No necesitamos implementar esto
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (mListener != null) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH

            // gForce será cercano a 1 cuando no hay movimiento
            val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                val now = System.currentTimeMillis()
                // Ignorar eventos de agitado demasiado cercanos
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    return
                }

                // Reiniciar el contador si ha pasado demasiado tiempo
                if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                    mShakeCount = 0
                }

                mShakeTimestamp = now
                mShakeCount++

                if (mShakeCount >= REQUIRED_SHAKES) {
                    mListener?.onShake()
                    mShakeCount = 0
                }
            }
        }
    }

    companion object {
        private const val SHAKE_THRESHOLD_GRAVITY = 2.7f
        private const val SHAKE_SLOP_TIME_MS = 500
        private const val SHAKE_COUNT_RESET_TIME_MS = 3000
        private const val REQUIRED_SHAKES = 3
    }
}
