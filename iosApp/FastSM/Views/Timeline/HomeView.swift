import SwiftUI
import shared

struct HomeView: View {
    @EnvironmentObject var appState: AppState
    @StateObject private var viewModel = HomeViewModel()
    
    var body: some View {
        List {
            if viewModel.isLoading && viewModel.statuses.isEmpty {
                ProgressView("Loading timeline...")
                    .frame(maxWidth: .infinity)
                    .listRowSeparator(.hidden)
            }
            
            ForEach(viewModel.statuses, id: \.id) { status in
                StatusItemView(status: status)
                    .listRowSeparator(.hidden)
                    .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                    .onTapGesture {
                        viewModel.selectedStatus = status
                    }
                    .task {
                        if status.id == viewModel.statuses.last?.id {
                            await viewModel.loadMore()
                        }
                    }
            }
        }
        .listStyle(.plain)
        .refreshable {
            await viewModel.refresh()
        }
        .navigationTitle("Home")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button(action: { Task { await viewModel.refresh() } }) {
                    Image(systemName: "arrow.clockwise")
                }
            }
        }
        .sheet(item: $viewModel.selectedStatus) { status in
            NavigationStack {
                ThreadView(statusId: status.id)
            }
        }
        .task {
            if let account = appState.currentAccount {
                await viewModel.load(account: account, container: appState.container)
            }
        }
    }
}

extension UniversalStatus: Identifiable {}
