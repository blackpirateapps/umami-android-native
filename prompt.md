This is an empty repo. I want you to write codes for a umami analytics app for android. You have to use android native components for making that app. Gradle and android sdk tools are installed here. After making all the files you have to commit and push the changes. 

Here is the feature list that I want. 
- Login to custom umami instance. saves login session on subsequent app opening. 
- Offline first approach. All the data is cached locally so when offline the app gives you are offline notice at the top of the screen but still lets the user browse the app, 
- A sidebar that contains following options. 
    - Overview: this page contains the visitor and views graph (you should use external libraries wherever its necessary to keep the codebase clean), Then shows pages and their visiors. then it shows sources, then Environment (browser, os and devices). There should be options to filter by page, country, region, referrer. It should be possible to add multiple filters. There hsould be option to choose the duration to show the stats for. The durations are, last 24 hours, this week, last 7 days, last 30 days, this month, this year, last 6 months, last 12 months, and all time. When the user applies any of the filter to the page it updates all the data for all the fields.  
    - Sessions: this page should look identical to the umami web app. I have added the screenshots in this directory. Check it. If you cannot find the images let me know and do not imagie it yourself. 

    - At the top of the sidebar there should be option to switch between different websites that are available in the umami account. 
    - at the bottom of the sidebar there should be an option for settings, and logout. 

The UI Stack
Language: Kotlin  
Declarative Framework: Jetpack Compose
Design System: androidx.compose.material3:material3
Key Material You Implementations
To make the app feel like a true system-level Android application, you'll need to utilize three specific features within the Material 3 library:
Dynamic Color: This is the hallmark of Material You. Instead of entirely hardcoding your own primary and secondary colors, you use dynamicLightColorScheme() and dynamicDarkColorScheme(). This allows your app to pull the color palette directly from the user's system wallpaper (supported on Android 12 and up). 
M3 Expressive Theming: To match the latest Android 16/17 aesthetic, you will want to leverage the newer "Expressive" styling updates rolling out in Compose. This includes a more emphasized typography scale, updated component shapes, and fluid motion systems that build upon the initial Material 3 release. 
Edge-to-Edge Layout: Modern native Google apps draw their content seamlessly behind transparent status and navigation bars. You achieve this by calling enableEdgeToEdge() in your MainActivity and handling the necessary padding via WindowInsets in your composables so content isn't obscured.


To get the full modern Material 3 and M3 Expressive capabilities, you'll need the latest `material3` Compose artifacts, along with the correct Activity Compose library to handle the system-level edge-to-edge styling.

Here are the specific dependencies you should include in your app's `build.gradle.kts` file:

## Core Material 3 & Compose

These are the essential libraries for the UI components, dynamic color, and the new Expressive typography and motion systems.

```kotlin
dependencies {
    // The main Material 3 library (includes M3 Expressive components and Dynamic Color)
    implementation("androidx.compose.material3:material3:1.4.0") // Or the latest 1.5.0-alpha if you want bleeding edge
    
    // Core Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    
    // Tooling for Android Studio Previews
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
}

```

## System Integration (Edge-to-Edge)

To draw your app behind the transparent status bar and navigation pill (a requirement for the native Android 16/17 look), you need the Activity Compose library. You no longer need third-party libraries like Accompanist for this.

```kotlin
dependencies {
    // Provides the enableEdgeToEdge() function and Compose Activity integration
    implementation("androidx.activity:activity-compose:1.9.0") // Or latest stable
}

```

## Adaptive Layouts

Google's modern apps adapt fluidly between phones, foldables, and tablets. to match this behavior (like automatically switching from a bottom navigation bar to a side navigation rail on larger screens), include the M3 Adaptive suite:

```kotlin
dependencies {
    // Material 3 Adaptive Navigation Suite
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.5.0-alpha23")
    
    // Core Adaptive building blocks
    implementation("androidx.compose.material3.adaptive:adaptive:1.3.0-rc01")
    implementation("androidx.compose.material3.adaptive:adaptive-layout:1.3.0-rc01")
}

```


To nail that native Android 16 and 17 feel, the UI framework is only half the battle. The rest comes down to system integration and adhering to the newest Material 3 guidelines.

Here is what you need to keep in mind to make the app feel like it belongs on a modern Pixel device:

## 1. M3 Expressive Motion and Typography

As of mid-2026, Google's native apps are transitioning to **M3 Expressive** (available in Compose 1.5.0 and later). This isn't just a color update; it fundamentally changes how the app moves and reads.

* **Motion Physics:** The new animation system uses a spring-based physics model rather than traditional easing curves. This makes interactions (like expanding a card or pulling a list) feel bouncier and more responsive.
* **Emphasized Type Scale:** Android 16 apps use a higher-contrast typography scale. Headlines are bolder and larger, while body text remains highly legible. Ensure you are using the updated `MaterialTheme.typography` tokens rather than hardcoding font sizes.

## 2. Predictive Back Navigation

Modern Android relies heavily on predictive back animations — where the user can partially swipe back to "peek" at the previous screen or the home screen before committing to the action.

* You must opt-in by setting `android:enableOnBackInvokedCallback="true"` in your `AndroidManifest.xml`.
* If you use Jetpack Navigation for Compose, this behavior is largely handled for you out of the box, provided you don't intercept system back presses unnecessarily with custom `BackHandler` logic.

## 3. Strict Color Token Pairing

When using Dynamic Color, you surrender exact hex codes to the system. A common mistake is mixing fixed colors with dynamic ones, resulting in unreadable text when the user changes their wallpaper.

* **Always pair tokens:** If a card's background uses `MaterialTheme.colorScheme.primaryContainer`, the text or icon inside it *must* use `MaterialTheme.colorScheme.onPrimaryContainer`. This guarantees accessible contrast ratios regardless of the system palette.

## 4. Themed App Icons

A Material You app isn't complete until it adapts to the user's home screen.

* You must provide a **monochromatic app icon** alongside your adaptive icon. When the user toggles "Themed icons" in their Android launcher settings, your app icon will shed its brand colors and adopt the system's dynamic color palette.

## 5. The System Splash Screen API

Do not build a custom splash screen activity. Native apps use the `androidx.core:core-splashscreen` library.

* This hooks directly into the Android OS launch sequence, ensuring the transition from the home screen into your app is a seamless zoom-and-fade animation that matches system-level behavior perfectly.


