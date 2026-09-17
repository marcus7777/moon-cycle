package com.example.moon.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import com.example.moon.domain.model.NoteEntry

interface NoteRepository {
    fun getNotes(): Flow<Map<LocalDate, String>>
    fun getFullNotes(): Flow<List<NoteEntry>>
    suspend fun saveNote(date: LocalDate, note: String, locationData: com.example.moon.domain.model.LocationData? = null)
    suspend fun deleteNote(date: LocalDate)
    suspend fun importJsonlData(jsonlText: String): Boolean
}
