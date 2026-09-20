package com.example.moon.data.store

import android.content.Context
import com.example.moon.core.domain.model.MoonData
import com.example.moon.core.domain.model.MoonPhase

/**
 * A simple storage to persist the last seen moon state for a splash-free launch.
 */
class MoonStateStore(context: Context) {
    private val prefs = context.getSharedPreferences("moon_state", Context.MODE_PRIVATE)

    fun saveMoonData(data: MoonData) {
        prefs.edit()
            .putString("phase", data.phase.name)
            .putFloat("illumination", data.illumination.toFloat())
            .putFloat("parallacticAngle", data.parallacticAngle?.toFloat() ?: 0f)
            .apply()
    }

    fun getMoonData(): MoonData? {
        val phaseName = prefs.getString("phase", null) ?: return null
        val phase = try { MoonPhase.valueOf(phaseName) } catch (e: Exception) { MoonPhase.NEW }
        
        return MoonData(
            phase = phase,
            illumination = prefs.getFloat("illumination", 0f).toDouble(),
            age = 0.0,
            riseTime = null,
            setTime = null,
            parallacticAngle = prefs.getFloat("parallacticAngle", 0f).toDouble()
        )
    }
}
