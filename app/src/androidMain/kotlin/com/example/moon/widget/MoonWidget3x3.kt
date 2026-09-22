package com.example.moon.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.example.moon.MainActivity
import com.example.moon.core.data.provider.MoonDataProviderImpl
import com.example.moon.core.data.repository.AstronomyRepositoryImpl
import com.example.moon.core.data.repository.LocationRepositoryImpl
import com.example.moon.core.domain.model.LocationData
import com.example.moon.core.domain.model.LunarEvent
import com.example.moon.core.domain.model.formatEventName
import com.example.moon.util.MoonBitmapRenderer
import com.google.android.gms.location.LocationServices
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

class MoonWidget3x3 : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val locationRepository = LocationRepositoryImpl(
            context,
            LocationServices.getFusedLocationProviderClient(context)
        )
        val astronomyRepository = AstronomyRepositoryImpl()
        val moonDataProvider = MoonDataProviderImpl(locationRepository, astronomyRepository)

        val moonData = try {
            moonDataProvider.getMoonData()
        } catch (_: Exception) {
            null
        }

        val location = try {
            locationRepository.getCurrentLocation()
        } catch (_: Exception) {
            LocationData(51.5074, -0.1278) // Default London
        }

        provideContent {
            val size = LocalSize.current
            val density = context.resources.displayMetrics.density

            val moonBitmap = androidx.compose.runtime.remember(size, moonData) {
                if (moonData != null) {
                    val widthPx = (size.width.value * density).toInt()
                    val availableHeightDp = (size.height.value - 90).coerceAtLeast(60f)
                    val heightPx = (availableHeightDp * density).toInt()

                    if (widthPx > 0 && heightPx > 0) {
                        MoonBitmapRenderer.renderMoon(
                            context = context,
                            moonData = moonData,
                            location = location,
                            width = widthPx,
                            height = heightPx
                        )
                    } else null
                } else null
            }

            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    if (moonData != null) {
                        Column(
                            modifier = GlanceModifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Header
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp)
                            ) {
                                Text(
                                    text = moonData.phase.description,
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onSurface,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                )
                                Text(
                                    text = "${(moonData.illumination * 100).roundToInt()}% Illumination",
                                    style = TextStyle(
                                        color = GlanceTheme.colors.outline,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }

                            // Moon Graphic
                            Box(
                                modifier = GlanceModifier.defaultWeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (moonBitmap != null) {
                                    Image(
                                        provider = ImageProvider(moonBitmap),
                                        contentDescription = "Current Moon Phase (3x3)",
                                        modifier = GlanceModifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }

                            // Footer Stats
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = GlanceModifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                val riseStr = formatTime(moonData.riseTime)
                                val setStr = formatTime(moonData.setTime)
                                Text(
                                    text = "Rise $riseStr  •  Set $setStr",
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onSurface,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                )

                                getCountdownText(moonData.nextEvent)?.let { countdown ->
                                    Spacer(modifier = GlanceModifier.height(2.dp))
                                    Text(
                                        text = countdown,
                                        style = TextStyle(
                                            color = GlanceTheme.colors.outline,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Updating...",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }

    private fun formatTime(dateTime: LocalDateTime?): String {
        if (dateTime == null) return "--:--"
        val hour = dateTime.hour.toString().padStart(2, '0')
        val minute = dateTime.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }

    private fun getCountdownText(event: LunarEvent?): String? {
        if (event == null) return null
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val duration = event.dateTime.toInstant(TimeZone.currentSystemDefault()) - now.toInstant(TimeZone.currentSystemDefault())
        val days = duration.inWholeDays
        val hours = duration.inWholeHours % 24

        val eventName = formatEventName(event)
        return when {
            days > 0 -> "$days ${if (days == 1L) "day" else "days"} until $eventName"
            hours > 0 -> "$hours ${if (hours == 1L) "hour" else "hours"} until $eventName"
            else -> "$eventName is tonight"
        }
    }
}

class MoonWidget3x3Receiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MoonWidget3x3()
}
