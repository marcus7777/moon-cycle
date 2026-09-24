package com.example.moon.core.domain.model

import kotlinx.datetime.LocalDateTime

data class MoonData(
    val phase: MoonPhase,
    val illumination: Double, // 0.0 to 1.0
    val age: Double, // Days into cycle
    val riseTime: LocalDateTime?,
    val setTime: LocalDateTime?,
    val altitude: Double? = null,
    val azimuth: Double? = null,
    val parallacticAngle: Double? = null,
    val nextEvent: LunarEvent? = null,
    val fullMoonOffsetMinutes: Long = 0L
)
