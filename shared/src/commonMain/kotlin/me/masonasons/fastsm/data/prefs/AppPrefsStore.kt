package me.masonasons.fastsm.data.prefs

interface AppPrefsStore {
    val activeAccountId: Long?
    fun setActiveAccountId(id: Long?)
    val streamingEnabled: Boolean
    fun setStreamingEnabled(enabled: Boolean)
    val markerSyncEnabled: Boolean
    fun setMarkerSyncEnabled(enabled: Boolean)
    val rememberTimelinePositions: Boolean
    fun setRememberTimelinePositions(enabled: Boolean)
    val fetchPages: Int
    fun setFetchPages(value: Int)
    val autoFocusCompose: Boolean
    fun setAutoFocusCompose(enabled: Boolean)
    val submitOnImeAction: Boolean
    fun setSubmitOnImeAction(enabled: Boolean)
    val postTemplate: String
    fun setPostTemplate(value: String)
    val boostTemplate: String
    fun setBoostTemplate(value: String)
    val notificationTemplate: String
    fun setNotificationTemplate(value: String)
    val cwMode: me.masonasons.fastsm.domain.template.CwMode
    fun setCwMode(mode: me.masonasons.fastsm.domain.template.CwMode)
    val includeMediaDescriptions: Boolean
    fun setIncludeMediaDescriptions(enabled: Boolean)
    val demojifyDisplayNames: Boolean
    fun setDemojifyDisplayNames(enabled: Boolean)
    val maxUsernamesDisplay: Int
    fun setMaxUsernamesDisplay(value: Int)
    val enabledPostActions: Set<me.masonasons.fastsm.domain.model.PostAction>
    fun setPostActionEnabled(action: me.masonasons.fastsm.domain.model.PostAction, enabled: Boolean)
}
