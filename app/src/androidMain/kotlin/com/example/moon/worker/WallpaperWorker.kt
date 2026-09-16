package com.example.moon.worker

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.moon.data.repository.AstronomyRepositoryImpl
import com.example.moon.data.repository.LocationRepositoryImpl
import com.example.moon.util.MoonVectorEngine
import com.google.android.gms.location.LocationServices
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class WallpaperWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
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

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fill background with black
        canvas.drawColor(android.graphics.Color.BLACK)

        val drawScope = CanvasDrawScope()
        val composeCanvas = androidx.compose.ui.graphics.Canvas(canvas)
        val size = androidx.compose.ui.geometry.Size(width.toFloat(), height.toFloat())
        
        drawScope.draw(
            density = Density(context),
            layoutDirection = LayoutDirection.Ltr,
            canvas = composeCanvas,
            size = size
        ) {
            MoonVectorEngine.drawMoon(this, moonData, location)
        }

        return try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            wallpaperManager.setBitmap(bitmap)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        } finally {
            bitmap.recycle()
        }
    }
}
