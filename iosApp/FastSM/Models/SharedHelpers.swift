import Foundation
import Shared

enum SharedHelpers {
    static func formatRelative(instant: Shared.Instant) -> String {
        let now = Clock.System.now()
        let seconds = (now - instant).inWholeSeconds
        switch seconds {
        case ..<0: return "just now"
        case 0..<60: return "just now"
        case 60..<3600: return "\(seconds / 60) minutes ago"
        case 3600..<86400: return "\(seconds / 3600) hours ago"
        case 86400..<604800: return "\(seconds / 86400) days ago"
        default: return "\(seconds / 604800) weeks ago"
        }
    }
}
