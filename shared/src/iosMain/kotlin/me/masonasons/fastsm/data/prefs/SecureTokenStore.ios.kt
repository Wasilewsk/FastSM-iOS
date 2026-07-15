package me.masonasons.fastsm.data.prefs

actual class SecureTokenStore {
    private val storage = mutableMapOf<Long, Pair<String?, String?>>()

    actual fun getAccessToken(accountId: Long): String? = storage[accountId]?.first
    actual fun setAccessToken(accountId: Long, token: String) {
        val existing = storage[accountId]
        storage[accountId] = token to existing?.second
    }
    actual fun getRefreshToken(accountId: Long): String? = storage[accountId]?.second
    actual fun setRefreshToken(accountId: Long, token: String) {
        val existing = storage[accountId]
        storage[accountId] = existing?.first to token
    }
    actual fun clearAccessToken(accountId: Long) { storage.remove(accountId) }
}
