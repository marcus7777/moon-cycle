package com.example.moon.domain.repository

import com.example.moon.domain.model.LocationData
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getLocationUpdates(): Flow<LocationData>
    suspend fun getCurrentLocation(): LocationData
    fun setManualLocation(latitude: Double, longitude: Double, name: String? = null)
    fun clearManualLocation()
    
    fun isFirstLaunch(): Boolean
    fun setFirstLaunchCompleted()

    companion object {
        val DEFAULT_LOCATION = LocationData(
            latitude = 51.5074, // London
            longitude = -0.1278,
            name = "London",
            isDefault = true
        )
    }
}
