import Foundation
import AVFoundation
import shared

class SpeechService: NSObject, SpeechEngine, ObservableObject {
    private let synthesizer = AVSpeechSynthesizer()
    private let feedbackPrefs: FeedbackPrefsStore
    
    init(feedbackPrefs: FeedbackPrefsStore) {
        self.feedbackPrefs = feedbackPrefs
        super.init()
        synthesizer.delegate = self
    }
    
    func handle(event: AppEvent) {
        guard feedbackPrefs.speechEnabled else { return }
        
        let text: String? = {
            switch event.key {
            case "send_post":
                return feedbackPrefs.speakPostSent ? "Post sent" : nil
            case "send_reply":
                return feedbackPrefs.speakPostSent ? "Reply sent" : nil
            case "send_repost":
                return feedbackPrefs.speakPostSent ? "Boost sent" : nil
            case "error":
                return feedbackPrefs.speakError ? "Error" : nil
            case "ready":
                return feedbackPrefs.speakTabLoaded ? "Posts loaded" : nil
            case "notification":
                return feedbackPrefs.speakNotification ? "New notification" : nil
            case "like", "unlike", "follow", "unfollow", "delete":
                return feedbackPrefs.speakPostSent ? "\(event.key)" : nil
            default:
                return nil
            }
        }()
        
        guard let text = text, !text.isEmpty else { return }
        
        let utterance = AVSpeechUtterance(string: text)
        utterance.rate = AVSpeechUtteranceDefaultSpeechRate
        utterance.pitchMultiplier = 1.0
        utterance.volume = 1.0
        synthesizer.speak(utterance)
    }
}

extension SpeechService: AVSpeechSynthesizerDelegate {
    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {}
}
