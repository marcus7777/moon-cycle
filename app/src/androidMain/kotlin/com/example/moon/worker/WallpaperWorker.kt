package com.example.moon.worker

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.moon.core.data.repository.AstronomyRepositoryImpl
import com.example.moon.core.data.repository.LocationRepositoryImpl
import com.example.moon.core.ui.util.MoonVectorEngine
import com.google.android.gms.location.LocationServices
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class WallpaperWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Recovery: ensure MainActivity is enabled
        try {
            context.packageManager.setComponentEnabledSetting(
                ComponentName(context.packageName, "com.example.moon.MainActivity"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) {
            // Ignore
        }

        val locationRepository = LocationRepositoryImpl(
            context,
            LocationServices.getFusedLocationProviderClient(context)
        )
        val astronomyRepository = AstronomyRepositoryImpl()

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val location = locationRepository.getCurrentLocation()
        val moonData = astronomyRepository.getMoonData(now, location)

        val metrics = context.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        val bitmap = com.example.moon.util.MoonBitmapRenderer.renderMoon(
            context = context,
            moonData = moonData,
            location = location,
            width = width,
            height = height
        )

        return try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            wallpaperManager.setBitmap(bitmap)
            
            // Also update the home screen widget
            try {
                com.example.moon.widget.MoonWidget().updateAll(context)
            } catch (e: Exception) {
                // Ignore widget update errors
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        } finally {
            bitmap.recycle()
        }
    }
}
