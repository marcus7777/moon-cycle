package com.example.moon.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moon.data.repository.AstronomyRepositoryImpl
import com.example.moon.domain.model.LocationData
import com.example.moon.domain.model.LunarEvent
import com.example.moon.domain.model.MoonData
import com.example.moon.domain.repository.NoteRepository
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

    fun saveNote(date: LocalDate, note: String) {
        viewModelScope.launch {
            if (note.isBlank()) {
                noteRepository?.deleteNote(date)
            } else {
                noteRepository?.saveNote(date, note)
            }
        }
    }

    fun loadEvents(year: Int, month: Int, location: LocationData) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedYear = year, selectedMonth = month)
            val events = astronomyRepository.getLunarEvents(year, month, location)
            
            // Calculate moon data for every day in the month
            val dailyMoonData = mutableMapOf<LocalDate, MoonData>()
            val firstOfMonth = LocalDate(year, month, 1)
            val daysInMonth = if (month == 12) {
                LocalDate(year + 1, 1, 1).toEpochDays() - firstOfMonth.toEpochDays()
            } else {
                LocalDate(year, month + 1, 1).toEpochDays() - firstOfMonth.toEpochDays()
            }
            
            for (day in 1..daysInMonth) {
                val date = LocalDate(year, month, day.toInt())
                dailyMoonData[date] = astronomyRepository.getBasicMoonData(date, location)
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false, 
                events = events,
                dailyMoonData = dailyMoonData
            )
        }
    }
    
    fun nextMonth(location: LocationData) {
        val nextMonth = if (_uiState.value.selectedMonth == 12) 1 else _uiState.value.selectedMonth + 1
        val nextYear = if (_uiState.value.selectedMonth == 12) _uiState.value.selectedYear + 1 else _uiState.value.selectedYear
        loadEvents(nextYear, nextMonth, location)
    }

    fun previousMonth(location: LocationData) {
        val prevMonth = if (_uiState.value.selectedMonth == 1) 12 else _uiState.value.selectedMonth - 1
        val prevYear = if (_uiState.value.selectedMonth == 1) _uiState.value.selectedYear - 1 else _uiState.value.selectedYear
        loadEvents(prevYear, prevMonth, location)
    }
}

data class CalendarUiState(
    val selectedYear: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year,
    val selectedMonth: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).monthNumber,
    val events: List<LunarEvent> = emptyList(),
    val dailyMoonData: Map<LocalDate, MoonData> = emptyMap(),
    val notes: Map<LocalDate, String> = emptyMap(),
    val isLoading: Boolean = false
)
