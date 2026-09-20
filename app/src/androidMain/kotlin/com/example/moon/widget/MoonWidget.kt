package com.example.moon.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.moon.MainActivity
import com.example.moon.core.data.repository.AstronomyRepositoryImpl
import com.example.moon.core.data.repository.LocationRepositoryImpl
import com.example.moon.core.data.provider.MoonDataProviderImpl
import com.example.moon.util.MoonBitmapRenderer
import com.google.android.gms.location.LocationServices

class MoonWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val locationRepository = LocationRepositoryImpl(
            context,
            LocationServices.getFusedLocationProviderClient(context)
        )
        val astronomyRepository = AstronomyRepositoryImpl()
        val moonDataProvider = MoonDataProviderImpl(locationRepository, astronomyRepository)

        val moonData = try {
            moonDataProvider.getMoonData()
        } catch (e: Exception) {
            null
        }
        
        val location = try {
            locationRepository.getCurrentLocation()
        } catch (e: Exception) {
            com.example.moon.core.domain.model.LocationData(51.5074, -0.1278) // Default London
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
                    modifier = GlanceModifier.fillMaxSize()
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    if (moonBitmap != null) {
                        Image(
                            provider = ImageProvider(moonBitmap),
                            contentDescription = "Current Moon Phase",
                            modifier = GlanceModifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            text = "Updating...",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}
