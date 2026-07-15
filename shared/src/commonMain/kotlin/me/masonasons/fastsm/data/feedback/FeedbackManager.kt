package me.masonasons.fastsm.data.feedback

import me.masonasons.fastsm.data.prefs.FeedbackPrefsStore
import me.masonasons.fastsm.domain.model.AppEvent

interface SpeechEngine {
    fun handle(event: AppEvent)
}

interface SoundEngine {
    fun handle(event: AppEvent, specId: String?)
    fun availablePacks(): List<String>
}

interface HapticsEngine {
    fun handle(event: AppEvent)
}

class FeedbackManager(
    val prefs: FeedbackPrefsStore,
    private val speech: SpeechEngine,
    private val sound: SoundEngine,
    private val haptics: HapticsEngine,
) {
    fun emit(event: AppEvent, specId: String? = null) {
        speech.handle(event)
        sound.handle(event, specId)
        haptics.handle(event)
    }

    fun toggleMuted(specId: String) = prefs.toggleMuted(specId)
    fun isMuted(specId: String): Boolean = specId in prefs.mutedSpecs
    fun availablePacks(): List<String> = sound.availablePacks()
}
