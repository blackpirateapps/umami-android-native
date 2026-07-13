package com.umami.analytics.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umami.analytics.data.api.models.ChartPointDto

@Composable
fun AnalyticsChart(
    pageviews: List<ChartPointDto>,
    sessions: List<ChartPointDto>,
    modifier: Modifier = Modifier
) {
    val pvColor = MaterialTheme.colorScheme.primary
    val sessColor = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
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
                    text = "Views & Visitors",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(pvColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Views",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(sessColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Visitors",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Bar Popup / Tooltip Card
            selectedIndex?.let { idx ->
                val pvItem = pageviews.getOrNull(idx)
                val sessItem = sessions.getOrNull(idx)
                if (pvItem != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pvItem.x,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Row {
                                Text(
                                    text = "Views: ${pvItem.y}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Visitors: ${sessItem?.y ?: 0}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            if (pageviews.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data for selected period",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                val maxPv = pageviews.maxOfOrNull { it.y } ?: 1L
                val maxSess = sessions.maxOfOrNull { it.y } ?: 1L
                val maxValue = maxOf(maxPv, maxSess, 5L)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    // Y-Axis Labels Column
                    Column(
                        modifier = Modifier
                            .height(150.dp)
                            .padding(end = 6.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "$maxValue",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${maxValue / 2}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "0",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Canvas Chart and X-Axis Container
                    Column(modifier = Modifier.weight(1f)) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .pointerInput(pageviews) {
                                    detectTapGestures { offset ->
                                        val width = size.width
                                        val itemCount = pageviews.size
                                        val stepX = width / maxOf(itemCount, 1)
                                        val tappedIndex = (offset.x / stepX).toInt().coerceIn(0, itemCount - 1)
                                        selectedIndex = tappedIndex
                                    }
                                }
                        ) {
                            val width = size.width
                            val height = size.height

                            // Grid lines
                            val gridLines = 3
                            for (i in 0..gridLines) {
                                val y = height - (height / gridLines) * i
                                drawLine(
                                    color = gridColor,
                                    start = Offset(0f, y),
                                    end = Offset(width, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            val itemCount = pageviews.size
                            val stepX = width / maxOf(itemCount, 1)
                            val barWidth = (stepX * 0.35f).coerceAtMost(20.dp.toPx())

                            pageviews.forEachIndexed { index, pv ->
                                val xCenter = stepX * index + stepX / 2f
                                val pvHeight = (pv.y.toFloat() / maxValue) * height
                                val sess = sessions.getOrNull(index)
                                val sessHeight = ((sess?.y ?: 0L).toFloat() / maxValue) * height

                                val isSelected = selectedIndex == index

                                // Views Bar
                                drawRoundRect(
                                    color = if (isSelected) pvColor else pvColor.copy(alpha = 0.85f),
                                    topLeft = Offset(xCenter - barWidth, height - pvHeight),
                                    size = Size(barWidth, pvHeight),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )

                                // Visitors Bar
                                drawRoundRect(
                                    color = if (isSelected) sessColor else sessColor.copy(alpha = 0.85f),
                                    topLeft = Offset(xCenter + 2.dp.toPx(), height - sessHeight),
                                    size = Size(barWidth, sessHeight),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // X-Axis Labels Row (Sampled timestamps)
                        val sampleStep = maxOf(1, pageviews.size / 5)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            pageviews.forEachIndexed { idx, item ->
                                if (idx % sampleStep == 0 || idx == pageviews.size - 1) {
                                    val labelStr = item.x.split(" ").lastOrNull() ?: item.x
                                    Text(
                                        text = labelStr.take(5),
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
