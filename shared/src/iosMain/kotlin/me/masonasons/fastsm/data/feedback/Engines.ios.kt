package me.masonasons.fastsm.data.feedback

import me.masonasons.fastsm.domain.model.AppEvent

class IosSpeechEngine : SpeechEngine {
    override fun handle(event: AppEvent) {
        // Handled by native iOS SpeechService via Swift wrapper
    }
}

class IosSoundEngine : SoundEngine {
    override fun handle(event: AppEvent, specId: String?) {
        // Handled by native iOS SoundService via Swift wrapper
    }
    override fun availablePacks(): List<String> = listOf("default")
}

class IosHapticsEngine : HapticsEngine {
    override fun handle(event: AppEvent) {
        // Handled by native iOS HapticsService via Swift wrapper
    }
}
