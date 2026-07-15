import Foundation
import Shared

@MainActor
class ComposeViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var maxChars: Int = 500
    
    func loadMaxChars(account: Account, container: AppContainer) async {
        do {
            let platform = container.forAccount(account: account)
            let max = try await platform.getMaxPostChars()
            await MainActor.run { maxChars = max }
        } catch {
            print("Failed to load max chars: \(error)")
        }
    }
}
