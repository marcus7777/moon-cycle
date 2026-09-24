package com.example.moon.core.data.storage

import android.content.Context
import com.example.moon.core.domain.repository.FullMoonOffsetStorage

class AndroidFullMoonOffsetStorage(context: Context) : FullMoonOffsetStorage {
    private val prefs = context.getSharedPreferences("astronomy_prefs", Context.MODE_PRIVATE)

    override fun getOffsetMinutes(): Long {
        return prefs.getLong("full_moon_offset_minutes", 0L)
    }

    override fun saveOffsetMinutes(minutes: Long) {
        prefs.edit().putLong("full_moon_offset_minutes", minutes).apply()
    }
}
