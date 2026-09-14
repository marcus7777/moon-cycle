package com.example.moon.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.example.moon.domain.model.LocationData
import com.example.moon.domain.repository.LocationRepository
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class LocationRepositoryImpl(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationRepository {

    private val manualLocation = MutableStateFlow<LocationData?>(null)

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): LocationData {
        manualLocation.value?.let { return it }
        return try {
            val location = withTimeoutOrNull(5000) {
                fusedLocationClient.lastLocation.await()
            }
            location?.toLocationData() ?: LocationRepository.DEFAULT_LOCATION
        } catch (e: Exception) {
            LocationRepository.DEFAULT_LOCATION
        }
    }

    @SuppressLint("MissingPermission")
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun getLocationUpdates(): Flow<LocationData> {
        val deviceUpdates = callbackFlow {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let {
                        trySend(it.toLocationData())
                    }
                }
            }

            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: Exception) {
                close(e)
            }

            awaitClose {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }

        return manualLocation.flatMapLatest { manual ->
            if (manual != null) {
                flowOf(manual)
            } else {
                deviceUpdates
            }
        }
    }

    override fun setManualLocation(latitude: Double, longitude: Double, name: String?) {
        manualLocation.value = LocationData(
            latitude = latitude,
            longitude = longitude,
            name = name,
            isDefault = false
        )
    }

    override fun clearManualLocation() {
        manualLocation.value = null
    }

    private fun Location.toLocationData(): LocationData {
        return LocationData(
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
            timestamp = time,
            isDefault = false
        )
    }
}
