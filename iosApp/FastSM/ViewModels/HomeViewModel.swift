import Foundation
import Shared

@MainActor
class HomeViewModel: ObservableObject {
    @Published var statuses: [UniversalStatus] = []
    @Published var isLoading = false
    @Published var selectedStatus: UniversalStatus?
    
    private var account: Account?
    private var container: AppContainer?
    private var maxId: String? = nil
    
    func load(account: Account, container: AppContainer) async {
        self.account = account
        self.container = container
        await refresh()
    }
    
    func refresh() async {
        guard let account = account, let container = container else { return }
        isLoading = true
        maxId = nil
        
        do {
            let platform = container.forAccount(account: account)
            let results = try await platform.getHomeTimeline(limit: 40, maxId: nil)
            await MainActor.run {
                statuses = results
                isLoading = false
                if !results.isEmpty {
                    maxId = results.last?.id
                }
            }
        } catch {
            print("Failed to load timeline: \(error)")
            await MainActor.run { isLoading = false }
        }
    }
    
    func loadMore() async {
        guard let account = account, let container = container, let maxId = maxId, !isLoading else { return }
        isLoading = true
        
        do {
            let platform = container.forAccount(account: account)
            let results = try await platform.getHomeTimeline(limit: 40, maxId: maxId)
            await MainActor.run {
                statuses.append(contentsOf: results)
                isLoading = false
                if !results.isEmpty {
                    self.maxId = results.last?.id
                }
            }
        } catch {
            print("Failed to load more: \(error)")
            await MainActor.run { isLoading = false }
        }
    }
}
