package com.example.moon.data.manager

import android.content.Context
import androidx.work.*
import com.example.moon.domain.manager.WallpaperManager
import com.example.moon.worker.WallpaperWorker
import java.util.concurrent.TimeUnit

class AndroidWallpaperManager(private val context: Context) : WallpaperManager {

    private val workManager = WorkManager.getInstance(context)
    private val prefs = context.getSharedPreferences("wallpaper_settings", Context.MODE_PRIVATE)

    override fun scheduleDailyUpdate(enabled: Boolean) {
        prefs.edit().putBoolean("daily_update", enabled).apply()
        
        if (enabled) {
            val request = PeriodicWorkRequestBuilder<WallpaperWorker>(6, TimeUnit.HOURS) // Every 6 hours is better for moon updates
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()

            workManager.enqueueUniquePeriodicWork(
                "moon_wallpaper_update",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        } else {
            workManager.cancelUniqueWork("moon_wallpaper_update")
        }
    }

    override fun isUpdateScheduled(): Boolean {
        return prefs.getBoolean("daily_update", false)
    }

    override suspend fun updateNow() {
        val request = OneTimeWorkRequestBuilder<WallpaperWorker>().build()
        workManager.enqueue(request)
    }
}
