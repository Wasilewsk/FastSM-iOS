import SwiftUI
import shared

struct SearchView: View {
    @EnvironmentObject var appState: AppState
    @State private var query = ""
    @StateObject private var viewModel = SearchViewModel()
    
    var body: some View {
        VStack {
            HStack {
                Image(systemName: "magnifyingglass")
                TextField("Search", text: $query)
                    .onSubmit { Task { await search() } }
            }
            .padding(10)
            .background(.quaternary)
            .cornerRadius(10)
            .padding(.horizontal)
            
            List {
                ForEach(viewModel.users, id: \.id) { user in
                    NavigationLink(destination: ProfileView(userId: user.id)) {
                        HStack {
                            AsyncImage(url: URL(string: user.avatar ?? "")) { image in
                                image.resizable().clipShape(Circle())
                            } placeholder: {
                                Circle().fill(.secondary.opacity(0.3))
                            }
                            .frame(width: 36, height: 36)
                            
                            VStack(alignment: .leading) {
                                Text(user.display_name).font(.subheadline.bold())
                                Text("@\(user.acct)").font(.caption).foregroundStyle(.secondary)
                            }
                        }
                    }
                }
                
                ForEach(viewModel.posts, id: \.id) { status in
                    StatusItemView(status: status)
                        .listRowSeparator(.hidden)
                }
            }
            .listStyle(.plain)
        }
        .navigationTitle("Search")
    }
    
    private func search() {
        Task {
            if let account = appState.currentAccount {
                await viewModel.search(query: query, account: account, container: appState.container)
            }
        }
    }
}
