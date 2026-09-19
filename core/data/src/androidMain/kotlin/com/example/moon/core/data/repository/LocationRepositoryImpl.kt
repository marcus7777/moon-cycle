package com.example.moon.core.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.example.moon.core.domain.model.LocationData
import com.example.moon.core.domain.repository.LocationRepository
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class LocationRepositoryImpl(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationRepository {

    private val prefs = context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
    private val manualLocation = MutableStateFlow<LocationData?>(loadManualLocation())

    private fun loadManualLocation(): LocationData? {
        if (prefs.getBoolean("use_device", true)) return null
        if (!prefs.contains("manual_lat")) return null
        return LocationData(
            latitude = prefs.getFloat("manual_lat", 0f).toDouble(),
            longitude = prefs.getFloat("manual_lng", 0f).toDouble(),
            name = prefs.getString("manual_name", null),
            isDefault = false
        )
    }

    private fun saveManualLocation(data: LocationData?) {
        prefs.edit().apply {
            if (data != null) {
                putBoolean("use_device", false)
                putFloat("manual_lat", data.latitude.toFloat())
                putFloat("manual_lng", data.longitude.toFloat())
                putString("manual_name", data.name)
            } else {
                putBoolean("use_device", true)
                remove("manual_lat")
                remove("manual_lng")
                remove("manual_name")
            }
        }.apply()
    }

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
        val data = LocationData(
            latitude = latitude,
            longitude = longitude,
            name = name,
            isDefault = false
        )
        manualLocation.value = data
        saveManualLocation(data)
    }

    override fun clearManualLocation() {
        manualLocation.value = null
        saveManualLocation(null)
    }

    override fun isFirstLaunch(): Boolean {
        return prefs.getBoolean("first_launch", true)
    }

    override fun setFirstLaunchCompleted() {
        prefs.edit().putBoolean("first_launch", false).apply()
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
