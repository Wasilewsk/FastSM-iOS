package me.masonasons.fastsm.data.feedback

import android.content.Context
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityEvent
import me.masonasons.fastsm.domain.model.AppEvent

class AndroidSpeechEngine(
    private val context: Context,
    private val prefs: FeedbackPrefsStore,
) : SpeechEngine {
    override fun handle(event: AppEvent) {
        if (!prefs.speechEnabled) return
        val text = when (event) {
            is AppEvent.PostSent -> if (prefs.speakPostSent) "Post sent" else return
            is AppEvent.ReplySent -> if (prefs.speakPostSent) "Reply sent" else return
            is AppEvent.BoostSent -> if (prefs.speakPostSent) "Boost sent" else return
            is AppEvent.PostFailed -> if (prefs.speakError) "Error: ${event.message}" else return
            is AppEvent.TabLoaded -> if (prefs.speakTabLoaded) "${event.count} posts loaded" else return
            is AppEvent.NotificationReceived -> if (prefs.speakNotification) event.text else return
            is AppEvent.Favourited -> if (prefs.speakPostSent) "Favourited" else return
            is AppEvent.Unfavourited -> if (prefs.speakPostSent) "Unfavourited" else return
            is AppEvent.Bookmarked -> if (prefs.speakPostSent) "Bookmarked" else return
            is AppEvent.Followed -> if (prefs.speakPostSent) "Followed" else return
            is AppEvent.Unfollowed -> if (prefs.speakPostSent) "Unfollowed" else return
            is AppEvent.Deleted -> if (prefs.speakPostSent) "Deleted" else return
            is AppEvent.Error -> if (prefs.speakError) "Error: ${event.message}" else return
            is AppEvent.NewPostReceived -> return
        }
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return
        val e = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT).apply {
            this.text.add(text)
        }
        am.sendAccessibilityEvent(e)
    }
}
