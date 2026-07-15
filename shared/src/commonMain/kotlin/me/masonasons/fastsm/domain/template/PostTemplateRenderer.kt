package me.masonasons.fastsm.domain.template

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.masonasons.fastsm.domain.model.NotificationType
import me.masonasons.fastsm.domain.model.UniversalMedia
import me.masonasons.fastsm.domain.model.UniversalNotification
import me.masonasons.fastsm.domain.model.UniversalStatus
import me.masonasons.fastsm.domain.model.UniversalUser

object PostTemplateRenderer {

    const val DEFAULT_POST = "\$account.display_name\$ (@\$account.acct\$): \$text\$ \$created_at\$"
    const val DEFAULT_BOOST = "\$account.display_name\$ boosted \$reblog.account.display_name\$: \$text\$ \$created_at\$"
    const val DEFAULT_NOTIFICATION = "\$account.display_name\$ (@\$account.acct\$) \$type\$: \$text\$ \$created_at\$"

    private val tokenRegex = Regex("""\$([a-zA-Z_][a-zA-Z_0-9.]*)\$""")
    private const val TEXT_SENTINEL = "@@FASTSM_TEXT@@"

    private val leadingMentions = Regex("^((?:@\\S+\\s+)+)")

    fun renderStatus(
        template: String,
        status: UniversalStatus,
        config: TemplateConfig = TemplateConfig(),
    ): String {
        val display = status.reblog ?: status
        val cookedText = cookStatusText(display, config, includeMedia = true)
        val deferred = template.replace("\$text\$", TEXT_SENTINEL)
        val expanded = tokenRegex.replace(deferred) { m ->
            resolveStatusToken(m.groupValues[1], status, display, config).orEmpty()
        }
        return expanded.replace(TEXT_SENTINEL, cookedText).trim()
    }

    fun renderNotification(
        template: String,
        notification: UniversalNotification,
        config: TemplateConfig = TemplateConfig(),
    ): String {
        val status = notification.status
        val cookedText = status?.let { cookStatusText(it, config, includeMedia = false) }.orEmpty()
        val deferred = template
            .replace("\$text\$", TEXT_SENTINEL)
            .replace("\$status.text\$", TEXT_SENTINEL)
        val expanded = tokenRegex.replace(deferred) { m ->
            resolveNotificationToken(m.groupValues[1], notification, config).orEmpty()
        }
        return expanded.replace(TEXT_SENTINEL, cookedText).trim()
    }

    private fun cookStatusText(
        status: UniversalStatus,
        config: TemplateConfig,
        includeMedia: Boolean,
    ): String {
        var text = status.text
        if (config.maxUsernamesDisplay > 0) {
            text = collapseLeadingMentions(text, config.maxUsernamesDisplay)
        }
        val cw = status.spoilerText?.takeIf { it.isNotBlank() }
        if (cw != null) {
            text = when (config.cwMode) {
                CwMode.HIDE -> "CW: $cw"
                CwMode.SHOW -> if (text.isNotBlank()) "CW: $cw. $text" else "CW: $cw"
                CwMode.IGNORE -> text
            }
        }
        if (includeMedia && config.includeMediaDescriptions) {
            status.mediaAttachments.forEach { m ->
                text += formatMediaSuffix(m)
            }
        }
        return text
    }

    private fun collapseLeadingMentions(text: String, threshold: Int): String {
        val match = leadingMentions.find(text) ?: return text
        val section = match.groupValues[1]
        val handles = section.trim().split(Regex("\\s+"))
        if (handles.size <= threshold) return text
        val rest = text.substring(section.length).trimStart()
        val remaining = handles.size - 1
        val head = "${handles[0]} and $remaining more"
        return if (rest.isEmpty()) head else "$head $rest"
    }

    private fun formatMediaSuffix(m: UniversalMedia): String {
        val label = mediaTypeLabel(m.type)
        val desc = m.description?.takeIf { it.isNotBlank() }
        return if (desc != null) " ($label) description: $desc" else " ($label) with no description"
    }

    private fun mediaTypeLabel(type: String): String = when (type) {
        "gifv" -> "GIFV"
        else -> type.uppercase()
    }

    private fun resolveStatusToken(
        path: String,
        root: UniversalStatus,
        display: UniversalStatus,
        config: TemplateConfig,
    ): String? {
        val parts = path.split(".")
        return when (parts[0]) {
            "account" -> resolveAccount(parts.drop(1), root.account, config)
            "reblog" -> root.reblog?.let {
                resolveBareStatus(parts.drop(1), it, config)
            }
            else -> resolveBareStatus(parts, display, config)
        }
    }

    private fun resolveBareStatus(
        path: List<String>,
        status: UniversalStatus,
        config: TemplateConfig,
    ): String? =
        when (path.firstOrNull()) {
            null, "" -> "${maybeDemojify(status.account.displayName, config)} (@${status.account.acct})"
            "text" -> status.text
            "spoiler_text" -> status.spoilerText
            "url" -> status.url
            "visibility" -> status.visibility?.label
            "created_at" -> formatRelative(status.createdAt)
            "favourites_count" -> status.favouritesCount.toString()
            "boosts_count" -> status.boostsCount.toString()
            "replies_count" -> status.repliesCount.toString()
            "account" -> resolveAccount(path.drop(1), status.account, config)
            else -> null
        }

    private fun resolveAccount(
        path: List<String>,
        user: UniversalUser,
        config: TemplateConfig,
    ): String? =
        when (path.firstOrNull()) {
            null, "" -> "${maybeDemojify(user.displayName, config)} (@${user.acct})"
            "display_name" -> maybeDemojify(user.displayName, config).ifBlank { user.acct }
            "acct" -> user.acct
            "username" -> user.username
            "url" -> user.url
            else -> null
        }

    private fun maybeDemojify(input: String, config: TemplateConfig): String =
        if (config.demojifyDisplayNames) Demojify.demojify(input) else input

    private fun resolveNotificationToken(
        path: String,
        notification: UniversalNotification,
        config: TemplateConfig,
    ): String? {
        val parts = path.split(".")
        return when (parts[0]) {
            "type" -> notificationTypeLabel(notification.type)
            "account" -> resolveAccount(parts.drop(1), notification.account, config)
            "status" -> notification.status?.let { resolveBareStatus(parts.drop(1), it, config) }
            "created_at" -> formatRelative(notification.createdAt)
            "text" -> notification.status?.text
            else -> null
        }
    }

    private fun notificationTypeLabel(type: NotificationType): String = when (type) {
        NotificationType.MENTION -> "mentioned you"
        NotificationType.REPLY -> "replied to your post"
        NotificationType.QUOTE -> "quoted your post"
        NotificationType.REBLOG -> "boosted your post"
        NotificationType.FAVOURITE -> "favourited your post"
        NotificationType.FOLLOW -> "followed you"
        NotificationType.FOLLOW_REQUEST -> "requested to follow you"
        NotificationType.POLL -> "poll ended"
        NotificationType.UPDATE -> "edited a post"
        NotificationType.STATUS -> "posted"
        NotificationType.OTHER -> "notification"
    }

    private fun formatRelative(instant: Instant): String {
        val now = Clock.System.now()
        val seconds = (now - instant).inWholeSeconds
        return when {
            seconds < 0 -> "just now"
            seconds < 60 -> "just now"
            seconds < 3600 -> "${seconds / 60} minutes ago"
            seconds < 86_400 -> "${seconds / 3600} hours ago"
            seconds < 604_800 -> "${seconds / 86_400} days ago"
            else -> "${seconds / 604_800} weeks ago"
        }
    }
}

data class TemplateConfig(
    val cwMode: CwMode = CwMode.HIDE,
    val includeMediaDescriptions: Boolean = true,
    val demojifyDisplayNames: Boolean = false,
    val maxUsernamesDisplay: Int = 0,
)

enum class CwMode(val key: String, val label: String) {
    HIDE("hide", "Hide post content behind CW"),
    SHOW("show", "Show CW and post content"),
    IGNORE("ignore", "Ignore CW, show post content");

    companion object {
        fun fromKey(key: String?): CwMode = entries.firstOrNull { it.key == key } ?: HIDE
    }
}
