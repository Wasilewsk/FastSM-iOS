package me.masonasons.fastsm.di

import io.ktor.client.HttpClient
import me.masonasons.fastsm.data.feedback.FeedbackManager
import me.masonasons.fastsm.data.feedback.HapticsEngine
import me.masonasons.fastsm.data.feedback.SoundEngine
import me.masonasons.fastsm.data.feedback.SpeechEngine
import me.masonasons.fastsm.data.prefs.AppPrefsStore
import me.masonasons.fastsm.data.prefs.FeedbackPrefsStore
import me.masonasons.fastsm.data.prefs.SecureTokenStore
import me.masonasons.fastsm.data.repo.AccountRepository
import me.masonasons.fastsm.data.repo.AccountStorage
import me.masonasons.fastsm.data.repo.TimelinePositionRepository
import me.masonasons.fastsm.data.repo.TimelinePositionStorage
import me.masonasons.fastsm.data.repo.TimelineRepository
import me.masonasons.fastsm.data.repo.TimelineStorage
import me.masonasons.fastsm.platform.PlatformAccount
import me.masonasons.fastsm.platform.mastodon.MastodonPlatform
import me.masonasons.fastsm.domain.model.Account

class AppContainer(
    val httpClient: HttpClient,
    val tokenStore: SecureTokenStore,
    val appPrefs: AppPrefsStore,
    val feedbackPrefs: FeedbackPrefsStore,
    val accountStorage: AccountStorage,
    val timelineStorage: TimelineStorage,
    val timelinePositionStorage: TimelinePositionStorage,
    val speechEngine: SpeechEngine,
    val soundEngine: SoundEngine,
    val hapticsEngine: HapticsEngine,
) {
    val accountRepository: AccountRepository by lazy {
        AccountRepository(tokenStore, appPrefs, accountStorage)
    }

    val timelineRepository: TimelineRepository by lazy {
        TimelineRepository(timelineStorage)
    }

    val timelinePositionRepository: TimelinePositionRepository by lazy {
        TimelinePositionRepository(timelinePositionStorage)
    }

    val feedbackManager: FeedbackManager by lazy {
        FeedbackManager(feedbackPrefs, speechEngine, soundEngine, hapticsEngine)
    }

    private val platformCache = mutableMapOf<Long, PlatformAccount>()
    private val lock = Any()

    fun forAccount(account: Account): PlatformAccount = synchronized(lock) {
        platformCache.getOrPut(account.id) {
            MastodonPlatform(account, httpClient) { accountRepository.tokenFor(account.id) }
        }
    }

    fun invalidatePlatform(accountId: Long) = synchronized(lock) { platformCache.remove(accountId) }
}
