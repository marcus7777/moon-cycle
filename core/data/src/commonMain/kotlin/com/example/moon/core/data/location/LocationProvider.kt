package com.example.moon.core.data.location

import com.example.moon.core.domain.model.LocationData

expect class LocationProvider {
    suspend fun getCurrentLocation(): LocationData
}
