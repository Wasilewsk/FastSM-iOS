package me.masonasons.fastsm.platform.mastodon

object HtmlStrip {
    fun toPlainText(html: String): String {
        if (html.isBlank()) return ""
        return html
            .replace(Regex("<br\\s*/?>"), "\n")
            .replace(Regex("</p><p>"), "\n\n")
            .replace(Regex("<p>"), "")
            .replace(Regex("</p>"), "")
            .replace(Regex("<[^>]+>"), "")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .trim()
    }

    private val leadingReRegex = Regex("""^(RE|QT|re|qt):\s*https?://\S+\s*""")
    private val trailingStatusUrlRegex = Regex("""\s*https?://[^\s]+/@[^\s]+/\d+\s*$""")

    fun stripQuoteMarkers(text: String, quoteUrl: String?): String {
        if (text.isBlank()) return text
        var result = leadingReRegex.replace(text, "").trim()
        if (!quoteUrl.isNullOrBlank() && result.endsWith(quoteUrl)) {
            result = result.substring(0, result.length - quoteUrl.length).trimEnd()
        }
        result = trailingStatusUrlRegex.replace(result, "").trim()
        return result
    }

    fun extractLinks(html: String): List<String> {
        if (html.isBlank()) return emptyList()
        val hrefRegex = Regex("""href=["'](https?://[^"']+)["']""")
        return hrefRegex.findAll(html).mapNotNull { match ->
            val url = match.groupValues[1]
            if (url.contains("/@") || url.contains("/tags/")) null else url
        }.toList()
    }
}
