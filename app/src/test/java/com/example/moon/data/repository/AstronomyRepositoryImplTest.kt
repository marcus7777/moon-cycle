package com.example.moon.data.repository

import com.example.moon.core.data.repository.AstronomyRepositoryImpl
import com.example.moon.core.domain.model.EventType
import com.example.moon.core.domain.model.LocationData
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.datetime.LocalDateTime

class AstronomyRepositoryImplTest {

    @Before
    fun setUp() {
        // Touch Kastro classes first to attempt to avoid cyclic class initialization deadlock/NPE
        try {
            Class.forName("dev.jamesyox.kastro.luna.LunarPhase")
            Class.forName("dev.jamesyox.kastro.luna.LunarEvent")
        } catch (_: Exception) {
            // Ignore
        }
    }

    private val repository = AstronomyRepositoryImpl()
    private val mockLocation = LocationData(latitude = 52.5200, longitude = 13.4050) // Berlin

    @Test
    fun getMoonData_includesNextEvent() {
        // Sep 1, 2024 - Next major phase is New Moon on Sep 3
        val date = LocalDateTime(2024, 9, 1, 12, 0)
        val data = repository.getMoonData(date, mockLocation)
        
        val nextEvent = data.nextEvent
        assertTrue(nextEvent != null)
        assertTrue(nextEvent?.type == EventType.NEW_MOON)
        assertTrue(nextEvent?.dateTime?.dayOfMonth == 3)
    }

    @Test
    fun getLunarEvents_returnsEventsForMonth() {
        val events = repository.getLunarEvents(2024, 9, mockLocation)
        
        // Sep 2024 has:
        // New Moon: Sep 3
        // First Quarter: Sep 11
        // Full Moon: Sep 18 (Supermoon)
        // Last Quarter: Sep 24
        
        assertTrue(events.any { it.type == EventType.NEW_MOON && it.dateTime.dayOfMonth == 3 })
        assertTrue(events.any { it.type == EventType.FIRST_QUARTER && it.dateTime.dayOfMonth == 11 })
        assertTrue(events.any { it.type == EventType.FULL_MOON && it.dateTime.dayOfMonth == 18 })
        assertTrue(events.any { it.type == EventType.LAST_QUARTER && it.dateTime.dayOfMonth == 24 })
    }
}
