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
            switch event {
            case is AppEvent.PostSent:
                return feedbackPrefs.speakPostSent ? "Post sent" : nil
            case is AppEvent.ReplySent:
                return feedbackPrefs.speakPostSent ? "Reply sent" : nil
            case is AppEvent.BoostSent:
                return feedbackPrefs.speakPostSent ? "Boost sent" : nil
            case is AppEvent.PostFailed:
                return feedbackPrefs.speakError ? "Error" : nil
            case is AppEvent.TabLoaded:
                return feedbackPrefs.speakTabLoaded ? "Posts loaded" : nil
            case is AppEvent.NotificationReceived:
                return feedbackPrefs.speakNotification ? "New notification" : nil
            case is AppEvent.Favourited, is AppEvent.Unfavourited, is AppEvent.Bookmarked, is AppEvent.Followed, is AppEvent.Unfollowed, is AppEvent.Deleted:
                return feedbackPrefs.speakPostSent ? "\(event.key)" : nil
            case is AppEvent.Error:
                return feedbackPrefs.speakError ? "Error occurred" : nil
            case is AppEvent.NewPostReceived:
                return nil
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
