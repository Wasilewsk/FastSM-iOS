import UIKit
import shared

class HapticsService: HapticsEngine, ObservableObject {
    private let feedbackPrefs: FeedbackPrefsStore
    
    init(feedbackPrefs: FeedbackPrefsStore) {
        self.feedbackPrefs = feedbackPrefs
    }
    
    func handle(event: AppEvent) {
        guard feedbackPrefs.hapticsEnabled else { return }
        
        let shouldVibrate: Bool = {
            switch event {
            case is PostSent, is ReplySent, is BoostSent:
                return feedbackPrefs.hapticsPostSent
            case is NewPostReceived:
                return feedbackPrefs.hapticsNewPost
            case is NotificationReceived:
                return feedbackPrefs.hapticsNotification
            case is PostFailed, is Error:
                return feedbackPrefs.hapticsError
            default:
                return false
            }
        }()
        
        guard shouldVibrate else { return }
        
        let generator = UIImpactFeedbackGenerator(style: .medium)
        generator.impactOccurred()
    }
}
