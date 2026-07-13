package com.umami.analytics.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umami.analytics.data.preferences.SessionManager
import com.umami.analytics.widget.WidgetPreferences
import com.umami.analytics.widget.WidgetSyncScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    sessionManager: SessionManager,
    onOpenDrawer: () -> Unit,
    onThemeModeChange: (String) -> Unit,
    onClearCache: () -> Unit
) {
    val context = LocalContext.current
    val widgetPrefs = remember { WidgetPreferences(context) }

    val serverUrl = sessionManager.getServerUrl() ?: "Not logged in"
    val username = sessionManager.getUsername() ?: "Guest"
    var currentTheme by remember { mutableStateOf(sessionManager.getThemeMode()) }
    var currentWidgetInterval by remember { mutableStateOf(widgetPrefs.getUpdateIntervalHours()) }
    var intervalDropdownExpanded by remember { mutableStateOf(false) }

    val intervalOptions = listOf(
        1 to "1 Hour",
        2 to "2 Hours (Default)",
        4 to "4 Hours",
        8 to "8 Hours",
        12 to "12 Hours",
        24 to "24 Hours"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Appearance & Theme Mode Section
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("App Theme", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Choose your preferred theme", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentTheme == "system",
                            onClick = {
                                currentTheme = "system"
                                sessionManager.saveThemeMode("system")
                                onThemeModeChange("system")
                            },
                            label = { Text("System") },
                            leadingIcon = { Icon(Icons.Default.SettingsSuggest, contentDescription = null) },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = currentTheme == "light",
                            onClick = {
                                currentTheme = "light"
                                sessionManager.saveThemeMode("light")
                                onThemeModeChange("light")
                            },
                            label = { Text("Light") },
                            leadingIcon = { Icon(Icons.Default.Brightness7, contentDescription = null) },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = currentTheme == "dark",
                            onClick = {
                                currentTheme = "dark"
                                sessionManager.saveThemeMode("dark")
                                onThemeModeChange("dark")
                            },
                            label = { Text("Dark") },
                            leadingIcon = { Icon(Icons.Default.Brightness4, contentDescription = null) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Widget Update Frequency Section
            Text(
                text = "Widget Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Update Frequency", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Background sync rate for home screen widgets", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { intervalDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = intervalOptions.find { it.first == currentWidgetInterval }?.second ?: "$currentWidgetInterval Hours",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    DropdownMenu(
                        expanded = intervalDropdownExpanded,
                        onDismissRequest = { intervalDropdownExpanded = false }
                    ) {
                        intervalOptions.forEach { (hours, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    intervalDropdownExpanded = false
                                    currentWidgetInterval = hours
                                    widgetPrefs.saveUpdateIntervalHours(hours)
                                    WidgetSyncScheduler.schedulePeriodicWork(context, hours)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Account & Server Info Section
            Text(
                text = "Account & Server",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Dns, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Server URL", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(serverUrl, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Username", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(username, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Offline Cache Management
            Text(
                text = "Offline Storage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cached analytics data allows offline browsing.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onClearCache,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Cache")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear Local Cache")
                    }
                }
            }
        }
    }
}
