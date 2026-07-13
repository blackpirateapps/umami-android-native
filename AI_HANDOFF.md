# AI Handoff & Architecture Guide

> **Project Name**: Umami Analytics Native Android App  
> **Application ID**: `com.blackpiratex.umami`  
> **Version Name**: `1.1` (Version Code: `2`)  
> **Repository URL**: `https://github.com/blackpirateapps/umami-android-native.git`  
> **Target Android Version**: SDK 35 (Min SDK: 26)  

---

## 1. Project Architecture Overview

This project is a high-performance, native Android application built with **Jetpack Compose (Material 3 Expressive UI)**, **Offline-First Room Caching**, **Ktor HTTP Client (Kotlinx Serialization)**, **WorkManager**, and **MPAndroidChart**.

It acts as a native mobile dashboard for self-hosted or cloud **Umami Analytics** instances (v1 and v2 API compatible).

```
 ┌────────────────────────────────────────────────────────────────────────┐
 │                              Presentation                              │
 │   MainActivity -> AppNavigation (NavHost with Smooth Slide Animations) │
 │     ├── LoginScreen (Server URL & Credentials Validation)              │
 │     ├── OverviewScreen (Views, Visitors, Visits, MPAndroidChart, Cards)│
 │     ├── RealtimeScreen (6s Polling, Active Online Badge, Live Feed)    │
 │     ├── SessionsScreen (Paginated Session Table with Detail Dialogs)   │
 │     ├── WebsitesScreen & WebsiteDetailScreen (Tracking Code Card)      │
 │     └── SettingsScreen (Theme Mode, Widget Sync Interval, Privacy)     │
 └───────────────────────────────────┬────────────────────────────────────┘
                                     │
 ┌───────────────────────────────────▼────────────────────────────────────┐
 │                           Domain & Data Layer                          │
 │                      UmamiRepository (Offline-First)                   │
 │       ┌───────────────────────────┴───────────────────────────┐        │
 │       ▼                                                       ▼        │
 │   Room DB (Offline Cache)                        Ktor ApiService       │
 │   - WebsiteEntity                                - Auth & Login        │
 │   - StatEntity                                   - Active Users        │
 │   - MetricEntity                                 - Pageviews & Stats   │
 └────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Directory Sitemap & File Reference Links

### Core & Application Entry
- [MainActivity.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/MainActivity.kt): Installs Splashscreen, handles edge-to-edge, and binds root reactive theme mode (`UmamiTheme`).
- [UmamiApplication.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/UmamiApplication.kt): Application singletons (`UmamiDatabase`, `UmamiApiService`, `SessionManager`, `NetworkObserver`).
- [AndroidManifest.xml](file:///home/dog/git/umami-android-native/app/src/main/AndroidManifest.xml): Application manifest declaring permissions, widget receiver, and config activities.

### Navigation & Shell
- [AppNavigation.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/ui/navigation/AppNavigation.kt): Main `NavHost` configuring state preservation (`saveState = true`, `restoreState = true`) and smooth horizontal slide transitions.
- [AppSidebar.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/ui/components/AppSidebar.kt): Modal Navigation Drawer listing Overview, Realtime, Sessions, Websites, Settings, and Website Selector.

### UI Screens & ViewModels
- **Overview**:
  - [OverviewScreen.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/ui/screens/overview/OverviewScreen.kt): Stat cards (Views, Visitors, Visits), Time-range selector, Filter dialog, MPAndroidChart dual-bar dataset, and Metric Cards.
  - [OverviewViewModel.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/ui/screens/overview/OverviewViewModel.kt): State flow managing time ranges, filters, date offsets, and repository fetching.
- **Realtime**:
  - [RealtimeScreen.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/ui/screens/realtime/RealtimeScreen.kt): Live `🟢 1 Online` badge, 4 Stat cards, 30-minute minute-by-minute bar chart, Activity Feed with search/filter tabs, and Breakdown Cards.
  - [RealtimeViewModel.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/ui/screens/realtime/RealtimeViewModel.kt): Polling engine running every 6 seconds with safe fallback between `/api/realtime` and standard `/stats` endpoints.
- **Sessions**:
  - [SessionsScreen.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/ui/screens/sessions/SessionsScreen.kt): Paginated session table displaying visitor hostname, browser/OS/device badges, location, date, and detail dialogs.
  - [SessionsViewModel.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/ui/screens/sessions/SessionsViewModel.kt): Manages session pagination, search queries, and website switching.
- **Websites**:
  - [WebsitesScreen.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/ui/screens/websites/WebsitesScreen.kt): List of user's Umami websites with domains.
  - [WebsiteDetailScreen.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/ui/screens/websites/WebsiteDetailScreen.kt): Website ID copy, name/domain fields, and HTML `<script>` tracking code container.
- **Settings**:
  - [SettingsScreen.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/ui/screens/settings/SettingsScreen.kt): Theme selection (`SYSTEM`, `LIGHT`, `DARK`), Widget update interval frequency selector, clear cache button, and Privacy Policy dialog.

### Components & Visual Extensions
- [AnalyticsChart.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/ui/components/AnalyticsChart.kt): Wraps MPAndroidChart `BarChart` in `AndroidView` with custom X-axis date formatters (`formatXAxisLabel`) and touch interactivity.
- [MetricCard.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/ui/components/MetricCard.kt): Progress indicators, favicons for Sources, vector icons for OS, emojis for Browsers/Countries, and root path `/` formatting.
- [FilterDialog.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/ui/components/FilterDialog.kt) & [FilterChipGroup.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/ui/components/FilterChipGroup.kt): Filter options dialog and dismissible filter chips.

### Utility Helpers
- [IconHelper.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/util/IconHelper.kt): Google Favicon service resolver for Sources, and mapping OS strings to bundled APK vector drawables ([ic_os_windows.xml](file:///home/dog/git/umami-android-native/app/src/main/res/drawable/ic_os_windows.xml), [ic_os_apple.xml](file:///home/dog/git/umami-android-native/app/src/main/res/drawable/ic_os_apple.xml), [ic_os_linux.xml](file:///home/dog/git/umami-android-native/app/src/main/res/drawable/ic_os_linux.xml), [ic_os_android.xml](file:///home/dog/git/umami-android-native/app/src/main/res/drawable/ic_os_android.xml)).
- [DateUtils.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/util/DateUtils.kt): ISO parsing, range calculations, relative time strings, and country ISO to Flag emoji conversion.
- [NetworkObserver.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/util/NetworkObserver.kt): Real-time network connectivity monitoring flow via Android `ConnectivityManager`.

### Data Layer (API, Database, Preferences)
- [UmamiApiService.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/data/api/UmamiApiService.kt): Ktor HTTP client handling `/api/auth/login`, `/api/websites`, `/api/websites/{id}/stats`, `/api/websites/{id}/pageviews`, `/api/websites/{id}/metrics`, `/api/websites/{id}/sessions`, `/api/websites/{id}/active`, `/api/realtime/{id}`.
- [ApiModels.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/data/api/models/ApiModels.kt): DTOs featuring custom serializers:
  - `StatValueSerializer`: Parses both raw numbers `10` or JSON objects `{"value": 10, "change": 0}`.
  - `MetricItemSerializer`: Dynamic parser handling variations in JSON keys (`x`, `url`, `name`, `page`, `domain`, `element` for labels, and `y`, `views`, `count`, `pageviews` for values).
- [UmamiRepository.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/data/repository/UmamiRepository.kt): Coordinates API fetches and Room Database caching.
- [UmamiDatabase.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/data/db/UmamiDatabase.kt), [Daos.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/data/db/dao/Daos.kt), [Entities.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/data/db/entities/Entities.kt): Local Room DB persistence.
- [SessionManager.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/data/preferences/SessionManager.kt): Persistent storage for Server URL, JWT Token, User ID, Theme mode (`SYSTEM`, `LIGHT`, `DARK`), and Selected Website ID.

### Home Screen App Widget
- [UmamiStatsWidgetProvider.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/widget/UmamiStatsWidgetProvider.kt): AppWidgetProvider managing 4x2 home screen widgets.
- [WidgetConfigActivity.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/widget/WidgetConfigActivity.kt): Configuration screen allowing users to pick website and duration for the widget.
- [WidgetUpdateWorker.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/widget/WidgetUpdateWorker.kt): Background WorkManager worker performing periodic widget background sync.
- [WidgetPreferences.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/widget/WidgetPreferences.kt): Stores per-widget preferences and update interval frequency (default: 2 hours).

---

## 3. Critical Serializer Details

Umami Server APIs return different JSON payload structures depending on whether the server is running Umami v1, Umami v2 self-hosted, or Umami Cloud. Two custom `KSerializer` implementations in [ApiModels.kt](file:///home/dog/git/umami-android-native/app/src/main/java/com/blackpiratex/umami/data/api/models/ApiModels.kt) handle this seamlessly:

### `StatValueSerializer`
- **Problem**: Some Umami endpoints return stats as raw numbers (`"pageviews": 150`), while others return JSON objects (`"pageviews": {"value": 150, "change": 12}`).
- **Solution**: Evaluates `JsonElement` type at runtime. If primitive, maps directly to `StatValue(value, 0)`. If object, extracts `"value"` and `"change"`.

### `MetricItemSerializer`
- **Problem**: Varying endpoints use different field names (`x`, `url`, `name`, `page`, `domain`, `element`) for string labels and (`y`, `views`, `count`, `pageviews`) for numeric values.
- **Solution**: Iterates fallback keys sequentially to populate `MetricItemDto(x, y)`.

---

## 4. Building & Release Instructions

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Signing
Release builds are signed via properties defined in `keystore.properties` (gitignored).

Example `keystore.properties`:
```properties
storeFile=/path/to/myapp-release.jks
storePassword=YOUR_PASSWORD
keyAlias=myapp
keyPassword=YOUR_PASSWORD
```

Commands to generate signed release artifacts:
```bash
./gradlew assembleRelease   # Generates app/build/outputs/apk/release/app-release.apk
./gradlew bundleRelease     # Generates app/build/outputs/bundle/release/app-release.aab
```

---

## 5. Summary of Recent Architectural Decisions & Fixes
1. **Navigation Transitions**: Replaced standard fade transitions with `slideInHorizontally` + `slideOutHorizontally` combined with `saveState = true` / `restoreState = true` to maintain 60 FPS transitions without screen re-instantiation.
2. **Chart Renderer**: Replaced custom manual canvas chart with `MPAndroidChart` inside `AndroidView` with defensive bounds checking against zero dataset exceptions.
3. **Icons & Favicons**: OS logos are bundled natively in `res/drawable/` (`ic_os_windows`, `ic_os_apple`, `ic_os_linux`, `ic_os_android`) to avoid third-party network hotlinking. Referrer sources fetch domain favicons via `https://www.google.com/s2/favicons?domain=...`.
4. **Theme Management**: `UmamiTheme` in `Theme.kt` performs case-insensitive uppercase matching (`DARK`, `LIGHT`, `SYSTEM`) to dynamically re-theme `MainActivity` instantly upon user selection in Settings.
