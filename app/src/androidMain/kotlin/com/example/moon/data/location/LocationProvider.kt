package com.example.moon.data.location

import android.annotation.SuppressLint
import android.content.Context
import com.example.moon.domain.model.LocationData
import com.example.moon.domain.repository.LocationRepository
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

actual class LocationProvider(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    actual suspend fun getCurrentLocation(): LocationData {
        return try {
            val location = withTimeoutOrNull(5000) {
                fusedLocationClient.lastLocation.await()
            }
            location?.let {
                LocationData(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    altitude = it.altitude,
                    timestamp = it.time
                )
            } ?: LocationRepository.DEFAULT_LOCATION
        } catch (e: Exception) {
            LocationRepository.DEFAULT_LOCATION
        }
    }
}
