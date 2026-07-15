package me.masonasons.fastsm.domain.model

data class UniversalMention(
    val id: String,
    val acct: String,
    val username: String,
    val url: String? = null,
)