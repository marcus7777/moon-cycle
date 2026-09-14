package com.example.moon.data.location

import com.example.moon.domain.model.LocationData

expect class LocationProvider {
    suspend fun getCurrentLocation(): LocationData
}
