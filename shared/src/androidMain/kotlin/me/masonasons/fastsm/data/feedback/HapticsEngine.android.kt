package me.masonasons.fastsm.data.feedback

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import me.masonasons.fastsm.domain.model.AppEvent

class AndroidHapticsEngine(
    private val context: Context,
    private val prefs: FeedbackPrefsStore,
) : HapticsEngine {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun handle(event: AppEvent) {
        if (!prefs.hapticsEnabled) return
        val shouldVibrate = when (event) {
            is AppEvent.PostSent, is AppEvent.ReplySent -> prefs.hapticsPostSent
            is AppEvent.BoostSent -> prefs.hapticsPostSent
            is AppEvent.NewPostReceived -> prefs.hapticsNewPost
            is AppEvent.NotificationReceived -> prefs.hapticsNotification
            is AppEvent.PostFailed, is AppEvent.Error -> prefs.hapticsError
            else -> false
        }
        if (!shouldVibrate) return
        vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
