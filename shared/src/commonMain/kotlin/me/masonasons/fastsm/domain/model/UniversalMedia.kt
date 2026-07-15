package me.masonasons.fastsm.domain.model

data class UniversalMedia(
    val id: String,
    val type: String,
    val url: String,
    val previewUrl: String? = null,
    val description: String? = null,
)