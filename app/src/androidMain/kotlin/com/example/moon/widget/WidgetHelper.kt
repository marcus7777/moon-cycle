package com.example.moon.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.glance.LocalSize
import com.example.moon.core.data.provider.MoonDataProviderImpl
import com.example.moon.core.data.repository.AstronomyRepositoryImpl
import com.example.moon.core.data.repository.LocationRepositoryImpl
import com.example.moon.core.domain.model.LocationData
import com.example.moon.core.domain.model.MoonData
import com.example.moon.util.MoonBitmapRenderer
import com.google.android.gms.location.LocationServices

data class WidgetData(
    val moonData: MoonData?,
    val location: LocationData
)

object WidgetHelper {

    suspend fun loadWidgetData(context: Context): WidgetData {
        val locationRepository = LocationRepositoryImpl(
            context,
            LocationServices.getFusedLocationProviderClient(context)
        )
        val astronomyRepository = AstronomyRepositoryImpl(com.example.moon.core.data.storage.AndroidFullMoonOffsetStorage(context))
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

        return WidgetData(moonData, location)
    }

    @Composable
    fun rememberMoonBitmap(
        context: Context,
        widgetData: WidgetData,
        heightOffsetDp: Float = 0f
    ): Bitmap? {
        val size = LocalSize.current
        val density = context.resources.displayMetrics.density
        val moonData = widgetData.moonData

        return remember(size, moonData) {
            if (moonData != null) {
                val widthPx = (size.width.value * density).toInt()
                val availableHeightDp = (size.height.value - heightOffsetDp).coerceAtLeast(40f)
                val heightPx = (availableHeightDp * density).toInt()

                if (widthPx > 0 && heightPx > 0) {
                    MoonBitmapRenderer.renderMoon(
                        context = context,
                        moonData = moonData,
                        location = widgetData.location,
                        width = widthPx,
                        height = heightPx
                    )
                } else null
            } else null
        }
    }
}
