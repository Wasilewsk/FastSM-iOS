package me.masonasons.fastsm.domain.template

object Demojify {

    private val shortcode = Regex(":[a-zA-Z0-9_+\\-]+:")
    private val unicodeEmoji = Regex(
        "[\\u2600-\\u27BF]" +
            "|[\\uFE00-\\uFE0F]" +
            "|\\u200D" +
            "|[\\uD83C-\\uD83E][\\uDC00-\\uDFFF]"
    )

    fun demojify(input: String): String {
        if (input.isEmpty()) return input
        return unicodeEmoji.replace(shortcode.replace(input, ""), "").trim()
    }
}
