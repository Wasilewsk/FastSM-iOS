import SwiftUI
import shared

struct AddAccountView: View {
    @EnvironmentObject var appState: AppState
    @State private var instance: String = ""
    @State private var isLoading = false
    @State private var errorMessage: String?
    
    var body: some View {
        VStack(spacing: 24) {
            Spacer()
            
            Image(systemName: "at")
                .font(.system(size: 64))
                .foregroundStyle(.blue)
            
            Text("FastSM")
                .font(.largeTitle.bold())
            
            Text("Connect to Mastodon")
                .font(.title3)
                .foregroundStyle(.secondary)
            
            TextField("Instance (e.g. mastodon.social)", text: $instance)
                .textContentType(.URL)
                .autocapitalization(.none)
                .disableAutocorrection(true)
                .textFieldStyle(.roundedBorder)
                .padding(.horizontal, 32)
            
            if let error = errorMessage {
                Text(error)
                    .foregroundStyle(.red)
                    .font(.caption)
            }
            
            Button(action: connect) {
                if isLoading {
                    ProgressView()
                } else {
                    Text("Connect")
                        .frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .disabled(instance.trimmingCharacters(in: .whitespaces).isEmpty || isLoading)
            .padding(.horizontal, 32)
            
            Spacer()
        }
        .navigationTitle("")
        .navigationBarHidden(true)
        .onReceive(NotificationCenter.default.publisher(for: .oauthFailed)) { note in
            if let msg = note.userInfo?["error"] as? String {
                errorMessage = "Login failed: \(msg)"
            } else {
                errorMessage = "Login failed"
            }
        }
    }
    
    private func connect() {
        let normalized = MastodonOAuth.shared.normalizeInstance(raw: instance)
        guard !normalized.isEmpty else { return }
        
        isLoading = true
        errorMessage = nil
        
        Task {
            do {
                let api = MastodonApi(
                    httpClient: appState.container.httpClient,
                    instanceBase: normalized,
                    tokenProvider: { nil }
                )
                let app = try await api.registerApp(
                    clientName: MastodonOAuth.shared.CLIENT_NAME,
                    redirectUri: MastodonOAuth.shared.REDIRECT_URI,
                    scopes: MastodonOAuth.shared.SCOPES,
                    website: MastodonOAuth.shared.WEBSITE
                )
                let authUrl = MastodonOAuth.shared.buildAuthorizeUrl(
                    instanceBase: normalized,
                    clientId: app.client_id
                )
                
                await MainActor.run {
                    isLoading = false
                    if let url = URL(string: authUrl) {
                        UIApplication.shared.open(url)
                    }
                    
                    let helper = OAuthHelper(
                        appState: appState,
                        instance: normalized,
                        clientId: app.client_id,
                        clientSecret: app.client_secret
                    )
                    OAuthHelperStore.shared.current = helper
                }
            } catch {
                await MainActor.run {
                    isLoading = false
                    errorMessage = "Failed to connect: \(error.localizedDescription)"
                }
            }
        }
    }
}

class OAuthHelperStore {
    static let shared = OAuthHelperStore()
    var current: OAuthHelper?
}

class OAuthHelper {
    let appState: AppState
    let instance: String
    let clientId: String
    let clientSecret: String
    
    private var observer: NSObjectProtocol?
    
    init(appState: AppState, instance: String, clientId: String, clientSecret: String) {
        self.appState = appState
        self.instance = instance
        self.clientId = clientId
        self.clientSecret = clientSecret
        
        observer = NotificationCenter.default.addObserver(
            forName: .oauthCallback, object: nil, queue: .main
        ) { [weak self] notification in
            guard let self = self,
                  let code = notification.userInfo?["code"] as? String else { return }
            self.exchangeToken(code: code)
        }
    }
    
    deinit {
        if let observer = observer {
            NotificationCenter.default.removeObserver(observer)
        }
    }
    
    func exchangeToken(code: String) {
        Task {
            do {
                let api = MastodonApi(
                    httpClient: appState.container.httpClient,
                    instanceBase: instance,
                    tokenProvider: { nil }
                )
                let token = try await api.exchangeToken(
                    clientId: clientId,
                    clientSecret: clientSecret,
                    code: code,
                    redirectUri: MastodonOAuth.shared.REDIRECT_URI,
                    scopes: MastodonOAuth.shared.SCOPES
                )
                
                let authedApi = MastodonApi(
                    httpClient: appState.container.httpClient,
                    instanceBase: instance,
                    tokenProvider: { token.access_token }
                )
                let me = try await authedApi.verifyCredentials()
                
                let id = try await appState.container.accountRepository.saveMastodonAccount(
                    instance: instance,
                    userId: me.id,
                    acct: me.acct,
                    displayName: me.display_name,
                    avatar: me.avatar,
                    accessToken: token.access_token,
                    clientId: clientId,
                    clientSecret: clientSecret
                )
                
                let savedAccount = try await appState.container.accountRepository.getById(id: Int64(truncating: id))
                
                await MainActor.run {
                    OAuthHelperStore.shared.current = nil
                    appState.currentAccount = savedAccount
                    appState.isLoggedIn = true
                }
            } catch {
                print("OAuth exchange failed: \(error)")
                await MainActor.run {
                    NotificationCenter.default.post(
                        name: .oauthFailed, object: nil,
                        userInfo: ["error": error.localizedDescription]
                    )
                    OAuthHelperStore.shared.current = nil
                }
            }
        }
    }
}
