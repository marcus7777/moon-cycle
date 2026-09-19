package com.example.moon.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moon.core.domain.model.LocationData
import com.example.moon.core.domain.model.MoonData
import com.example.moon.core.domain.repository.LocationRepository
import com.example.moon.core.domain.repository.AstronomyRepository
import com.example.moon.core.domain.provider.MoonDataProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class MainViewModel(
    private val locationRepository: LocationRepository,
    private val astronomyRepository: AstronomyRepository,
    private val moonDataProvider: MoonDataProvider
) : ViewModel() {

    private val _locationData = MutableStateFlow<LocationData>(LocationRepository.DEFAULT_LOCATION)
    val locationData: StateFlow<LocationData> = _locationData.asStateFlow()

    private val _moonData = MutableStateFlow<MoonData?>(null)
    val moonData: StateFlow<MoonData?> = _moonData.asStateFlow()

    private val _isTextVisible = MutableStateFlow(true)
    val isTextVisible: StateFlow<Boolean> = _isTextVisible.asStateFlow()

    private val _showSwipeHint = MutableStateFlow(locationRepository.isFirstLaunch())
    val showSwipeHint: StateFlow<Boolean> = _showSwipeHint.asStateFlow()

    private var idleJob: Job? = null

    init {
        viewModelScope.launch {
            val initial = moonDataProvider.getMoonData()
            _moonData.value = initial
            resetIdleTimer()

            locationRepository.getLocationUpdates().collect { location ->
                _locationData.value = location
                val now = kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val updated = astronomyRepository.getMoonData(now, location)
                _moonData.value = updated
            }
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

    private fun hideText() {
        idleJob?.cancel()
        _isTextVisible.value = false
    }

    fun resetIdleTimer() {
        idleJob?.cancel()
        idleJob = viewModelScope.launch {
            delay(5000)
            _isTextVisible.value = false
        }
    }

    fun dismissSwipeHint() {
        _showSwipeHint.value = false
        locationRepository.setFirstLaunchCompleted()
    }
}
