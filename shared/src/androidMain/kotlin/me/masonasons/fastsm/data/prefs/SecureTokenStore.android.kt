package me.masonasons.fastsm.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AndroidSecureTokenStore(context: Context) : SecureTokenStore {
    private val prefs: SharedPreferences = run {
        val key = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context, "fastsm_secure", key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override fun getAccessToken(accountId: Long): String? = prefs.getString("token_$accountId", null)
    override fun setAccessToken(accountId: Long, token: String) { prefs.edit().putString("token_$accountId", token).apply() }
    override fun getRefreshToken(accountId: Long): String? = prefs.getString("refresh_$accountId", null)
    override fun setRefreshToken(accountId: Long, token: String) { prefs.edit().putString("refresh_$accountId", token).apply() }
    override fun clearAccessToken(accountId: Long) { prefs.edit().remove("token_$accountId").remove("refresh_$accountId").apply() }
}
