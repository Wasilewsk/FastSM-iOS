package me.masonasons.fastsm.data.repo

class TimelinePositionRepository(private val storage: TimelinePositionStorage) {
    suspend fun get(accountId: Long, timelineId: String): String? = storage.getStatusId(accountId, timelineId)
    suspend fun save(accountId: Long, timelineId: String, statusId: String) = storage.upsert(accountId, timelineId, statusId)
    suspend fun clear(accountId: Long, timelineId: String) = storage.delete(accountId, timelineId)
    suspend fun clearForAccount(accountId: Long) = storage.deleteByAccount(accountId)
}

interface TimelinePositionStorage {
    suspend fun getStatusId(accountId: Long, timelineId: String): String?
    suspend fun upsert(accountId: Long, timelineId: String, statusId: String)
    suspend fun delete(accountId: Long, timelineId: String)
    suspend fun deleteByAccount(accountId: Long)
}
