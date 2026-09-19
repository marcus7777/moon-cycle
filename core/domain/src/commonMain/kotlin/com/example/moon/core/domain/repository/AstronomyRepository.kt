package com.example.moon.core.domain.repository

import com.example.moon.core.domain.model.LocationData
import com.example.moon.core.domain.model.LunarEvent
import com.example.moon.core.domain.model.MoonData
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

interface AstronomyRepository {
    fun getMoonData(date: LocalDateTime, location: LocationData): MoonData
    fun getBasicMoonData(date: LocalDate, location: LocationData): MoonData
    fun getLunarEvents(year: Int, month: Int, location: LocationData): List<LunarEvent>
}
