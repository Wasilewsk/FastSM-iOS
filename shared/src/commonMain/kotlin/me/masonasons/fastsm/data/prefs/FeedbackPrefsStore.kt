package me.masonasons.fastsm.data.prefs

interface FeedbackPrefsStore {
    val speechEnabled: Boolean
    fun setSpeechEnabled(v: Boolean)
    val speakTabLoaded: Boolean
    fun setSpeakTabLoaded(v: Boolean)
    val speakNotification: Boolean
    fun setSpeakNotification(v: Boolean)
    val speakPostSent: Boolean
    fun setSpeakPostSent(v: Boolean)
    val speakError: Boolean
    fun setSpeakError(v: Boolean)
    val soundEnabled: Boolean
    fun setSoundEnabled(v: Boolean)
    val soundVolume: Float
    fun setSoundVolume(v: Float)
    val mutedSpecs: Set<String>
    fun toggleMuted(specId: String)
    val hapticsEnabled: Boolean
    fun setHapticsEnabled(v: Boolean)
    val hapticsPostSent: Boolean
    fun setHapticsPostSent(v: Boolean)
    val hapticsNewPost: Boolean
    fun setHapticsNewPost(v: Boolean)
    val hapticsNotification: Boolean
    fun setHapticsNotification(v: Boolean)
    val hapticsError: Boolean
    fun setHapticsError(v: Boolean)
    val accountSoundpacks: Map<Long, String>
    fun setAccountSoundpack(accountId: Long, pack: String)
    fun soundpackFor(accountId: Long?): String
}
