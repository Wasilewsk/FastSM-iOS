package me.masonasons.fastsm.domain.model

sealed class TimelineSpec {
    abstract val id: String
    abstract val label: String
    abstract val closable: Boolean
    open val newPostSoundKey: String get() = "home"

    data object Home : TimelineSpec() {
        override val id = "home"
        override val label = "Home"
        override val closable = false
    }

    data object Notifications : TimelineSpec() {
        override val id = "notifications"
        override val label = "Notifications"
        override val closable = false
        override val newPostSoundKey = "notification"
    }

    data object LocalPublic : TimelineSpec() {
        override val id = "local"
        override val label = "Local"
        override val closable = true
    }

    data object FederatedPublic : TimelineSpec() {
        override val id = "federated"
        override val label = "Federated"
        override val closable = true
    }

    data object Bookmarks : TimelineSpec() {
        override val id = "bookmarks"
        override val label = "Bookmarks"
        override val closable = true
    }

    data object Favourites : TimelineSpec() {
        override val id = "favourites"
        override val label = "Favourites"
        override val closable = true
        override val newPostSoundKey = "likes"
    }

    data class UserList(val listId: String, val title: String) : TimelineSpec() {
        override val id: String get() = "list:$listId"
        override val label: String get() = title
        override val closable = true
        override val newPostSoundKey = "list"
    }

    data class RemoteInstance(val instance: String, val localOnly: Boolean) : TimelineSpec() {
        override val id: String get() = "remote-instance:$instance:${if (localOnly) "local" else "fed"}"
        override val label: String get() =
            "${hostOf(instance)} ${if (localOnly) "local" else "federated"}"
        override val closable = true
    }

    data class RemoteUser(val instance: String, val acct: String) : TimelineSpec() {
        override val id: String get() = "remote-user:$instance:$acct"
        override val label: String get() = "@${acct.removePrefix("@")}"
        override val closable = true
        override val newPostSoundKey = "user"
    }

    data class UserPosts(val userId: String, val displayName: String) : TimelineSpec() {
        override val id: String get() = "user-posts:$userId"
        override val label: String get() = displayName
        override val closable = true
        override val newPostSoundKey = "user"
    }

    data class Hashtag(val tag: String) : TimelineSpec() {
        override val id: String get() = "hashtag:${tag.lowercase()}"
        override val label: String get() = "#$tag"
        override val closable = true
        override val newPostSoundKey = "search"
    }
}

private fun hostOf(instance: String): String =
    instance.removePrefix("https://").removePrefix("http://").removeSuffix("/")