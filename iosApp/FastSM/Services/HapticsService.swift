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
            switch event.key {
            case "send_post", "send_reply", "send_repost":
                return feedbackPrefs.hapticsPostSent
            case "ready":
                return feedbackPrefs.hapticsNewPost
            case "notification":
                return feedbackPrefs.hapticsNotification
            case "error":
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
