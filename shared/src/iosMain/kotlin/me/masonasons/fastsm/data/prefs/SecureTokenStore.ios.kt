package me.masonasons.fastsm.data.prefs

class IosSecureTokenStore : SecureTokenStore {
    private val storage = mutableMapOf<Long, Pair<String?, String?>>()

    override fun getAccessToken(accountId: Long): String? = storage[accountId]?.first
    override fun setAccessToken(accountId: Long, token: String) {
        val existing = storage[accountId]
        storage[accountId] = token to existing?.second
    }
    override fun getRefreshToken(accountId: Long): String? = storage[accountId]?.second
    override fun setRefreshToken(accountId: Long, token: String) {
        val existing = storage[accountId]
        storage[accountId] = existing?.first to token
    }
    override fun clearAccessToken(accountId: Long) { storage.remove(accountId) }
}
