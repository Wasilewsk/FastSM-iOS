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
            case .PostSent:
                return feedbackPrefs.speakPostSent ? "Post sent" : nil
            case .ReplySent:
                return feedbackPrefs.speakPostSent ? "Reply sent" : nil
            case .BoostSent:
                return feedbackPrefs.speakPostSent ? "Boost sent" : nil
            case is AppEvent.PostFailed:
                return feedbackPrefs.speakError ? "Error" : nil
            case is AppEvent.TabLoaded:
                return feedbackPrefs.speakTabLoaded ? "Posts loaded" : nil
            case is AppEvent.NotificationReceived:
                return feedbackPrefs.speakNotification ? "New notification" : nil
            case .Favourited, .Unfavourited, .Bookmarked, .Followed, .Unfollowed, .Deleted:
                return feedbackPrefs.speakPostSent ? "\(event.key)" : nil
            case is AppEvent.Error:
                return feedbackPrefs.speakError ? "Error occurred" : nil
            case is AppEvent.NewPostReceived:
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
