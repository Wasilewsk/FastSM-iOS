import Foundation
import Shared

@MainActor
class SearchViewModel: ObservableObject {
    @Published var users: [UniversalUser] = []
    @Published var posts: [UniversalStatus] = []
    @Published var hashtags: [String] = []
    
    func search(query: String, account: Account, container: AppContainer) async {
        guard !query.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        
        do {
            let platform = container.forAccount(account: account)
            let results = try await platform.search(query: query)
            
            await MainActor.run {
                users = results.users
                posts = results.posts
                hashtags = results.hashtags
            }
        } catch {
            print("Failed to search: \(error)")
        }
    }
}
