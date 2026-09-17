package com.example.moon.data.repository

import android.content.Context
import com.example.moon.domain.model.LocationData
import com.example.moon.domain.model.NoteEntry
import com.example.moon.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class NoteRepositoryImpl(context: Context) : NoteRepository {
    private val prefs = context.getSharedPreferences("moon_notes_v4", Context.MODE_PRIVATE)
    private val _fullNotes = MutableStateFlow<List<NoteEntry>>(loadAllNotes())
    private val _notesMap = MutableStateFlow<Map<LocalDate, String>>(emptyMap())

    init {
        updateNotesMap()
    }

    private fun updateNotesMap() {
        val map = _fullNotes.value.associate { 
            try {
                LocalDate.parse(it.calendarDay) to it.text
            } catch (e: Exception) {
                LocalDate(2026, 9, 17) to it.text
            }
        }
        _notesMap.value = map
    }

    override fun getNotes(): Flow<Map<LocalDate, String>> = _notesMap.asStateFlow()
    
    override fun getFullNotes(): Flow<List<NoteEntry>> = _fullNotes.asStateFlow()

    override suspend fun saveNote(date: LocalDate, note: String, locationData: LocationData?) {
        val nowStr = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
        val currentList = _fullNotes.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.calendarDay == date.toString() }
        val gh = locationData?.let { computeGeohash5(it.latitude, it.longitude) }

        if (note.isBlank()) {
            if (existingIndex != -1) {
                currentList.removeAt(existingIndex)
                prefs.edit().remove(date.toString()).apply()
            }
        } else {
            val newEntry = if (existingIndex != -1) {
                val old = currentList[existingIndex]
                old.copy(
                    text = note,
                    lastUpdated = nowStr,
                    geohash = gh ?: old.geohash
                )
            } else {
                NoteEntry(
                    text = note,
                    calendarDay = date.toString(),
                    dateWritten = nowStr,
                    lastUpdated = nowStr,
                    geohash = gh
                )
            }
            if (existingIndex != -1) {
                currentList[existingIndex] = newEntry
            } else {
                currentList.add(newEntry)
            }
            prefs.edit().putString(date.toString(), encodeNoteEntry(newEntry)).apply()
        }

        _fullNotes.value = currentList
        updateNotesMap()
    }

    override suspend fun deleteNote(date: LocalDate) {
        saveNote(date, "", null)
    }

    override suspend fun importJsonlData(jsonlText: String): Boolean {
        try {
            val lines = jsonlText.split("\n").filter { it.isNotBlank() }
            val importedEntries = lines.mapNotNull { decodeNoteEntry(it) }
            
            val editor = prefs.edit()
            editor.clear()
            
            importedEntries.forEach { entry ->
                editor.putString(entry.calendarDay, encodeNoteEntry(entry))
            }
            editor.apply()
            
            _fullNotes.value = importedEntries
            updateNotesMap()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun loadAllNotes(): List<NoteEntry> {
        val all = prefs.all
        val list = mutableListOf<NoteEntry>()
        all.forEach { (_, value) ->
            if (value is String) {
                decodeNoteEntry(value)?.let { list.add(it) }
            }
        }
        return list
    }

    private fun encodeNoteEntry(entry: NoteEntry): String {
        val escape = { s: String -> s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") }
        return buildString {
            append("{")
            append("\"text\":\"").append(escape(entry.text)).append("\",")
            append("\"calendarDay\":\"").append(escape(entry.calendarDay)).append("\",")
            append("\"dateWritten\":\"").append(escape(entry.dateWritten)).append("\",")
            append("\"lastUpdated\":\"").append(escape(entry.lastUpdated)).append("\"")
            if (entry.geohash != null) append(",\"geohash\":\"").append(escape(entry.geohash)).append("\"")
            append("}")
        }
    }

    private fun decodeNoteEntry(jsonStr: String): NoteEntry? {
        try {
            val extractString = { key: String ->
                val idx = jsonStr.indexOf("\"$key\":\"")
                if (idx != -1) {
                    val start = idx + key.length + 4
                    val end = jsonStr.indexOf("\"", start)
                    if (end != -1) {
                        jsonStr.substring(start, end)
                            .replace("\\n", "\n")
                            .replace("\\r", "\r")
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\")
                    } else ""
                } else ""
            }

            val text = extractString("text")
            val calendarDay = extractString("calendarDay")
            val dateWritten = extractString("dateWritten")
            val lastUpdated = extractString("lastUpdated")
            val gh = extractString("geohash")
            val geohash = if (gh.isEmpty() && !jsonStr.contains("\"geohash\"")) null else gh

            if (text.isEmpty() && calendarDay.isEmpty()) return null
            return NoteEntry(
                text = text,
                calendarDay = calendarDay,
                dateWritten = dateWritten,
                lastUpdated = lastUpdated,
                geohash = if (geohash == "") null else geohash
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun computeGeohash5(lat: Double, lng: Double): String {
        val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"
        var minLat = -90.0
        var maxLat = 90.0
        var minLng = -180.0
        var maxLng = 180.0
        
        val geohash = StringBuilder()
        var isEven = true
        var bit = 0
        var ch = 0
        
        while (geohash.length < 5) {
            if (isEven) {
                val mid = (minLng + maxLng) / 2.0
                if (lng >= mid) {
                    ch = ch or (1 shl (4 - bit))
                    minLng = mid
                } else {
                    maxLng = mid
                }
            } else {
                val mid = (minLat + maxLat) / 2.0
                if (lat >= mid) {
                    ch = ch or (1 shl (4 - bit))
                    minLat = mid
                } else {
                    maxLat = mid
                }
            }
            isEven = !isEven
            if (bit < 4) {
                bit++
            } else {
                geohash.append(BASE32[ch])
                bit = 0
                ch = 0
            }
        }
        return geohash.toString()
    }
}
