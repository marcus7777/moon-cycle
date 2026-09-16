package com.example.moon.data.repository

import android.content.Context
import com.example.moon.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate

class NoteRepositoryImpl(context: Context) : NoteRepository {
    private val prefs = context.getSharedPreferences("moon_notes", Context.MODE_PRIVATE)
    private val _notes = MutableStateFlow<Map<LocalDate, String>>(loadNotes())
    
    override fun getNotes(): Flow<Map<LocalDate, String>> = _notes.asStateFlow()

    override suspend fun saveNote(date: LocalDate, note: String) {
        val current = _notes.value.toMutableMap()
        current[date] = note
        _notes.value = current
        prefs.edit().putString(date.toString(), note).apply()
    }

    override suspend fun deleteNote(date: LocalDate) {
        val current = _notes.value.toMutableMap()
        current.remove(date)
        _notes.value = current
        prefs.edit().remove(date.toString()).apply()
    }

    private fun loadNotes(): Map<LocalDate, String> {
        val all = prefs.all
        val map = mutableMapOf<LocalDate, String>()
        all.forEach { (key, value) ->
            if (value is String) {
                try {
                    map[key.toLocalDate()] = value
                } catch (e: Exception) {
                    // Ignore invalid keys
                }
            }
        }
        return map
    }
}
