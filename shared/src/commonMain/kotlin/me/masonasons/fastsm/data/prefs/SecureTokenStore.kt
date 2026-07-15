package me.masonasons.fastsm.data.prefs

interface SecureTokenStore {
    fun getAccessToken(accountId: Long): String?
    fun setAccessToken(accountId: Long, token: String)
    fun getRefreshToken(accountId: Long): String?
    fun setRefreshToken(accountId: Long, token: String)
    fun clearAccessToken(accountId: Long)
}
