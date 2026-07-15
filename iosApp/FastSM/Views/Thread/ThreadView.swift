import SwiftUI
import shared

struct ThreadView: View {
    let statusId: String
    @EnvironmentObject var appState: AppState
    @StateObject private var viewModel = ThreadViewModel()
    
    var body: some View {
        List {
            ForEach(viewModel.ancestors, id: \.id) { status in
                StatusItemView(status: status)
                    .listRowSeparator(.hidden)
            }
            
            if let main = viewModel.mainStatus {
                StatusItemView(status: main)
                    .listRowSeparator(.hidden)
                    .listRowBackground(Color.blue.opacity(0.05))
            }
            
            ForEach(viewModel.descendants, id: \.id) { status in
                StatusItemView(status: status)
                    .listRowSeparator(.hidden)
            }
        }
        .listStyle(.plain)
        .navigationTitle("Thread")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            if let account = appState.currentAccount {
                await viewModel.load(statusId: statusId, account: account, container: appState.container)
            }
        }
    }
}
