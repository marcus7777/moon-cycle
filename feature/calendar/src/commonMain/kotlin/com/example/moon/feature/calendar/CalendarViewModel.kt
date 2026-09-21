package com.example.moon.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moon.core.data.repository.AstronomyRepositoryImpl
import com.example.moon.core.domain.model.EventType
import com.example.moon.core.domain.model.LocationData
import com.example.moon.core.domain.model.LunarEvent
import com.example.moon.core.domain.model.MoonData
import com.example.moon.core.domain.repository.NoteRepository
import com.example.moon.core.domain.util.IcsExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class CalendarViewModel(
    private val noteRepository: NoteRepository? = null
) : ViewModel() {
    private val astronomyRepository = AstronomyRepositoryImpl()

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            noteRepository?.getNotes()?.collect { notes ->
                _uiState.value = _uiState.value.copy(notes = notes)
            }
        }
    }

    fun saveNote(date: LocalDate, note: String, locationData: LocationData? = null) {
        viewModelScope.launch {
            if (note.isBlank()) {
                noteRepository?.deleteNote(date)
            } else {
                noteRepository?.saveNote(date, note, locationData)
            }
        }
    }

    fun loadEvents(referenceDate: LocalDate, location: LocationData) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val refDateTime = LocalDateTime(referenceDate.year, referenceDate.monthNumber, referenceDate.dayOfMonth, 12, 0)
            
            // 1. Find boundaries of the lunar month (New Moon to New Moon)
            val prevNewMoon = astronomyRepository.findPreviousEvent(EventType.NEW_MOON, refDateTime, location)
            val nextNewMoon = astronomyRepository.findNextEvent(EventType.NEW_MOON, refDateTime, location)
            
            if (prevNewMoon == null || nextNewMoon == null) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }

            // 2. Load all events in this lunar cycle
            val events = astronomyRepository.getLunarEventsInRange(prevNewMoon.dateTime, nextNewMoon.dateTime, location)
            
            // 3. Load daily data for each day in the cycle
            val dailyMoonData = mutableMapOf<LocalDate, MoonData>()
            var current = prevNewMoon.dateTime.date
            val end = nextNewMoon.dateTime.date
            
            while (current <= end) {
                dailyMoonData[current] = astronomyRepository.getBasicMoonData(current, location)
                current = current.plus(1, DateTimeUnit.DAY)
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                cycleStart = prevNewMoon,
                cycleEnd = nextNewMoon,
                events = events,
                dailyMoonData = dailyMoonData
            )
        }
    }
    
    fun nextMonth(location: LocationData) {
        val currentEnd = _uiState.value.cycleEnd?.dateTime ?: return
        // Jump to 2 days after the next New Moon to find the FOLLOWING cycle
        loadEvents(currentEnd.date.plus(2, DateTimeUnit.DAY), location)
    }

    fun previousMonth(location: LocationData) {
        val currentStart = _uiState.value.cycleStart?.dateTime ?: return
        // Jump to 2 days before the current cycle start to find the PREVIOUS cycle
        loadEvents(currentStart.date.minus(2, DateTimeUnit.DAY), location)
    }

    fun getIcsExportContent(location: LocationData): String {
        return IcsExporter.generateIcs(_uiState.value.events, location)
    }

    fun getNotesExportContent(): String {
        return noteRepository?.exportAllNotesAsJsonl() ?: ""
    }
}

data class CalendarUiState(
    val cycleStart: LunarEvent? = null,
    val cycleEnd: LunarEvent? = null,
    val events: List<LunarEvent> = emptyList(),
    val dailyMoonData: Map<LocalDate, MoonData> = emptyMap(),
    val notes: Map<LocalDate, String> = emptyMap(),
    val isLoading: Boolean = false
)
