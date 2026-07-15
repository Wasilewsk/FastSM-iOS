package me.masonasons.fastsm.domain.model

data class MediaUploadResult(
    val mediaId: String,
    val previewUrl: String?,
    val processing: Boolean,
)