package com.example.moon.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface NoteRepository {
    fun getNotes(): Flow<Map<LocalDate, String>>
    suspend fun saveNote(date: LocalDate, note: String)
    suspend fun deleteNote(date: LocalDate)
}
