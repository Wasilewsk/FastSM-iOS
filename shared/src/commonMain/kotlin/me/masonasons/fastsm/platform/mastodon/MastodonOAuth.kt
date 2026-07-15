package me.masonasons.fastsm.platform.mastodon

object MastodonOAuth {
    const val SCOPES = "read write follow push"
    const val REDIRECT_URI = "fastsm://oauth"
    const val CLIENT_NAME = "FastSM"
    const val WEBSITE = "https://github.com/masonasons/FastSM"

    fun normalizeInstance(raw: String): String {
        val trimmed = raw.trim().removeSuffix("/")
        return when {
            trimmed.isEmpty() -> trimmed
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            else -> "https://$trimmed"
        }
    }

    fun buildAuthorizeUrl(instanceBase: String, clientId: String): String =
        "$instanceBase/oauth/authorize?response_type=code&client_id=$clientId" +
            "&redirect_uri=${encodeURIComponent(REDIRECT_URI)}&scope=${encodeURIComponent(SCOPES)}"

    fun extractCode(url: String): String? = extractQueryParam(url, "code")
    fun extractError(url: String): String? = extractQueryParam(url, "error")

    private fun extractQueryParam(url: String, key: String): String? {
        val queryStart = url.indexOf('?')
        if (queryStart < 0) return null
        val query = url.substring(queryStart + 1)
        return query.split('&')
            .firstOrNull { it.startsWith("$key=") }
            ?.substringAfter("$key=")
    }

    private fun encodeURIComponent(value: String): String =
        value.replace(" ", "%20")
            .replace("/", "%2F")
            .replace(":", "%3A")
}
