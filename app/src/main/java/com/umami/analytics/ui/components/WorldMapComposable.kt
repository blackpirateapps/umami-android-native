package com.umami.analytics.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umami.analytics.data.api.models.MetricItemDto
import com.umami.analytics.util.DateUtils

private data class CountryRegion(
    val code: String,
    val name: String,
    val centerPoint: Offset, // Normalized [0..1]
    val bounds: Pair<Offset, Offset> // Normalized bounds (topLeft, bottomRight)
)

private val WORLD_REGIONS = listOf(
    CountryRegion("US", "United States", Offset(0.22f, 0.35f), Pair(Offset(0.12f, 0.25f), Offset(0.30f, 0.45f))),
    CountryRegion("CA", "Canada", Offset(0.20f, 0.20f), Pair(Offset(0.10f, 0.10f), Offset(0.32f, 0.25f))),
    CountryRegion("MX", "Mexico", Offset(0.20f, 0.48f), Pair(Offset(0.15f, 0.42f), Offset(0.25f, 0.54f))),
    CountryRegion("BR", "Brazil", Offset(0.35f, 0.65f), Pair(Offset(0.28f, 0.52f), Offset(0.40f, 0.78f))),
    CountryRegion("GB", "United Kingdom", Offset(0.47f, 0.24f), Pair(Offset(0.45f, 0.20f), Offset(0.49f, 0.28f))),
    CountryRegion("FR", "France", Offset(0.49f, 0.29f), Pair(Offset(0.47f, 0.26f), Offset(0.51f, 0.32f))),
    CountryRegion("DE", "Germany", Offset(0.52f, 0.26f), Pair(Offset(0.50f, 0.23f), Offset(0.54f, 0.29f))),
    CountryRegion("ES", "Spain", Offset(0.47f, 0.34f), Pair(Offset(0.44f, 0.31f), Offset(0.49f, 0.37f))),
    CountryRegion("IT", "Italy", Offset(0.53f, 0.33f), Pair(Offset(0.51f, 0.30f), Offset(0.55f, 0.36f))),
    CountryRegion("RU", "Russia", Offset(0.72f, 0.22f), Pair(Offset(0.58f, 0.10f), Offset(0.92f, 0.36f))),
    CountryRegion("IN", "India", Offset(0.71f, 0.45f), Pair(Offset(0.66f, 0.38f), Offset(0.75f, 0.54f))),
    CountryRegion("CN", "China", Offset(0.78f, 0.38f), Pair(Offset(0.71f, 0.30f), Offset(0.85f, 0.46f))),
    CountryRegion("JP", "Japan", Offset(0.88f, 0.36f), Pair(Offset(0.85f, 0.32f), Offset(0.91f, 0.40f))),
    CountryRegion("SG", "Singapore", Offset(0.77f, 0.57f), Pair(Offset(0.76f, 0.55f), Offset(0.79f, 0.59f))),
    CountryRegion("AU", "Australia", Offset(0.85f, 0.72f), Pair(Offset(0.78f, 0.62f), Offset(0.92f, 0.82f))),
    CountryRegion("ZA", "South Africa", Offset(0.54f, 0.76f), Pair(Offset(0.50f, 0.70f), Offset(0.58f, 0.82f)))
)

@Composable
fun WorldMapComposable(
    countries: List<MetricItemDto>,
    modifier: Modifier = Modifier
) {
    val countryMap = remember(countries) {
        countries.associate { (it.x ?: "").uppercase() to it.y }
    }

    var selectedRegion by remember { mutableStateOf<CountryRegion?>(null) }
    var selectedVisitorCount by remember { mutableStateOf(0L) }

    val outlineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    val activeFillColor = MaterialTheme.colorScheme.secondary
    val inactiveDotColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Visitor Map",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${countries.size} Countries Active",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Tooltip Card matching Screenshot 1 (e.g. "Russia: 0 visitors")
            selectedRegion?.let { reg ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${DateUtils.getFlagEmoji(reg.code)} ${reg.name}: ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$selectedVisitorCount visitors",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // High-Performance Vector Canvas World Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(countryMap) {
                            detectTapGestures { offset ->
                                val w = size.width
                                val h = size.height
                                val normX = offset.x / w
                                val normY = offset.y / h

                                val matched = WORLD_REGIONS.find { reg ->
                                    val (tl, br) = reg.bounds
                                    normX in tl.x..br.x && normY in tl.y..br.y
                                }

                                if (matched != null) {
                                    selectedRegion = matched
                                    selectedVisitorCount = countryMap[matched.code] ?: 0L
                                } else {
                                    // Default fallback to Russia or nearest region if tapped elsewhere
                                    val nearest = WORLD_REGIONS.minByOrNull { reg ->
                                        val dx = normX - reg.centerPoint.x
                                        val dy = normY - reg.centerPoint.y
                                        dx * dx + dy * dy
                                    }
                                    selectedRegion = nearest
                                    selectedVisitorCount = countryMap[nearest?.code] ?: 0L
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // Grid outlines for dark map design
                    val stroke = Stroke(width = 1.dp.toPx())

                    // Draw continents / country bounding regions
                    WORLD_REGIONS.forEach { reg ->
                        val (tl, br) = reg.bounds
                        val left = tl.x * w
                        val top = tl.y * h
                        val width = (br.x - tl.x) * w
                        val height = (br.y - tl.y) * h

                        val count = countryMap[reg.code] ?: 0L
                        val isActive = count > 0 || selectedRegion?.code == reg.code

                        // Draw region boundary rect with rounded corners
                        drawRoundRect(
                            color = if (isActive) activeFillColor.copy(alpha = 0.25f) else Color(0xFF1E293B).copy(alpha = 0.5f),
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )

                        drawRoundRect(
                            color = if (isActive) activeFillColor else outlineColor,
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                            style = stroke
                        )

                        // Center indicator dot
                        val cx = reg.centerPoint.x * w
                        val cy = reg.centerPoint.y * h
                        drawCircle(
                            color = if (isActive) activeFillColor else inactiveDotColor,
                            radius = if (isActive) 6.dp.toPx() else 3.dp.toPx(),
                            center = Offset(cx, cy)
                        )
                    }
                }
            }
        }
    }
}
