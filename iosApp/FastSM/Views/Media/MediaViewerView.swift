import SwiftUI
import AVKit

struct MediaViewerView: View {
    let url: String
    let type: String
    
    var body: some View {
        Group {
            if type == "video" || type == "gifv" {
                if let videoUrl = URL(string: url) {
                    VideoPlayer(player: AVPlayer(url: videoUrl))
                }
            } else if type == "audio" {
                if let audioUrl = URL(string: url) {
                    AudioPlayerView(url: audioUrl)
                }
            } else {
                AsyncImage(url: URL(string: url)) { image in
                    image.resizable().aspectRatio(contentMode: .fit)
                } placeholder: {
                    ProgressView()
                }
            }
        }
        .navigationTitle("Media")
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct AudioPlayerView: View {
    let url: URL
    @State private var player: AVPlayer?
    
    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "waveform")
                .font(.system(size: 64))
                .foregroundStyle(.blue)
            
            Button(action: {
                if let player = player {
                    if player.timeControlStatus == .playing {
                        player.pause()
                    } else {
                        player.play()
                    }
                }
            }) {
                Image(systemName: "play.circle.fill")
                    .font(.system(size: 64))
            }
        }
        .onAppear { player = AVPlayer(url: url) }
        .onDisappear { player?.pause() }
    }
}
