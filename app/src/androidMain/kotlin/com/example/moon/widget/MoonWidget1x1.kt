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
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.moon.MainActivity
import com.example.moon.core.data.provider.MoonDataProviderImpl
import com.example.moon.core.data.repository.AstronomyRepositoryImpl
import com.example.moon.core.data.repository.LocationRepositoryImpl
import com.example.moon.core.domain.model.LocationData
import com.example.moon.util.MoonBitmapRenderer
import com.google.android.gms.location.LocationServices
import kotlin.math.roundToInt

class MoonWidget1x1 : GlanceAppWidget() {

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
                    val heightPx = (size.height.value * density).toInt()

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
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    if (moonBitmap != null) {
                        Image(
                            provider = ImageProvider(moonBitmap),
                            contentDescription = "Current Moon Phase (1x1)",
                            modifier = GlanceModifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        moonData?.let { data ->
                            Box(
                                modifier = GlanceModifier
                                    .fillMaxSize()
                                    .padding(bottom = 2.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Text(
                                    text = "${(data.illumination * 100).roundToInt()}%",
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onSurface,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "...",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

class MoonWidget1x1Receiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MoonWidget1x1()
}
