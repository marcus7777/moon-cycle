package com.example.moon.core.domain.repository

import com.example.moon.core.domain.model.EventType
import com.example.moon.core.domain.model.LocationData
import com.example.moon.core.domain.model.LunarEvent
import com.example.moon.core.domain.model.MoonData
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

interface AstronomyRepository {
    fun getMoonData(date: LocalDateTime, location: LocationData): MoonData
    fun getBasicMoonData(date: LocalDate, location: LocationData): MoonData
    fun getLunarEvents(year: Int, month: Int, location: LocationData): List<LunarEvent>
    fun getLunarEventsInRange(start: LocalDateTime, end: LocalDateTime, location: LocationData): List<LunarEvent>
    fun findNextEvent(type: EventType, from: LocalDateTime, location: LocationData): LunarEvent?
    fun findPreviousEvent(type: EventType, from: LocalDateTime, location: LocationData): LunarEvent?

    val fullMoonOffsetMinutes: StateFlow<Long>
    fun setFullMoonOffsetMinutes(minutes: Long)
    fun setObservedFullMoonTime(observedTime: LocalDateTime, location: LocationData)
    fun clearFullMoonOffset()
}
