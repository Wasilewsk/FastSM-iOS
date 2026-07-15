package me.masonasons.fastsm.domain.model

sealed interface AppEvent {
    val key: String

    data object PostSent : AppEvent { override val key = "send_post" }
    data object ReplySent : AppEvent { override val key = "send_reply" }
    data object BoostSent : AppEvent { override val key = "send_repost" }
    data class PostFailed(val message: String) : AppEvent { override val key = "error" }
    data class NewPostReceived(val specId: String, val soundKey: String) : AppEvent {
        override val key: String get() = soundKey
    }
    data class NotificationReceived(val text: String) : AppEvent { override val key = "notification" }
    data class TabLoaded(val specId: String, val count: Int) : AppEvent { override val key = "ready" }
    data object Favourited : AppEvent { override val key = "like" }
    data object Unfavourited : AppEvent { override val key = "unlike" }
    data object Bookmarked : AppEvent { override val key = "like" }
    data object Followed : AppEvent { override val key = "follow" }
    data object Unfollowed : AppEvent { override val key = "unfollow" }
    data object Deleted : AppEvent { override val key = "delete" }
    data class Error(val message: String) : AppEvent { override val key = "error" }
}