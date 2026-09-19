package com.example.moon.core.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import com.example.moon.core.domain.model.NoteEntry
import com.example.moon.core.domain.model.LocationData

interface NoteRepository {
    fun getNotes(): Flow<Map<LocalDate, String>>
    fun getFullNotes(): Flow<List<NoteEntry>>
    suspend fun saveNote(date: LocalDate, note: String, locationData: LocationData? = null)
    suspend fun deleteNote(date: LocalDate)
    suspend fun importJsonlData(jsonlText: String): Boolean
}
