package com.example.moon.domain.model

import kotlinx.datetime.Clock

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val name: String? = null,
    val altitude: Double = 0.0,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val isDefault: Boolean = false
)
