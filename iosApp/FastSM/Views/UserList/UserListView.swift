import SwiftUI
import shared

struct UserListView: View {
    let userId: String
    let kind: String
    @EnvironmentObject var appState: AppState
    @State private var users: [UniversalUser] = []
    @State private var nextCursor: String? = nil
    
    var body: some View {
        List(users, id: \.id) { user in
            NavigationLink(destination: ProfileView(userId: user.id)) {
                HStack {
                    AsyncImage(url: URL(string: user.avatar ?? "")) { image in
                        image.resizable().clipShape(Circle())
                    } placeholder: {
                        Circle().fill(.secondary.opacity(0.3))
                    }
                    .frame(width: 40, height: 40)
                    
                    VStack(alignment: .leading) {
                        Text(user.displayName).font(.subheadline.bold())
                        Text("@\(user.acct)").font(.caption).foregroundStyle(.secondary)
                    }
                }
            }
        }
        .listStyle(.plain)
        .navigationTitle(kind == "followers" ? "Followers" : "Following")
        .task {
            await loadUsers()
        }
    }
    
    private func loadUsers() async {
        guard let account = appState.currentAccount else { return }
        let platform = appState.container.forAccount(account: account)
        do {
            let page: UserListPage
            if kind == "followers" {
                page = try await platform.getFollowers(userId: userId, limit: 40, cursor: nextCursor)
            } else {
                page = try await platform.getFollowing(userId: userId, limit: 40, cursor: nextCursor)
            }
            await MainActor.run {
                users.append(contentsOf: page.users)
                nextCursor = page.nextCursor_
            }
        } catch {
            print("Failed to load users: \(error)")
        }
    }
}

extension UserListPage {
    var nextCursor_: String? { nextCursor }
}
