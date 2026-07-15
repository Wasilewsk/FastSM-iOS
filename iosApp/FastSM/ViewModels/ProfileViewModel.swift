import Foundation
import Shared

@MainActor
class ProfileViewModel: ObservableObject {
    @Published var user: UniversalUser?
    @Published var statuses: [UniversalStatus] = []
    
    func load(userId: String, account: Account, container: AppContainer) async {
        do {
            let platform = container.forAccount(account: account)
            let user = try await platform.getUser(userId: userId)
            let statuses = try await platform.getUserStatuses(userId: userId, limit: 40, maxId: nil, excludeReplies: false)
            
            await MainActor.run {
                self.user = user
                self.statuses = statuses
            }
        } catch {
            print("Failed to load profile: \(error)")
        }
    }
}
