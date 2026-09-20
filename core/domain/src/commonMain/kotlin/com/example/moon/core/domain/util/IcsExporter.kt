package com.example.moon.core.domain.util

import com.example.moon.core.domain.model.LunarEvent
import com.example.moon.core.domain.model.LocationData
import com.example.moon.core.domain.model.formatEventName
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

object IcsExporter {
    fun generateIcs(events: List<LunarEvent>, location: LocationData? = null): String {
        return buildString {
            append("BEGIN:VCALENDAR\n")
            append("VERSION:2.0\n")
            append("PRODID:-//Moon Cycle//Lunar Events//EN\n")
            append("CALSCALE:GREGORIAN\n")
            append("METHOD:PUBLISH\n")

            events.forEach { event ->
                append("BEGIN:VEVENT\n")
                val timestamp = formatIcsDateTime(event.dateTime)
                append("DTSTART:$timestamp\n")
                // End date is required by some parsers, set to 1 hour later
                val endDateTime = try {
                    val instant = event.dateTime.toInstant(TimeZone.UTC)
                    val endInstant = kotlinx.datetime.Instant.fromEpochMilliseconds(instant.toEpochMilliseconds() + 3600000)
                    endInstant.toLocalDateTime(TimeZone.UTC)
                } catch (e: Exception) {
                    event.dateTime
                }
                append("DTEND:${formatIcsDateTime(endDateTime)}\n")
                append("SUMMARY:${formatEventName(event)}\n")
                append("DESCRIPTION:Lunar event calculated by Moon Cycle App\n")
                
                if (location != null) {
                    append("GEO:${location.latitude};${location.longitude}\n")
                }
                
                append("STATUS:CONFIRMED\n")
                append("TRANSP:OPAQUE\n")
                append("END:VEVENT\n")
            }

            append("END:VCALENDAR")
        }
    }

    private fun formatIcsDateTime(dateTime: LocalDateTime): String {
        // Format: YYYYMMDDTHHMMSSZ
        return dateTime.run {
            "${year.toString().padStart(4, '0')}${monthNumber.toString().padStart(2, '0')}${dayOfMonth.toString().padStart(2, '0')}T" +
            "${hour.toString().padStart(2, '0')}${minute.toString().padStart(2, '0')}${second.toString().padStart(2, '0')}Z"
        }
    }
}
