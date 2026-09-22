package com.example.moon.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moon.core.domain.model.LocationData
import com.example.moon.core.domain.model.MoonData
import com.example.moon.core.domain.model.MoonPhase
import com.example.moon.core.domain.repository.LocationRepository
import com.example.moon.core.domain.repository.AstronomyRepository
import com.example.moon.core.domain.repository.NoteRepository
import com.example.moon.core.domain.manager.WallpaperManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class MoonDetailViewModel(
    private val locationRepository: LocationRepository,
    private val astronomyRepository: AstronomyRepository,
    private val noteRepository: NoteRepository? = null,
    private val wallpaperManager: WallpaperManager? = null
) : ViewModel() {

    private val _isWallpaperScheduled = MutableStateFlow(wallpaperManager?.isUpdateScheduled() ?: false)
    val isWallpaperScheduled: StateFlow<Boolean> = _isWallpaperScheduled.asStateFlow()

    fun toggleWallpaperSchedule(enabled: Boolean) {
        wallpaperManager?.scheduleDailyUpdate(enabled)
        _isWallpaperScheduled.value = enabled
    }

    fun updateWallpaperNow() {
        viewModelScope.launch {
            wallpaperManager?.updateNow()
        }
    }

    fun setManualLocation(latitude: Double, longitude: Double, name: String? = null) {
        locationRepository.setManualLocation(latitude, longitude, name)
    }

    fun useDeviceLocation() {
        locationRepository.clearManualLocation()
    }

    fun exportNotesJsonl(): String {
        var result = ""
        val fullNotesFlow = noteRepository?.getFullNotes()
        if (fullNotesFlow != null) {
            val currentList = (fullNotesFlow as? StateFlow)?.value ?: emptyList()
            
            val escape = { s: String -> s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") }
            result = currentList.joinToString("\n") { entry ->
                buildString {
                    append("{")
                    append("\"text\":\"").append(escape(entry.text)).append("\",")
                    append("\"calendarDay\":\"").append(escape(entry.calendarDay)).append("\",")
                    append("\"dateWritten\":\"").append(escape(entry.dateWritten)).append("\",")
                    append("\"lastUpdated\":\"").append(escape(entry.lastUpdated)).append("\"")
                    val geohash = entry.geohash
                    if (geohash != null) append(",\"geohash\":\"").append(escape(geohash)).append("\"")
                    append("}")
                }
            }
        }
        return result
    }

    fun importNotesJsonl(jsonlText: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = noteRepository?.importJsonlData(jsonlText) ?: false
            onComplete(success)
        }
    }

    fun exportNotesICal(): String {
        val fullNotesFlow = noteRepository?.getFullNotes()
        val currentList = (fullNotesFlow as? StateFlow)?.value ?: emptyList()

        return buildString {
            append("BEGIN:VCALENDAR\n")
            append("VERSION:2.0\n")
            append("PRODID:-//MoonCycle//Daily Notes//EN\n")
            
            currentList.forEach { entry ->
                val dayClean = entry.calendarDay.replace("-", "")
                append("BEGIN:VEVENT\n")
                append("UID:note-${entry.calendarDay}@com.example.moon\n")
                val summaryText = if (entry.text.length > 30) entry.text.take(27) + "..." else entry.text
                append("SUMMARY:").append(summaryText.replace("\n", " ").replace("\r", " ")).append("\n")
                append("DESCRIPTION:").append(entry.text.replace("\n", "\\n").replace("\r", "\\r")).append("\n")
                append("DTSTART;VALUE=DATE:").append(dayClean).append("\n")
                append("DTEND;VALUE=DATE:").append(dayClean).append("\n")
                val geohash = entry.geohash
                if (geohash != null) {
                    append("LOCATION:Geohash ").append(geohash).append("\n")
                }
                append("END:VEVENT\n")
            }

            append("END:VCALENDAR")
        }
    }

    fun importNotesICal(iCalText: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (noteRepository == null) {
                onComplete(false)
                return@launch
            }
            try {
                val lines = iCalText.split("\n").map { it.trim() }
                val importedJsonl = StringBuilder()
                
                var inEvent = false
                var currentSummary = ""
                var currentDesc = ""
                var currentDtStart = ""
                var currentGeohash: String? = null
                
                lines.forEach { line ->
                    when {
                        line.startsWith("BEGIN:VEVENT") -> {
                            inEvent = true
                            currentSummary = ""
                            currentDesc = ""
                            currentDtStart = ""
                            currentGeohash = null
                        }
                        line.startsWith("END:VEVENT") -> {
                            if (inEvent && currentDtStart.isNotEmpty()) {
                                val dateStr = if (currentDtStart.length >= 8) {
                                    "${currentDtStart.substring(0,4)}-${currentDtStart.substring(4,6)}-${currentDtStart.substring(6,8)}"
                                } else "2026-09-17"
                                
                                val finalText = if (currentDesc.isNotEmpty()) currentDesc else currentSummary
                                val nowStr = "2026-09-17T13:54:33"
                                
                                val escape = { s: String -> s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") }
                                importedJsonl.append("{")
                                importedJsonl.append("\"text\":\"").append(escape(finalText.replace("\\n", "\n").replace("\\r", "\r"))).append("\",")
                                importedJsonl.append("\"calendarDay\":\"").append(escape(dateStr)).append("\",")
                                importedJsonl.append("\"dateWritten\":\"").append(escape(nowStr)).append("\",")
                                importedJsonl.append("\"lastUpdated\":\"").append(escape(nowStr)).append("\"")
                                if (currentGeohash != null) {
                                    importedJsonl.append(",\"geohash\":\"").append(escape(currentGeohash!!)).append("\"")
                                }
                                importedJsonl.append("}\n")
                            }
                            inEvent = false
                        }
                        inEvent && line.startsWith("SUMMARY:") -> currentSummary = line.substring(8)
                        inEvent && line.startsWith("DESCRIPTION:") -> currentDesc = line.substring(12)
                        inEvent && line.startsWith("DTSTART") -> {
                            val parts = line.split(":")
                            if (parts.size > 1) currentDtStart = parts[1].trim()
                        }
                        inEvent && line.startsWith("LOCATION:Geohash ") -> currentGeohash = line.substring(17).trim()
                    }
                }
                
                val result = noteRepository.importJsonlData(importedJsonl.toString())
                onComplete(result)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }
}
