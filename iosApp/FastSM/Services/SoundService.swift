import AVFoundation
import shared

class SoundService: SoundEngine, ObservableObject {
    private let feedbackPrefs: FeedbackPrefsStore
    
    init(feedbackPrefs: FeedbackPrefsStore) {
        self.feedbackPrefs = feedbackPrefs
    }
    
    func handle(event: AppEvent, specId: String?) {
        guard feedbackPrefs.soundEnabled else { return }
        if let specId = specId, feedbackPrefs.mutedSpecs.contains(specId) { return }
        
        let soundName = event.key
        guard let url = Bundle.main.url(forResource: soundName, withExtension: "ogg",
                                         subdirectory: "sounds/default") else { return }
        
        do {
            let player = try AVAudioPlayer(contentsOf: url)
            player.volume = feedbackPrefs.soundVolume
            player.play()
        } catch {
            print("Failed to play sound: \(error)")
        }
    }
    
    func availablePacks() -> [String] {
        return ["default"]
    }
}
