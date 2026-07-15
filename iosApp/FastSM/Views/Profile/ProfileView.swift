import SwiftUI
import shared

struct ProfileView: View {
    let userId: String
    @EnvironmentObject var appState: AppState
    @StateObject private var viewModel = ProfileViewModel()
    
    var body: some View {
        List {
            if let user = viewModel.user {
                VStack(alignment: .leading, spacing: 12) {
                    HStack(alignment: .top, spacing: 12) {
                        AsyncImage(url: URL(string: user.avatar ?? "")) { image in
                            image.resizable().clipShape(Circle())
                        } placeholder: {
                            Circle().fill(.secondary.opacity(0.3))
                        }
                        .frame(width: 64, height: 64)
                        
                        VStack(alignment: .leading) {
                            Text(user.display_name).font(.title3.bold())
                            Text("@\(user.acct)").font(.subheadline).foregroundStyle(.secondary)
                        }
                    }
                    
                    HStack(spacing: 20) {
                        Label("\(user.statusesCount)", systemImage: "doc.text")
                        Label("\(user.followersCount)", systemImage: "person.2")
                        Label("\(user.followingCount)", systemImage: "person")
                    }
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    
                    if !user.note.isEmpty {
                        Text(user.note).font(.subheadline)
                    }
                }
                .listRowSeparator(.hidden)
            }
            
            ForEach(viewModel.statuses, id: \.id) { status in
                StatusItemView(status: status)
                    .listRowSeparator(.hidden)
            }
        }
        .listStyle(.plain)
        .navigationTitle(viewModel.user?.display_name ?? "Profile")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            if let account = appState.currentAccount {
                await viewModel.load(userId: userId, account: account, container: appState.container)
            }
        }
    }
}
