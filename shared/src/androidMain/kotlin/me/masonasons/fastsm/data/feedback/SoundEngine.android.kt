package me.masonasons.fastsm.data.feedback

import android.content.Context
import android.media.MediaPlayer
import me.masonasons.fastsm.domain.model.AppEvent

class AndroidSoundEngine(
    private val context: Context,
    private val prefs: FeedbackPrefsStore,
) : SoundEngine {
    override fun handle(event: AppEvent, specId: String?) {
        if (!prefs.soundEnabled) return
        if (specId != null && specId in prefs.mutedSpecs) return
        val soundFile = event.key
        try {
            val afd = context.assets.openFd("sounds/default/$soundFile.ogg")
            val player = MediaPlayer()
            player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            player.setVolume(prefs.soundVolume, prefs.soundVolume)
            player.setOnCompletionListener { it.release() }
            player.prepare()
            player.start()
        } catch (_: Exception) {}
    }

    override fun availablePacks(): List<String> = listOf("default")
}
