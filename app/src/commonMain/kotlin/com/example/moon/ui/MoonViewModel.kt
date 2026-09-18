package com.example.moon.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moon.data.provider.MoonDataProviderImpl
import com.example.moon.domain.model.LocationData
import com.example.moon.domain.model.MoonData
import com.example.moon.domain.repository.AstronomyRepository
import com.example.moon.domain.repository.LocationRepository
import com.example.moon.domain.manager.WallpaperManager
import com.example.moon.domain.repository.NoteRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MoonViewModel(
    private val locationRepository: LocationRepository,
    private val astronomyRepository: AstronomyRepository,
    private val wallpaperManager: WallpaperManager? = null,
    private val noteRepository: NoteRepository? = null
) : ViewModel() {

    private val moonDataProvider = MoonDataProviderImpl(locationRepository, astronomyRepository)

    private val _locationData = MutableStateFlow<LocationData>(LocationRepository.DEFAULT_LOCATION)
    val locationData: StateFlow<LocationData> = _locationData.asStateFlow()

    private val _moonData = MutableStateFlow<MoonData?>(null)
    val moonData: StateFlow<MoonData?> = _moonData.asStateFlow()

    private val _lastKnownMoonData = MutableStateFlow<MoonData?>(null)
    val lastKnownMoonData: StateFlow<MoonData?> = _lastKnownMoonData.asStateFlow()

    private val _isTextVisible = MutableStateFlow(true)
    val isTextVisible: StateFlow<Boolean> = _isTextVisible.asStateFlow()

    private val _isWallpaperScheduled = MutableStateFlow(wallpaperManager?.isUpdateScheduled() ?: false)
    val isWallpaperScheduled: StateFlow<Boolean> = _isWallpaperScheduled.asStateFlow()

    private val _showSwipeHint = MutableStateFlow(locationRepository.isFirstLaunch())
    val showSwipeHint: StateFlow<Boolean> = _showSwipeHint.asStateFlow()

    private var idleJob: Job? = null

    fun toggleWallpaperSchedule(enabled: Boolean) {
        wallpaperManager?.scheduleDailyUpdate(enabled)
        _isWallpaperScheduled.value = enabled
    }

    fun updateWallpaperNow() {
        viewModelScope.launch {
            wallpaperManager?.updateNow()
        }
    }

    fun toggleTextVisibility() {
        if (_isTextVisible.value) {
            hideText()
        } else {
            showTextWithTimer()
        }
    }

    fun showTextWithTimer() {
        _isTextVisible.value = true
        resetIdleTimer()
    }

    fun setManualLocation(latitude: Double, longitude: Double, name: String? = null) {
        locationRepository.setManualLocation(latitude, longitude, name)
    }

    fun useDeviceLocation() {
        locationRepository.clearManualLocation()
    }

    fun dismissSwipeHint() {
        _showSwipeHint.value = false
        locationRepository.setFirstLaunchCompleted()
    }

    fun exportNotesJsonl(): String {
        var result = ""
        val fullNotesFlow = noteRepository?.getFullNotes()
        if (fullNotesFlow != null) {
            val currentList = (fullNotesFlow as? MutableStateFlow)?.value 
                ?: (fullNotesFlow as? StateFlow)?.value 
                ?: emptyList()
            
            val escape = { s: String -> s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") }
            result = currentList.joinToString("\n") { entry ->
                buildString {
                    append("{")
                    append("\"text\":\"").append(escape(entry.text)).append("\",")
                    append("\"calendarDay\":\"").append(escape(entry.calendarDay)).append("\",")
                    append("\"dateWritten\":\"").append(escape(entry.dateWritten)).append("\",")
                    append("\"lastUpdated\":\"").append(escape(entry.lastUpdated)).append("\"")
                    if (entry.geohash != null) append(",\"geohash\":\"").append(escape(entry.geohash)).append("\"")
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
        val currentList = if (fullNotesFlow != null) {
            (fullNotesFlow as? MutableStateFlow)?.value 
                ?: (fullNotesFlow as? StateFlow)?.value 
                ?: emptyList()
        } else emptyList()

        return buildString {
            append("BEGIN:VCALENDAR\n")
            append("VERSION:2.0\n")
            append("PRODID:-//MoonCycle//Daily Notes//EN\n")
            
            // 1. Export User Notes
            currentList.forEach { entry ->
                val dayClean = entry.calendarDay.replace("-", "")
                append("BEGIN:VEVENT\n")
                append("UID:note-${entry.calendarDay}@com.example.moon\n")
                val summaryText = if (entry.text.length > 30) entry.text.take(27) + "..." else entry.text
                append("SUMMARY:").append(summaryText.replace("\n", " ").replace("\r", " ")).append("\n")
                append("DESCRIPTION:").append(entry.text.replace("\n", "\\n").replace("\r", "\\r")).append("\n")
                append("DTSTART;VALUE=DATE:").append(dayClean).append("\n")
                append("DTEND;VALUE=DATE:").append(dayClean).append("\n")
                if (entry.geohash != null) {
                    append("LOCATION:Geohash ").append(entry.geohash).append("\n")
                }
                append("END:VEVENT\n")
            }

            // 2. Project Full Moon and New Moon phases for the next 5 years (2026 to 2031)
            // Approx synodic lunar month length ~ 29.53 days. Let's populate key calculated lunar landmarks.
            val currentLoc = _locationData.value
            var currentDay = LocalDate(2026, 9, 17)
            val endDay = LocalDate(2031, 9, 17)
            
            // Generate basic timeline steps matching high-accuracy calculations
            while (currentDay.toEpochDays() < endDay.toEpochDays()) {
                val moonCalc = astronomyRepository.getBasicMoonData(currentDay, currentLoc)
                val dayStr = currentDay.toString().replace("-", "")
                
                if (moonCalc.phase == com.example.moon.domain.model.MoonPhase.FULL) {
                    append("BEGIN:VEVENT\n")
                    append("UID:lunar-full-${currentDay}@com.example.moon\n")
                    append("SUMMARY:🌕 Full Moon\n")
                    append("DESCRIPTION:The moon is completely illuminated by the sun.\n")
                    append("DTSTART;VALUE=DATE:").append(dayStr).append("\n")
                    append("DTEND;VALUE=DATE:").append(dayStr).append("\n")
                    append("END:VEVENT\n")
                } else if (moonCalc.phase == com.example.moon.domain.model.MoonPhase.NEW) {
                    append("BEGIN:VEVENT\n")
                    append("UID:lunar-new-${currentDay}@com.example.moon\n")
                    append("SUMMARY:🌑 New Moon\n")
                    append("DESCRIPTION:The moon is in alignment between the earth and sun.\n")
                    append("DTSTART;VALUE=DATE:").append(dayStr).append("\n")
                    append("DTEND;VALUE=DATE:").append(dayStr).append("\n")
                    append("END:VEVENT\n")
                }
                currentDay = LocalDate.fromEpochDays(currentDay.toEpochDays() + 1)
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
                
                var currentUid = ""
                var currentSummary = ""
                var currentDesc = ""
                var currentDtStart = ""
                var currentGeohash: String? = null
                
                var inEvent = false
                
                lines.forEach { line ->
                    when {
                        line.startsWith("BEGIN:VEVENT") -> {
                            inEvent = true
                            currentUid = ""
                            currentSummary = ""
                            currentDesc = ""
                            currentDtStart = ""
                            currentGeohash = null
                        }
                        line.startsWith("END:VEVENT") -> {
                            if (inEvent && currentDtStart.isNotEmpty()) {
                                // format iCal YYYYMMDD to YYYY-MM-DD
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
                        inEvent && line.startsWith("UID:") -> currentUid = line.substring(4)
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

    private fun hideText() {
        idleJob?.cancel()
        _isTextVisible.value = false
    }

    private fun resetIdleTimer() {
        idleJob?.cancel()
        idleJob = viewModelScope.launch {
            delay(5000) // Hide after 5 seconds of idleness
            _isTextVisible.value = false
        }
    }

    init {
        viewModelScope.launch {
            // Calculate initial data with default location immediately
            val initial = moonDataProvider.getMoonData()
            _moonData.value = initial
            _lastKnownMoonData.value = initial
            resetIdleTimer() // Start initial timer

            // Observe updates
            locationRepository.getLocationUpdates().collect { location ->
                _locationData.value = location
                
                // Recalculate moon data using the fresh location immediately
                val now = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                val updated = astronomyRepository.getMoonData(now, location)
                
                _moonData.value = updated
                _lastKnownMoonData.value = updated
                
                // Trigger image/external updates
                wallpaperManager?.updateNow()
            }
        }
    }
}
