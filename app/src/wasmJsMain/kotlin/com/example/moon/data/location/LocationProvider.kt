package com.example.moon.data.location

import com.example.moon.core.domain.model.LocationData
import com.example.moon.core.domain.repository.LocationRepository

actual class LocationProvider {
    actual suspend fun getCurrentLocation(): LocationData {
        // Simple stub for now
        return LocationRepository.DEFAULT_LOCATION
    }
}
