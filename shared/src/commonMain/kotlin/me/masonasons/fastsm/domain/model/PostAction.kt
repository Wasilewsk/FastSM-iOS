package me.masonasons.fastsm.domain.model

enum class PostAction(val key: String, val label: String) {
    REPLY("reply", "Reply"),
    QUOTE("quote", "Quote"),
    FAVOURITE("favourite", "Favourite / unfavourite"),
    BOOST("boost", "Boost / unboost"),
    BOOKMARK("bookmark", "Bookmark / remove bookmark"),
    EDIT("edit", "Edit (own posts)"),
    DELETE("delete", "Delete (own posts)"),
    VIEW_PROFILE("view_profile", "View profile"),
    VIEW_MEDIA("view_media", "View media"),
    OPEN_LINK("open_link", "Open link");

    companion object {
        val ALL: Set<PostAction> = entries.toSet()
        fun fromKey(key: String): PostAction? = entries.firstOrNull { it.key == key }
    }
}