import SwiftUI
import Shared

@main
struct FastSMApp: App {
    @StateObject private var appState = AppState()
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(appState)
                .onOpenURL { url in
                    appState.handleOAuthCallback(url: url)
                }
        }
    }
}

class AppState: ObservableObject {
    let container: AppContainer
    let speechService: SpeechService
    let hapticsService: HapticsService
    let soundService: SoundService
    
    @Published var isLoggedIn = false
    @Published var currentAccount: Account?
    
    init() {
        let tokenStore = IosSecureTokenStore()
        let appPrefs = InMemoryAppPrefs()
        let feedbackPrefs = InMemoryFeedbackPrefs()
        
        self.speechService = SpeechService(feedbackPrefs: feedbackPrefs)
        self.hapticsService = HapticsService(feedbackPrefs: feedbackPrefs)
        self.soundService = SoundService(feedbackPrefs: feedbackPrefs)
        
        self.container = AppContainer(
            httpClient: createDefaultHttpClient(),
            tokenStore: tokenStore,
            appPrefs: appPrefs,
            feedbackPrefs: feedbackPrefs,
            accountStorage: InMemoryAccountStorage(),
            timelineStorage: InMemoryTimelineStorage(),
            timelinePositionStorage: InMemoryTimelinePositionStorage(),
            speechEngine: speechService,
            soundEngine: soundService,
            hapticsEngine: hapticsService
        )
        
        Task { @MainActor in
            if let account = await container.accountRepository.getActiveAccount() {
                self.currentAccount = account
                self.isLoggedIn = true
            }
        }
    }
    
    func handleOAuthCallback(url: URL) {
        guard let code = MastodonOAuth.extractCode(url: url.absoluteString) else { return }
        NotificationCenter.default.post(
            name: .oauthCallback,
            object: nil,
            userInfo: ["code": code]
        )
    }
}

extension Notification.Name {
    static let oauthCallback = Notification.Name("oauthCallback")
}
