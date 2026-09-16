package com.example.moon.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moon.data.provider.MoonDataProviderImpl
import com.example.moon.domain.model.LocationData
import com.example.moon.domain.model.MoonData
import com.example.moon.domain.repository.AstronomyRepository
import com.example.moon.domain.repository.LocationRepository
import com.example.moon.domain.manager.WallpaperManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MoonViewModel(
    private val locationRepository: LocationRepository,
    private val astronomyRepository: AstronomyRepository,
    private val wallpaperManager: WallpaperManager? = null
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
                val updated = moonDataProvider.getMoonData()
                _moonData.value = updated
                _lastKnownMoonData.value = updated
                
                // Trigger image/external updates
                wallpaperManager?.updateNow()
            }
        }
    }
}
