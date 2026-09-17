package org.catholiccompanion.app.audio

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Tactile feedback explicitly controlled by the Rosary's in-app Haptics switch. */
class RosaryHaptics(context: Context) {
    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Throwable) {
        null
    }

    fun playBead() {
        val effect = runCatching {
            VibrationEffect.createOneShot(BEAD_DURATION_MILLIS, VibrationEffect.DEFAULT_AMPLITUDE)
        }.getOrNull()
        vibrate(effect)
    }

    fun playCompletion() {
        val effect = runCatching {
            VibrationEffect.createWaveform(longArrayOf(0L, 45L, 60L, 65L), -1)
        }.getOrNull()
        vibrate(effect)
    }

    private fun vibrate(effect: VibrationEffect?) {
        if (effect == null) return
        val deviceVibrator = vibrator ?: return
        if (runCatching { !deviceVibrator.hasVibrator() }.getOrDefault(defaultValue = true)) return

        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                deviceVibrator.vibrate(
                    effect,
                    VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH),
                )
            } else {
                deviceVibrator.vibrate(effect)
            }
        }
    }

    private companion object {
        const val BEAD_DURATION_MILLIS = 38L
    }
}
