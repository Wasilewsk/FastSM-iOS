import SwiftUI
import shared

struct SettingsView: View {
    @EnvironmentObject var appState: AppState
    
    var body: some View {
        List {
            Section("Account") {
                if let account = appState.currentAccount {
                    HStack {
                        AsyncImage(url: URL(string: account.avatar ?? "")) { image in
                            image.resizable().clipShape(Circle())
                        } placeholder: {
                            Circle().fill(.secondary.opacity(0.3))
                        }
                        .frame(width: 40, height: 40)
                        
                        VStack(alignment: .leading) {
                            Text(account.displayName).font(.subheadline.bold())
                            Text("@\(account.acct)").font(.caption).foregroundStyle(.secondary)
                        }
                    }
                }
                
                Button("Sign Out", role: .destructive) {
                    Task {
                        if let account = appState.currentAccount {
                            try? await appState.container.accountRepository.delete(id: account.id)
                            await MainActor.run {
                                appState.currentAccount = nil
                                appState.isLoggedIn = false
                            }
                        }
                    }
                }
            }
            
            Section("Feedback") {
                NavigationLink("Speech & Sound") {
                    FeedbackSettingsView()
                }
                NavigationLink("Haptics") {
                    HapticsSettingsView()
                }
            }
            
            Section("About") {
                HStack {
                    Text("Version")
                    Spacer()
                    Text("0.2.4").foregroundStyle(.secondary)
                }
            }
        }
        .navigationTitle("Settings")
    }
}

struct FeedbackSettingsView: View {
    var body: some View {
        List {
            Section("Speech") {
                Toggle("Enable Speech", isOn: .constant(true))
                Toggle("Speak Tab Loaded", isOn: .constant(true))
                Toggle("Speak Notifications", isOn: .constant(true))
                Toggle("Speak Post Sent", isOn: .constant(false))
            }
            Section("Sound") {
                Toggle("Enable Sound", isOn: .constant(true))
            }
        }
        .navigationTitle("Speech & Sound")
    }
}

struct HapticsSettingsView: View {
    var body: some View {
        List {
            Toggle("Enable Haptics", isOn: .constant(true))
            Toggle("Haptic on Post Sent", isOn: .constant(true))
            Toggle("Haptic on New Post", isOn: .constant(false))
            Toggle("Haptic on Notification", isOn: .constant(true))
        }
        .navigationTitle("Haptics")
    }
}
