package me.masonasons.fastsm.data.repo

import me.masonasons.fastsm.domain.model.TimelineSpec

class TimelineRepository(private val storage: TimelineStorage) {

    suspend fun get(accountId: Long): List<TimelineSpec> =
        storage.getByAccount(accountId).mapNotNull { it.toSpec() }

    suspend fun add(accountId: Long, spec: TimelineSpec) {
        if (!spec.closable) return
        val existing = storage.getByAccount(accountId)
        if (existing.any { it.toSpec() == spec }) return
        val nextPosition = (existing.maxOfOrNull { it.position } ?: 0) + 1
        storage.insert(spec.toStorage(accountId, nextPosition))
    }

    suspend fun remove(accountId: Long, spec: TimelineSpec) {
        if (!spec.closable) return
        storage.getByAccount(accountId).firstOrNull { it.toSpec() == spec }
            ?.let { storage.delete(it.rowId) }
    }

    suspend fun clearForAccount(accountId: Long) = storage.deleteByAccount(accountId)
}

data class StoredTimeline(
    val rowId: Long = 0,
    val accountId: Long,
    val kind: String,
    val position: Int,
    val instance: String? = null,
    val localOnly: Boolean? = null,
    val acct: String? = null,
    val userId: String? = null,
    val label: String? = null,
    val createdAt: Long = 0L,
)

interface TimelineStorage {
    suspend fun getByAccount(accountId: Long): List<StoredTimeline>
    suspend fun insert(timeline: StoredTimeline)
    suspend fun delete(rowId: Long)
    suspend fun deleteByAccount(accountId: Long)
}

private fun StoredTimeline.toSpec(): TimelineSpec? = when (kind) {
    "local" -> TimelineSpec.LocalPublic
    "federated" -> TimelineSpec.FederatedPublic
    "bookmarks" -> TimelineSpec.Bookmarks
    "favourites" -> TimelineSpec.Favourites
    "remote-instance" -> { val inst = instance ?: return null; TimelineSpec.RemoteInstance(inst, localOnly ?: true) }
    "remote-user" -> { val inst = instance ?: return null; val a = acct ?: return null; TimelineSpec.RemoteUser(inst, a) }
    "user-posts" -> { val uid = userId ?: return null; TimelineSpec.UserPosts(uid, label ?: uid) }
    "list" -> { val listId = userId ?: return null; TimelineSpec.UserList(listId, label ?: listId) }
    "hashtag" -> { val tag = acct ?: return null; TimelineSpec.Hashtag(tag) }
    else -> null
}

private fun TimelineSpec.toStorage(accountId: Long, position: Int): StoredTimeline = when (this) {
    TimelineSpec.LocalPublic -> base("local", accountId, position)
    TimelineSpec.FederatedPublic -> base("federated", accountId, position)
    TimelineSpec.Bookmarks -> base("bookmarks", accountId, position)
    TimelineSpec.Favourites -> base("favourites", accountId, position)
    is TimelineSpec.RemoteInstance -> base("remote-instance", accountId, position).copy(instance = instance, localOnly = localOnly)
    is TimelineSpec.RemoteUser -> base("remote-user", accountId, position).copy(instance = instance, acct = acct)
    is TimelineSpec.UserPosts -> base("user-posts", accountId, position).copy(userId = userId, label = displayName)
    is TimelineSpec.UserList -> base("list", accountId, position).copy(userId = listId, label = title)
    is TimelineSpec.Hashtag -> base("hashtag", accountId, position).copy(acct = tag)
    TimelineSpec.Home, TimelineSpec.Notifications -> error("Home and Notifications are implicit")
}

private fun base(kind: String, accountId: Long, position: Int) = StoredTimeline(
    accountId = accountId, position = position, kind = kind, createdAt = 0L,
)
