package com.umami.analytics.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.umami.analytics.data.api.models.MetricItemDto
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Composable
fun WorldMapComposable(
    countries: List<MetricItemDto>,
    modifier: Modifier = Modifier
) {
    val countryDataJson = buildJsonObject {
        countries.forEach { item ->
            val code = item.x ?: ""
            put(code.uppercase(), item.y)
        }
    }.toString()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Visitor Locations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    }
                },
                update = { webView ->
                    val htmlContent = generateWorldMapHtml(countryDataJson)
                    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                }
            )
        }
    }
}

private fun generateWorldMapHtml(countryJson: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    margin: 0;
                    padding: 0;
                    background-color: #0F172A;
                    color: #FFFFFF;
                    font-family: system-ui, -apple-system, sans-serif;
                    overflow: hidden;
                }
                #map-container {
                    width: 100vw;
                    height: 100vh;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    position: relative;
                }
                svg {
                    width: 100%;
                    height: 100%;
                }
                path {
                    fill: #0F2A4A;
                    stroke: #0284C7;
                    stroke-width: 0.8;
                    transition: fill 0.2s, stroke 0.2s;
                }
                path.active {
                    fill: #38BDF8;
                }
                path:hover, path:active {
                    fill: #6366F1;
                    stroke: #E0E7FF;
                }
                .tooltip {
                    position: absolute;
                    background-color: rgba(15, 23, 42, 0.95);
                    border: 1px solid #38BDF8;
                    color: #FFFFFF;
                    padding: 6px 12px;
                    border-radius: 6px;
                    font-size: 12px;
                    font-weight: bold;
                    pointer-events: none;
                    display: none;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.5);
                    z-index: 10;
                }
            </style>
        </head>
        <body>
            <div id="map-container">
                <div id="tooltip" class="tooltip"></div>
                <svg viewBox="0 0 1000 500">
                    <!-- Simplistic High Quality Vector World Paths -->
                    <g id="countries">
                        <path id="RU" d="M 600,100 L 900,100 L 920,200 L 650,220 Z" data-name="Russia" />
                        <path id="IN" d="M 680,220 L 730,220 L 710,290 L 680,260 Z" data-name="India" />
                        <path id="US" d="M 150,150 L 300,150 L 300,240 L 150,240 Z" data-name="United States" />
                        <path id="CA" d="M 150,60 L 320,60 L 300,145 L 140,145 Z" data-name="Canada" />
                        <path id="CN" d="M 720,180 L 840,180 L 820,260 L 710,240 Z" data-name="China" />
                        <path id="BR" d="M 280,270 L 380,270 L 340,390 L 270,330 Z" data-name="Brazil" />
                        <path id="AU" d="M 800,320 L 920,320 L 900,420 L 790,400 Z" data-name="Australia" />
                        <path id="SG" d="M 735,295 L 745,295 L 745,305 L 735,305 Z" data-name="Singapore" />
                        <path id="JP" d="M 850,160 L 880,160 L 870,220 L 840,210 Z" data-name="Japan" />
                        <path id="DE" d="M 500,140 L 530,140 L 530,175 L 500,175 Z" data-name="Germany" />
                        <path id="FR" d="M 465,150 L 495,150 L 490,190 L 460,185 Z" data-name="France" />
                        <path id="ES" d="M 440,180 L 470,180 L 465,215 L 435,210 Z" data-name="Spain" />
                        <path id="GB" d="M 460,110 L 485,110 L 480,140 L 455,135 Z" data-name="United Kingdom" />
                    </g>
                </svg>
            </div>
            <script>
                const countryData = $countryJson;
                const tooltip = document.getElementById('tooltip');
                const paths = document.querySelectorAll('path');

                paths.forEach(p => {
                    const id = p.getAttribute('id');
                    const name = p.getAttribute('data-name') || id;
                    const count = countryData[id] || 0;

                    if (count > 0) {
                        p.classList.add('active');
                    }

                    p.addEventListener('click', (e) => {
                        tooltip.style.display = 'block';
                        tooltip.innerText = name + ': ' + count + ' visitors';
                        tooltip.style.left = (e.clientX - 40) + 'px';
                        tooltip.style.top = (e.clientY - 40) + 'px';
                    });
                });
            </script>
        </body>
        </html>
    """.trimIndent()
}
