package me.masonasons.fastsm.data.repo

import me.masonasons.fastsm.data.prefs.AppPrefsStore
import me.masonasons.fastsm.data.prefs.SecureTokenStore
import me.masonasons.fastsm.domain.model.Account
import me.masonasons.fastsm.domain.model.PlatformType

class AccountRepository(
    private val tokenStore: SecureTokenStore,
    private val appPrefs: AppPrefsStore,
    private val storage: AccountStorage,
) {

    suspend fun getAll(): List<Account> = storage.getAll()
    suspend fun getById(id: Long): Account? = storage.getById(id)

    suspend fun getActiveAccount(): Account? {
        val id = appPrefs.activeAccountId ?: return getAll().firstOrNull()
        return getById(id) ?: getAll().firstOrNull()
    }

    suspend fun saveMastodonAccount(
        instance: String, userId: String, acct: String, displayName: String,
        avatar: String?, accessToken: String, clientId: String, clientSecret: String,
    ): Long {
        val account = Account(
            id = 0, platform = PlatformType.MASTODON, instance = instance,
            userId = userId, acct = acct, displayName = displayName, avatar = avatar,
        )
        val id = storage.upsert(account, clientId, clientSecret)
        tokenStore.setAccessToken(id, accessToken)
        if (appPrefs.activeAccountId == null) appPrefs.setActiveAccountId(id)
        return id
    }

    suspend fun saveBlueskyAccount(
        pdsBase: String, did: String, handle: String, displayName: String,
        avatar: String?, accessJwt: String, refreshJwt: String,
    ): Long {
        val account = Account(
            id = 0, platform = PlatformType.BLUESKY, instance = pdsBase,
            userId = did, acct = handle, displayName = displayName, avatar = avatar,
        )
        val id = storage.upsert(account, null, null)
        tokenStore.setAccessToken(id, accessJwt)
        tokenStore.setRefreshToken(id, refreshJwt)
        if (appPrefs.activeAccountId == null) appPrefs.setActiveAccountId(id)
        return id
    }

    suspend fun delete(id: Long) {
        storage.deleteById(id)
        tokenStore.clearAccessToken(id)
        if (appPrefs.activeAccountId == id) {
            val fallback = getAll().firstOrNull()?.id
            appPrefs.setActiveAccountId(fallback)
        }
    }

    fun setActive(id: Long) = appPrefs.setActiveAccountId(id)
    fun tokenFor(id: Long): String? = tokenStore.getAccessToken(id)
}

interface AccountStorage {
    suspend fun getAll(): List<Account>
    suspend fun getById(id: Long): Account?
    suspend fun upsert(account: Account, clientId: String?, clientSecret: String?): Long
    suspend fun deleteById(id: Long)
}
