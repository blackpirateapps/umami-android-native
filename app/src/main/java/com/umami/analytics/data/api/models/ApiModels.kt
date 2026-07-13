package com.umami.analytics.data.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String? = null,
    val user: UserDto? = null
)

@Serializable
data class UserDto(
    val id: String? = null,
    val username: String? = null,
    val role: String? = null,
    val createdAt: String? = null
)

@Serializable
data class WebsiteDto(
    val id: String,
    val name: String,
    val domain: String? = null,
    val shareId: String? = null,
    val createdAt: String? = null
)

@Serializable
data class StatValue(
    val value: Long = 0,
    val change: Long = 0
)

@Serializable
data class WebsiteStatsDto(
    val pageviews: StatValue = StatValue(),
    val visitors: StatValue = StatValue(),
    val visits: StatValue = StatValue(),
    val bounces: StatValue = StatValue(),
    val totaltime: StatValue = StatValue()
)

@Serializable
data class ChartPointDto(
    val x: String,
    val y: Long = 0
)

@Serializable
data class PageviewsResponseDto(
    val pageviews: List<ChartPointDto> = emptyList(),
    val sessions: List<ChartPointDto> = emptyList()
)

@Serializable
data class MetricItemDto(
    val x: String? = "",
    val y: Long = 0
)

@Serializable
data class SessionItemDto(
    val id: String,
    val hostname: String? = null,
    val browser: String? = null,
    val os: String? = null,
    val device: String? = null,
    val screen: String? = null,
    val language: String? = null,
    val country: String? = null,
    val subdivision1: String? = null,
    val city: String? = null,
    val views: Int = 1,
    val visits: Int = 1,
    val events: Int = 0,
    val createdAt: String? = null,
    val firstAt: String? = null,
    val lastAt: String? = null
)

@Serializable
data class SessionResponseDto(
    val data: List<SessionItemDto> = emptyList(),
    val count: Int = 0,
    val page: Int = 1,
    val pageSize: Int = 20
)

enum class TimeRange(
    val label: String,
    val unit: String
) {
    LAST_24_HOURS("Last 24 hours", "hour"),
    THIS_WEEK("This week", "day"),
    LAST_7_DAYS("Last 7 days", "day"),
    LAST_30_DAYS("Last 30 days", "day"),
    THIS_MONTH("This month", "day"),
    THIS_YEAR("This year", "month"),
    LAST_6_MONTHS("Last 6 months", "month"),
    LAST_12_MONTHS("Last 12 months", "month"),
    ALL_TIME("All time", "month")
}

data class AnalyticsFilter(
    val page: String? = null,
    val country: String? = null,
    val region: String? = null,
    val referrer: String? = null
) {
    fun isEmpty() = page.isNullOrBlank() && country.isNullOrBlank() && region.isNullOrBlank() && referrer.isNullOrBlank()
    
    fun toCacheKey(): String {
        return "p=${page ?: ""}_c=${country ?: ""}_r=${region ?: ""}_ref=${referrer ?: ""}"
    }
}
