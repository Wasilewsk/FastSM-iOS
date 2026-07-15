package me.masonasons.fastsm.platform

import kotlinx.coroutines.flow.Flow
import me.masonasons.fastsm.domain.model.Account
import me.masonasons.fastsm.domain.model.MediaUploadResult
import me.masonasons.fastsm.domain.model.PlatformType
import me.masonasons.fastsm.domain.model.PostRequest
import me.masonasons.fastsm.domain.model.Relationship
import me.masonasons.fastsm.domain.model.SearchResults
import me.masonasons.fastsm.domain.model.StatusContext
import me.masonasons.fastsm.domain.model.TimelineEvent
import me.masonasons.fastsm.domain.model.UniversalNotification
import me.masonasons.fastsm.domain.model.StatusSource
import me.masonasons.fastsm.domain.model.UniversalStatus
import me.masonasons.fastsm.domain.model.UniversalUser
import me.masonasons.fastsm.domain.model.UserListInfo
import me.masonasons.fastsm.domain.model.UserListPage

interface PlatformAccount {

    val account: Account
    val platform: PlatformType
    val supportsVisibility: Boolean
    val supportsContentWarning: Boolean

    suspend fun getMaxPostChars(): Int

    val maxPageSize: Int get() = 40

    suspend fun verifyCredentials(): UniversalUser
    suspend fun getHomeTimeline(limit: Int = 40, maxId: String? = null): List<UniversalStatus>
    suspend fun getLocalTimeline(limit: Int = 40, maxId: String? = null): List<UniversalStatus>
    suspend fun getFederatedTimeline(limit: Int = 40, maxId: String? = null): List<UniversalStatus>

    suspend fun getRemoteInstanceTimeline(
        instance: String, localOnly: Boolean, limit: Int = 40, maxId: String? = null,
    ): List<UniversalStatus>

    suspend fun getRemoteUserTimeline(
        instance: String, acct: String, limit: Int = 40, maxId: String? = null,
    ): List<UniversalStatus>

    suspend fun getBookmarks(limit: Int = 40, maxId: String? = null): List<UniversalStatus>
    suspend fun getFavourites(limit: Int = 40, maxId: String? = null): List<UniversalStatus>
    suspend fun getUserLists(): List<UserListInfo>
    suspend fun getListTimeline(listId: String, limit: Int = 40, maxId: String? = null): List<UniversalStatus>
    suspend fun getHashtagTimeline(tag: String, limit: Int = 40, maxId: String? = null): List<UniversalStatus>
    suspend fun getListsContainingUser(userId: String): List<UserListInfo>
    suspend fun addUserToList(listId: String, userId: String): Boolean
    suspend fun removeUserFromList(listId: String, userId: String): Boolean
    suspend fun getStatus(statusId: String): UniversalStatus
    suspend fun resolveLocal(status: UniversalStatus): UniversalStatus = status
    suspend fun getStatusContext(statusId: String): StatusContext
    suspend fun getNotifications(limit: Int = 40, maxId: String? = null): List<UniversalNotification>
    suspend fun getUser(userId: String): UniversalUser
    suspend fun getUserStatuses(userId: String, limit: Int = 40, maxId: String? = null, excludeReplies: Boolean = false): List<UniversalStatus>
    suspend fun getRelationship(userId: String): Relationship?
    suspend fun getRelationships(userIds: List<String>): Map<String, Relationship> =
        userIds.mapNotNull { id -> getRelationship(id)?.let { id to it } }.toMap()
    suspend fun follow(userId: String): Relationship
    suspend fun unfollow(userId: String): Relationship
    suspend fun setShowReblogs(userId: String, show: Boolean): Relationship? = null
    suspend fun mute(userId: String): Relationship
    suspend fun unmute(userId: String): Relationship
    suspend fun block(userId: String): Relationship
    suspend fun unblock(userId: String): Relationship
    suspend fun getFollowers(userId: String, limit: Int = 40, cursor: String? = null): UserListPage
    suspend fun getFollowing(userId: String, limit: Int = 40, cursor: String? = null): UserListPage
    suspend fun post(request: PostRequest): UniversalStatus
    val supportsScheduling: Boolean get() = false
    suspend fun schedulePost(request: PostRequest): Boolean =
        throw UnsupportedOperationException("Scheduling not supported on $platform")
    val supportsEdit: Boolean get() = false
    val supportsMedia: Boolean get() = false
    val maxMediaAttachments: Int get() = 4
    suspend fun uploadMedia(bytes: ByteArray, filename: String, mime: String, description: String?): MediaUploadResult =
        throw UnsupportedOperationException("Media upload not supported on $platform")
    suspend fun updateMediaDescription(mediaId: String, description: String): Boolean = false
    suspend fun getStatusSourceText(statusId: String): StatusSource? = null
    suspend fun editStatus(statusId: String, request: PostRequest): UniversalStatus =
        throw UnsupportedOperationException("Edit not supported on $platform")
    suspend fun deleteStatus(statusId: String): Boolean
    suspend fun favourite(statusId: String): Boolean
    suspend fun unfavourite(statusId: String): Boolean
    suspend fun boost(statusId: String): Boolean
    suspend fun unboost(statusId: String): Boolean
    suspend fun bookmark(statusId: String): Boolean
    suspend fun unbookmark(statusId: String): Boolean
    suspend fun search(query: String): SearchResults
    fun streamEvents(): Flow<TimelineEvent>
    suspend fun getHomeMarker(): String?
    suspend fun setHomeMarker(statusId: String): Boolean
}
