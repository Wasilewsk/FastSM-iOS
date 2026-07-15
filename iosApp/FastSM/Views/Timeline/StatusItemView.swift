import SwiftUI
import Shared

struct StatusItemView: View {
    let status: UniversalStatus
    @EnvironmentObject var appState: AppState
    
    private var displayStatus: UniversalStatus {
        status.reblog_ ?? status
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            if status.reblog_ != nil {
                HStack(spacing: 4) {
                    Image(systemName: "arrow.2.squarepath")
                        .font(.caption)
                    Text("\(status.account.display_name) boosted")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }
            
            HStack(alignment: .top, spacing: 10) {
                AsyncImage(url: URL(string: displayStatus.account.avatar_ ?? "")) { image in
                    image.resizable().clipShape(Circle())
                } placeholder: {
                    Circle().fill(.secondary.opacity(0.3))
                }
                .frame(width: 40, height: 40)
                
                VStack(alignment: .leading, spacing: 2) {
                    Text(displayStatus.account.display_name)
                        .font(.subheadline.bold())
                    Text("@\(displayStatus.account.acct)")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                
                Spacer()
                
                Text(displayStatus.created_at.formatted(.relative(presentation: .named)))
                    .font(.caption2)
                    .foregroundStyle(.secondary)
            }
            
            if let spoiler = displayStatus.spoiler_text_ {
                Text("⚠️ \(spoiler)")
                    .font(.subheadline.italic())
            }
            
            Text(displayStatus.text)
                .font(.subheadline)
                .fixedSize(horizontal: false, vertical: true)
            
            if !displayStatus.mediaAttachments.isEmpty {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(displayStatus.mediaAttachments, id: \.id) { media in
                            AsyncImage(url: URL(string: media.previewUrl_ ?? media.url)) { image in
                                image.resizable().aspectRatio(contentMode: .fill)
                            } placeholder: {
                                Rectangle().fill(.secondary.opacity(0.2))
                            }
                            .frame(width: 120, height: 90)
                            .cornerRadius(8)
                            .clipped()
                        }
                    }
                }
            }
            
            HStack(spacing: 20) {
                actionButton(icon: "bubble.right", count: displayStatus.repliesCount) {}
                actionButton(icon: "arrow.2.squarepath", count: displayStatus.boostsCount) {}
                actionButton(icon: "heart", count: displayStatus.favouritesCount,
                             isHighlighted: displayStatus.favourited) {}
                Button(action: {}) {
                    Image(systemName: "bookmark")
                        .foregroundStyle(displayStatus.bookmarked ? .blue : .secondary)
                }
            }
            .font(.caption)
            .foregroundStyle(.secondary)
        }
        .padding(.vertical, 8)
    }
    
    @ViewBuilder
    private func actionButton(icon: String, count: Int, isHighlighted: Bool = false, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: 4) {
                Image(systemName: icon)
                    .foregroundStyle(isHighlighted ? .blue : .secondary)
                if count > 0 {
                    Text("\(count)")
                }
            }
        }
    }
}

extension UniversalStatus {
    var reblog_: UniversalStatus? { reblog }
    var spoiler_text_: String? { spoilerText }
}
