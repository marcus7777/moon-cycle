package com.example.moon.core.domain.model

import kotlinx.datetime.LocalDateTime

data class LunarEvent(
    val type: EventType,
    val dateTime: LocalDateTime,
    val isSuperMoon: Boolean = false,
    val isMicroMoon: Boolean = false,
    val moonData: MoonData? = null
)

enum class EventType {
    NEW_MOON,
    FIRST_QUARTER,
    FULL_MOON,
    LAST_QUARTER,
    PERIGEE, 
    APOGEE
}

fun formatEventName(event: LunarEvent): String {
    val base = when (event.type) {
        EventType.NEW_MOON -> "Dark Moon"
        EventType.FIRST_QUARTER -> "First Quarter"
        EventType.FULL_MOON -> "Full Moon"
        EventType.LAST_QUARTER -> "Last Quarter"
        EventType.PERIGEE -> "Perigee"
        EventType.APOGEE -> "Apogee"
    }
    
    return buildString {
        append(base)
        if (event.isSuperMoon) append(" (Supermoon)")
        if (event.isMicroMoon) append(" (Micromoon)")
    }
}
