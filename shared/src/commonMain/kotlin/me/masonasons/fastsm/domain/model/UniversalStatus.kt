package me.masonasons.fastsm.domain.model

import kotlinx.datetime.Instant

data class UniversalStatus(
    val id: String,
    val account: UniversalUser,
    val content: String,
    val text: String,
    val createdAt: Instant,
    val favouritesCount: Int = 0,
    val boostsCount: Int = 0,
    val repliesCount: Int = 0,
    val inReplyToId: String? = null,
    val inReplyToAccountId: String? = null,
    val reblog: UniversalStatus? = null,
    val quote: UniversalStatus? = null,
    val mediaAttachments: List<UniversalMedia> = emptyList(),
    val mentions: List<UniversalMention> = emptyList(),
    val url: String? = null,
    val visibility: Visibility? = null,
    val spoilerText: String? = null,
    val pinned: Boolean = false,
    val favourited: Boolean = false,
    val boosted: Boolean = false,
    val bookmarked: Boolean = false,
    val platform: PlatformType,
    val platformLikeUri: String? = null,
    val platformRepostUri: String? = null,
    val platformCid: String? = null,
    val links: List<String> = emptyList(),
)