import Foundation
import shared

@MainActor
class ThreadViewModel: ObservableObject {
    @Published var ancestors: [UniversalStatus] = []
    @Published var mainStatus: UniversalStatus?
    @Published var descendants: [UniversalStatus] = []
    
    func load(statusId: String, account: Account, container: AppContainer) async {
        do {
            let platform = container.forAccount(account: account)
            let status = try await platform.getStatus(statusId: statusId)
            let context = try await platform.getStatusContext(statusId: statusId)
            
            await MainActor.run {
                ancestors = context.ancestors
                mainStatus = status
                descendants = context.descendants
            }
        } catch {
            print("Failed to load thread: \(error)")
        }
    }
}
