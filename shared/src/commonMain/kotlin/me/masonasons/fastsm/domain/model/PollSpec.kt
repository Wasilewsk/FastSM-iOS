package me.masonasons.fastsm.domain.model

data class PollSpec(
    val options: List<String>,
    val expiresInSec: Int,
    val multiple: Boolean,
    val hideTotals: Boolean,
)