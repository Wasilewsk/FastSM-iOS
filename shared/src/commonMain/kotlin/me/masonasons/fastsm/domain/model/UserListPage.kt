package me.masonasons.fastsm.domain.model

data class UserListPage(
    val users: List<UniversalUser>,
    val nextCursor: String?,
)