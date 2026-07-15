package me.masonasons.fastsm.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.masonasons.fastsm.data.feedback.FeedbackManager
import me.masonasons.fastsm.data.feedback.HapticsEngine
import me.masonasons.fastsm.data.feedback.IosHapticsEngine
import me.masonasons.fastsm.data.feedback.IosSoundEngine
import me.masonasons.fastsm.data.feedback.IosSpeechEngine
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
import me.masonasons.fastsm.util.AppInfo

object IosModule {
    fun createDefaultHttpClient(): HttpClient = HttpClient(platformHttpClientEngine()) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }
        install(Logging) { level = LogLevel.INFO }
        install(WebSockets)
        install(DefaultRequest) {
            header(HttpHeaders.Accept, "application/json")
            header(HttpHeaders.UserAgent, AppInfo.USER_AGENT)
        }
    }
}

class InMemoryAccountStorage : AccountStorage {
    private val accounts = mutableMapOf<Long, AccountRow>()

    data class AccountRow(
        val account: me.masonasons.fastsm.domain.model.Account,
        val clientId: String?,
        val clientSecret: String?,
    )

    private var nextId = 1L

    override suspend fun getAll(): List<me.masonasons.fastsm.domain.model.Account> = accounts.values.map { it.account }
    override suspend fun getById(id: Long): me.masonasons.fastsm.domain.model.Account? = accounts[id]?.account
    override suspend fun upsert(account: me.masonasons.fastsm.domain.model.Account, clientId: String?, clientSecret: String?): Long {
        val id = if (account.id > 0) account.id else nextId++
        accounts[id] = AccountRow(account.copy(id = id), clientId, clientSecret)
        return id
    }
    override suspend fun deleteById(id: Long) { accounts.remove(id) }
}

class InMemoryTimelineStorage : TimelineStorage {
    private val timelines = mutableListOf<me.masonasons.fastsm.data.repo.StoredTimeline>()
    private var nextRowId = 1L

    override suspend fun getByAccount(accountId: Long): List<me.masonasons.fastsm.data.repo.StoredTimeline> =
        timelines.filter { it.accountId == accountId }.sortedBy { it.position }
    override suspend fun insert(timeline: me.masonasons.fastsm.data.repo.StoredTimeline) {
        timelines.add(timeline.copy(rowId = nextRowId++))
    }
    override suspend fun delete(rowId: Long) { timelines.removeAll { it.rowId == rowId } }
    override suspend fun deleteByAccount(accountId: Long) { timelines.removeAll { it.accountId == accountId } }
}

class InMemoryTimelinePositionStorage : TimelinePositionStorage {
    private val positions = mutableMapOf<String, String>()
    override suspend fun getStatusId(accountId: Long, timelineId: String): String? = positions["$accountId:$timelineId"]
    override suspend fun upsert(accountId: Long, timelineId: String, statusId: String) { positions["$accountId:$timelineId"] = statusId }
    override suspend fun delete(accountId: Long, timelineId: String) { positions.remove("$accountId:$timelineId") }
    override suspend fun deleteByAccount(accountId: Long) { positions.keys.removeAll { it.startsWith("$accountId:") } }
}

class InMemoryAppPrefs : AppPrefsStore {
    override var activeAccountId: Long? = null
    override fun setActiveAccountId(id: Long?) { activeAccountId = id }
    override var streamingEnabled: Boolean = true
    override fun setStreamingEnabled(enabled: Boolean) { streamingEnabled = enabled }
    override var markerSyncEnabled: Boolean = true
    override fun setMarkerSyncEnabled(enabled: Boolean) { markerSyncEnabled = enabled }
    override var rememberTimelinePositions: Boolean = false
    override fun setRememberTimelinePositions(enabled: Boolean) { rememberTimelinePositions = enabled }
    override var fetchPages: Int = 1
    override fun setFetchPages(value: Int) { fetchPages = value }
    override var autoFocusCompose: Boolean = true
    override fun setAutoFocusCompose(enabled: Boolean) { autoFocusCompose = enabled }
    override var submitOnImeAction: Boolean = false
    override fun setSubmitOnImeAction(enabled: Boolean) { submitOnImeAction = enabled }
    override var postTemplate: String = me.masonasons.fastsm.domain.template.PostTemplateRenderer.DEFAULT_POST
    override fun setPostTemplate(value: String) { postTemplate = value }
    override var boostTemplate: String = me.masonasons.fastsm.domain.template.PostTemplateRenderer.DEFAULT_BOOST
    override fun setBoostTemplate(value: String) { boostTemplate = value }
    override var notificationTemplate: String = me.masonasons.fastsm.domain.template.PostTemplateRenderer.DEFAULT_NOTIFICATION
    override fun setNotificationTemplate(value: String) { notificationTemplate = value }
    override var cwMode: me.masonasons.fastsm.domain.template.CwMode = me.masonasons.fastsm.domain.template.CwMode.HIDE
    override fun setCwMode(mode: me.masonasons.fastsm.domain.template.CwMode) { cwMode = mode }
    override var includeMediaDescriptions: Boolean = true
    override fun setIncludeMediaDescriptions(enabled: Boolean) { includeMediaDescriptions = enabled }
    override var demojifyDisplayNames: Boolean = false
    override fun setDemojifyDisplayNames(enabled: Boolean) { demojifyDisplayNames = enabled }
    override var maxUsernamesDisplay: Int = 0
    override fun setMaxUsernamesDisplay(value: Int) { maxUsernamesDisplay = value }
    override var enabledPostActions: Set<me.masonasons.fastsm.domain.model.PostAction> = me.masonasons.fastsm.domain.model.PostAction.ALL
    override fun setPostActionEnabled(action: me.masonasons.fastsm.domain.model.PostAction, enabled: Boolean) {
        enabledPostActions = if (enabled) enabledPostActions + action else enabledPostActions - action
    }
}

class InMemoryFeedbackPrefs : FeedbackPrefsStore {
    override var speechEnabled: Boolean = true
    override fun setSpeechEnabled(v: Boolean) { speechEnabled = v }
    override var speakTabLoaded: Boolean = true
    override fun setSpeakTabLoaded(v: Boolean) { speakTabLoaded = v }
    override var speakNotification: Boolean = true
    override fun setSpeakNotification(v: Boolean) { speakNotification = v }
    override var speakPostSent: Boolean = false
    override fun setSpeakPostSent(v: Boolean) { speakPostSent = v }
    override var speakError: Boolean = true
    override fun setSpeakError(v: Boolean) { speakError = v }
    override var soundEnabled: Boolean = true
    override fun setSoundEnabled(v: Boolean) { soundEnabled = v }
    override var soundVolume: Float = 1.0f
    override fun setSoundVolume(v: Float) { soundVolume = v }
    override var mutedSpecs: Set<String> = emptySet()
    override fun toggleMuted(specId: String) { mutedSpecs = if (specId in mutedSpecs) mutedSpecs - specId else mutedSpecs + specId }
    override var hapticsEnabled: Boolean = true
    override fun setHapticsEnabled(v: Boolean) { hapticsEnabled = v }
    override var hapticsPostSent: Boolean = true
    override fun setHapticsPostSent(v: Boolean) { hapticsPostSent = v }
    override var hapticsNewPost: Boolean = false
    override fun setHapticsNewPost(v: Boolean) { hapticsNewPost = v }
    override var hapticsNotification: Boolean = true
    override fun setHapticsNotification(v: Boolean) { hapticsNotification = v }
    override var hapticsError: Boolean = true
    override fun setHapticsError(v: Boolean) { hapticsError = v }
    override var accountSoundpacks: Map<Long, String> = emptyMap()
    override fun setAccountSoundpack(accountId: Long, pack: String) { accountSoundpacks = accountSoundpacks + (accountId to pack) }
    override fun soundpackFor(accountId: Long?): String = if (accountId == null) "default" else accountSoundpacks[accountId] ?: "default"
}
