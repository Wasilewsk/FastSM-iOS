package me.masonasons.fastsm.domain.model

import kotlinx.datetime.Instant

data class PostRequest(
    val text: String,
    val inReplyToId: String? = null,
    val visibility: Visibility? = null,
    val spoilerText: String? = null,
    val mediaIds: List<String> = emptyList(),
    val poll: PollSpec? = null,
    val scheduledAt: Instant? = null,
    val quoteStatusId: String? = null,
    val quoteStatusCid: String? = null,
)