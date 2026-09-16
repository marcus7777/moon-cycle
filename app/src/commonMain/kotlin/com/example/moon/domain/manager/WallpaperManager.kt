package com.example.moon.domain.manager

interface WallpaperManager {
    fun scheduleDailyUpdate(enabled: Boolean)
    fun isUpdateScheduled(): Boolean
    suspend fun updateNow()
}
