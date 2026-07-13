package com.umami.analytics.util

object IconHelper {

    fun getFaviconUrl(domainOrUrl: String?): String? {
        if (domainOrUrl.isNullOrBlank()) return null
        val cleanDomain = domainOrUrl
            .replace(Regex("^https?://"), "")
            .split("/")
            .firstOrNull()
            ?.trim() ?: return null

        if (cleanDomain.isBlank() || cleanDomain.contains("Direct / None", true) || cleanDomain.contains("(direct)", true)) {
            return null
        }

        return "https://www.google.com/s2/favicons?domain=$cleanDomain&sz=64"
    }

    fun getBrowserEmoji(browser: String?): String {
        if (browser.isNullOrBlank()) return "🌐"
        val name = browser.lowercase()
        return when {
            name.contains("chrome") -> "🌐"
            name.contains("firefox") -> "🦊"
            name.contains("safari") -> "🧭"
            name.contains("edge") -> "🌐"
            name.contains("brave") -> "🦁"
            name.contains("opera") -> "🔴"
            name.contains("arc") -> "🌈"
            name.contains("tor") -> "🧅"
            name.contains("vivaldi") -> "🔴"
            name.contains("samsung") -> "📱"
            name.contains("duckduckgo") -> "🦆"
            name.contains("android") -> "🤖"
            else -> "🌐"
        }
    }

    fun getOsEmoji(os: String?): String {
        if (os.isNullOrBlank()) return "💻"
        val name = os.lowercase()
        return when {
            name.contains("linux") -> "🐧"
            name.contains("ubuntu") -> "🟠"
            name.contains("debian") -> "🌀"
            name.contains("fedora") -> "🔵"
            name.contains("arch") -> "🌐"
            name.contains("windows") -> "🪟"
            name.contains("mac") || name.contains("ios") -> "🍎"
            name.contains("android") -> "🤖"
            name.contains("chrome") -> "💻"
            name.contains("bsd") -> "😈"
            else -> "💻"
        }
    }
}
