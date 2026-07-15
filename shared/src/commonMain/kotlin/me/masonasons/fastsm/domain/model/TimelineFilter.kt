package me.masonasons.fastsm.domain.model

data class TimelineFilter(
    val query: String = "",
    val hideBoosts: Boolean = false,
    val hideReplies: Boolean = false,
    val hideOwn: Boolean = false,
    val mediaOnly: Boolean = false,
) {
    val isActive: Boolean get() =
        query.isNotBlank() || hideBoosts || hideReplies || hideOwn || mediaOnly

    fun matches(status: UniversalStatus, myUserId: String?): Boolean {
        if (hideBoosts && status.reblog != null) return false
        val display = status.reblog ?: status
        if (hideReplies && display.inReplyToId != null) return false
        if (hideOwn && myUserId != null && display.account.id == myUserId) return false
        if (mediaOnly && display.mediaAttachments.isEmpty()) return false
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            val haystack = buildString {
                append(display.text.lowercase()).append(' ')
                append(display.account.displayName.lowercase()).append(' ')
                append(display.account.acct.lowercase()).append(' ')
                display.spoilerText?.lowercase()?.let { append(it) }
            }
            if (!haystack.contains(q)) return false
        }
        return true
    }

    companion object {
        val NONE = TimelineFilter()
    }
}