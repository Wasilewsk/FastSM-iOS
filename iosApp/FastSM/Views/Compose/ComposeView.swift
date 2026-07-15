import SwiftUI
import Shared

struct ComposeView: View {
    @EnvironmentObject var appState: AppState
    @State private var text: String = ""
    @State private var visibility: Visibility = .public_
    @State private var isLoading = false
    @State private var errorMessage: String?
    var inReplyToId: String? = nil
    
    var body: some View {
        VStack {
            HStack {
                if let account = appState.currentAccount {
                    AsyncImage(url: URL(string: account.avatar ?? "")) { image in
                        image.resizable().clipShape(Circle())
                    } placeholder: {
                        Circle().fill(.secondary.opacity(0.3))
                    }
                    .frame(width: 36, height: 36)
                }
                
                Picker("Visibility", selection: $visibility) {
                    Text("Public").tag(Visibility.public_)
                    Text("Unlisted").tag(Visibility.unlisted)
                    Text("Followers").tag(Visibility.private_)
                    Text("Direct").tag(Visibility.direct)
                }
                .pickerStyle(.menu)
            }
            .padding()
            
            TextEditor(text: $text)
                .frame(minHeight: 120)
                .padding()
            
            if let error = errorMessage {
                Text(error).foregroundStyle(.red).font(.caption)
            }
            
            HStack {
                Spacer()
                Text("\(text.count)")
                    .font(.caption)
                    .foregroundStyle(text.count > 500 ? .red : .secondary)
                
                Button(action: submit) {
                    if isLoading {
                        ProgressView()
                    } else {
                        Text(inReplyToId != nil ? "Reply" : "Post")
                            .bold()
                    }
                }
                .buttonStyle(.borderedProminent)
                .disabled(text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || isLoading)
            }
            .padding()
        }
        .navigationTitle(inReplyToId != nil ? "Reply" : "New Post")
        .navigationBarTitleDisplayMode(.inline)
    }
    
    private func submit() {
        guard let account = appState.currentAccount else { return }
        isLoading = true
        errorMessage = nil
        
        Task {
            do {
                let platform = appState.container.forAccount(account: account)
                let request = PostRequest(
                    text: text,
                    inReplyToId: inReplyToId,
                    visibility: visibility,
                    spoilerText: nil,
                    mediaIds: [],
                    poll: nil,
                    scheduledAt: nil,
                    quoteStatusId: nil,
                    quoteStatusCid: nil
                )
                _ = try await platform.post(request: request)
                await MainActor.run {
                    isLoading = false
                    text = ""
                }
            } catch {
                await MainActor.run {
                    isLoading = false
                    errorMessage = "Failed to post: \(error.localizedDescription)"
                }
            }
        }
    }
}

enum Visibility: Hashable {
    case public_, unlisted, private_, direct
}
