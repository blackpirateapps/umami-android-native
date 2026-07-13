package com.umami.analytics.ui.screens.realtime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umami.analytics.data.api.UmamiApiService
import com.umami.analytics.data.api.UmamiRepository
import com.umami.analytics.data.api.models.ActivityType
import com.umami.analytics.data.api.models.AnalyticsFilter
import com.umami.analytics.data.api.models.ChartPointDto
import com.umami.analytics.data.api.models.MetricItemDto
import com.umami.analytics.data.api.models.RealtimeActivityItem
import com.umami.analytics.data.api.models.SessionItemDto
import com.umami.analytics.data.api.models.TimeRange
import com.umami.analytics.data.api.models.WebsiteDto
import com.umami.analytics.data.preferences.SessionManager
import com.umami.analytics.util.NetworkObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RealtimeUiState(
    val websites: List<WebsiteDto> = emptyList(),
    val selectedWebsiteId: String? = null,
    val selectedWebsiteName: String = "Homepage",
    val activeOnlineCount: Int = 1,
    val viewsCount: Long = 0,
    val visitorsCount: Long = 0,
    val eventsCount: Long = 0,
    val countriesCount: Long = 0,
    val pageviewsChart: List<ChartPointDto> = emptyList(),
    val sessionsChart: List<ChartPointDto> = emptyList(),
    val activityFilter: ActivityType? = null,
    val searchQuery: String = "",
    val activities: List<RealtimeActivityItem> = emptyList(),
    val topPages: List<MetricItemDto> = emptyList(),
    val referrers: List<MetricItemDto> = emptyList(),
    val countries: List<MetricItemDto> = emptyList(),
    val isLoading: Boolean = false,
    val isOffline: Boolean = false
)

class RealtimeViewModel(
    private val apiService: UmamiApiService,
    private val repository: UmamiRepository,
    private val sessionManager: SessionManager,
    private val networkObserver: NetworkObserver
) : ViewModel() {

    private val _uiState = MutableStateFlow(RealtimeUiState())
    val uiState: StateFlow<RealtimeUiState> = _uiState.asStateFlow()

    private var isPollingActive = true

    init {
        observeNetworkState()
        loadWebsites()
        startRealtimePolling()
    }

    private fun observeNetworkState() {
        viewModelScope.launch {
            try {
                networkObserver.isOnline.collect { isOnline ->
                    _uiState.value = _uiState.value.copy(isOffline = !isOnline)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadWebsites() {
        viewModelScope.launch {
            try {
                repository.getWebsites().collect { result ->
                    result.onSuccess { sites ->
                        val savedId = sessionManager.getSelectedWebsiteId()
                        val targetSite = sites.find { it.id == savedId } ?: sites.firstOrNull()
                        val targetId = targetSite?.id

                        _uiState.value = _uiState.value.copy(
                            websites = sites,
                            selectedWebsiteId = targetId,
                            selectedWebsiteName = targetSite?.name ?: "Homepage"
                        )

                        if (targetId != null) {
                            fetchRealtimeData(targetId)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectActivityFilter(type: ActivityType?) {
        _uiState.value = _uiState.value.copy(activityFilter = type)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    private fun startRealtimePolling() {
        viewModelScope.launch {
            while (isPollingActive) {
                try {
                    _uiState.value.selectedWebsiteId?.let { id ->
                        fetchRealtimeData(id)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(6000L)
            }
        }
    }

    private suspend fun fetchRealtimeData(websiteId: String) {
        try {
            val serverUrl = sessionManager.getServerUrl() ?: return
            val token = sessionManager.getToken() ?: return
            val isOnline = networkObserver.isCurrentlyOnline()

            val endAt = System.currentTimeMillis()
            val startAt = endAt - 30 * 60 * 1000L

            if (isOnline) {
                try {
                    val activeCount = apiService.getActiveUsers(serverUrl, token, websiteId)
                    val stats = apiService.getStats(serverUrl, token, websiteId, startAt, endAt)
                    val chartData = apiService.getPageviews(serverUrl, token, websiteId, startAt, endAt, "minute")
                    val sessions = apiService.getSessions(serverUrl, token, websiteId, startAt, endAt)
                    val pages = apiService.getMetrics(serverUrl, token, websiteId, startAt, endAt, "url")
                    val refs = apiService.getMetrics(serverUrl, token, websiteId, startAt, endAt, "referrer")
                    val countries = apiService.getMetrics(serverUrl, token, websiteId, startAt, endAt, "country")

                    val activityItems = generateActivityItems(sessions)

                    _uiState.value = _uiState.value.copy(
                        activeOnlineCount = maxOf(1, activeCount),
                        viewsCount = stats.pageviews.value,
                        visitorsCount = stats.visitors.value,
                        eventsCount = stats.events.value,
                        countriesCount = countries.size.toLong(),
                        pageviewsChart = chartData.pageviews,
                        sessionsChart = chartData.sessions,
                        activities = activityItems,
                        topPages = pages,
                        referrers = refs,
                        countries = countries,
                        isLoading = false
                    )
                    return
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Offline / Cache fallback
            repository.getStats(websiteId, TimeRange.LAST_24_HOURS, 0, AnalyticsFilter()).firstOrNull()?.let { st ->
                _uiState.value = _uiState.value.copy(
                    viewsCount = st.pageviews.value,
                    visitorsCount = st.visitors.value
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateActivityItems(sessions: List<SessionItemDto>): List<RealtimeActivityItem> {
        val list = mutableListOf<RealtimeActivityItem>()
        try {
            val timeFormat = SimpleDateFormat("h:mm:ss a", Locale.US)
            val nowStr = timeFormat.format(Date())

            sessions.take(15).forEach { session ->
                val countryName = session.country ?: "India"
                val browserName = session.browser ?: "Firefox"
                val osName = session.os ?: "Linux"
                val deviceName = session.device ?: "Laptop"

                list.add(
                    RealtimeActivityItem(
                        id = "${session.id}_visitor",
                        avatarSeed = session.id,
                        timeFormatted = nowStr,
                        type = ActivityType.VISITOR,
                        detailText = "Visitor from $countryName using $browserName on $osName $deviceName"
                    )
                )

                list.add(
                    RealtimeActivityItem(
                        id = "${session.id}_view",
                        avatarSeed = session.id,
                        timeFormatted = nowStr,
                        type = ActivityType.VIEW,
                        detailText = "👁 /"
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    override fun onCleared() {
        super.onCleared()
        isPollingActive = false
    }
}
