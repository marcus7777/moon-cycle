package com.example.moon.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NoteEntry(
    val text: String,
    val calendarDay: String, // LocalDate ISO format (e.g., "2026-09-17")
    val dateWritten: String, // LocalDateTime ISO format
    val lastUpdated: String, // LocalDateTime ISO format
    val geohash: String? = null
)
