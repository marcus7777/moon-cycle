package com.example.moon.domain.model

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val name: String? = null,
    val altitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false
)
