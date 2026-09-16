package com.example.moon.data.repository

import com.example.moon.domain.model.EventType
import com.example.moon.domain.model.LocationData
import com.example.moon.domain.model.LunarEvent
import com.example.moon.domain.model.MoonData
import com.example.moon.domain.model.MoonPhase
import com.example.moon.domain.repository.AstronomyRepository
import dev.jamesyox.kastro.luna.calculateLunarIllumination
import dev.jamesyox.kastro.luna.calculateLunarPosition
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant as KastroInstant

@OptIn(kotlin.time.ExperimentalTime::class)
private fun kotlinx.datetime.Instant.toKastro(): KastroInstant =
    KastroInstant.fromEpochMilliseconds(toEpochMilliseconds())

class AstronomyRepositoryImpl : AstronomyRepository {

    private var cachedData: Pair<Pair<Long, LocationData>, MoonData>? = null

    @OptIn(kotlin.time.ExperimentalTime::class)
    override fun getMoonData(date: LocalDateTime, location: LocationData): MoonData {
        return getMoonDataInternal(date, location, includeDetails = true)
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun getMoonDataInternal(date: LocalDateTime, location: LocationData, includeDetails: Boolean): MoonData {
        val instant = date.toInstant(TimeZone.currentSystemDefault())
        val roundedTimeMs = (instant.toEpochMilliseconds() / 60000) * 60000

        if (includeDetails) {
            cachedData?.let { (key, data) ->
                if (key.first == roundedTimeMs && key.second == location) return data
            }
        }
        
        val illumination = instant.toKastro().calculateLunarIllumination()
        val position = instant.toKastro().calculateLunarPosition(location.latitude, location.longitude)

        val phase = mapAngleToMoonPhase(illumination.phase)
        
        val nextEvent = if (includeDetails) findNextMajorEvent(date, location) else null
        val horizonTimes = if (includeDetails) findHorizonEvents(date, location) else Pair(null, null)

        val result = MoonData(
            phase = phase,
            illumination = illumination.fraction,
            age = calculateMoonAge(illumination.phase),
            riseTime = horizonTimes.first,
            setTime = horizonTimes.second,
            altitude = position.altitude,
            azimuth = position.azimuth,
            parallacticAngle = position.parallacticAngle,
            nextEvent = nextEvent
        )
        
        if (includeDetails) {
            cachedData = Pair(Pair(roundedTimeMs, location), result)
        }
        return result
    }

    // New method for calendar optimized view
    override fun getBasicMoonData(date: LocalDate, location: LocationData): MoonData {
        val dateTime = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 12, 0)
        return getMoonDataInternal(dateTime, location, includeDetails = false)
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun findHorizonEvents(date: LocalDateTime, location: LocationData): Pair<LocalDateTime?, LocalDateTime?> {
        val startInstant = date.toInstant(TimeZone.currentSystemDefault())
        var currentMs = startInstant.toEpochMilliseconds()
        val stepMs = 30 * 60 * 1000L 
        
        val maxSteps = (24 * 60) / 30
        
        var prevPos = kotlinx.datetime.Instant.fromEpochMilliseconds(currentMs).toKastro().calculateLunarPosition(location.latitude, location.longitude)
        
        var riseTime: LocalDateTime? = null
        var setTime: LocalDateTime? = null
        
        var stepCount = 0
        while (stepCount < maxSteps && (riseTime == null || setTime == null)) {
            val nextMs = currentMs + stepMs
            val nextPos = kotlinx.datetime.Instant.fromEpochMilliseconds(nextMs).toKastro().calculateLunarPosition(location.latitude, location.longitude)
            
            val h1 = prevPos.altitude
            val h2 = nextPos.altitude
            val targetAlt = -0.833

            if (h1 < targetAlt && h2 >= targetAlt && riseTime == null) {
                riseTime = refineHorizonCrossing(currentMs, nextMs, targetAlt, location, true)
            } else if (h1 > targetAlt && h2 <= targetAlt && setTime == null) {
                setTime = refineHorizonCrossing(currentMs, nextMs, targetAlt, location, false)
            }
            
            currentMs = nextMs
            prevPos = nextPos
            stepCount++
        }
        
        return Pair(riseTime, setTime)
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun refineHorizonCrossing(t1: Long, t2: Long, targetAlt: Double, location: LocationData, rising: Boolean): LocalDateTime {
        var low = t1
        var high = t2
        for (i in 0 until 5) {
            val mid = (low + high) / 2
            val alt = kotlinx.datetime.Instant.fromEpochMilliseconds(mid).toKastro()
                .calculateLunarPosition(location.latitude, location.longitude).altitude
            
            if (rising) {
                if (alt < targetAlt) low = mid else high = mid
            } else {
                if (alt > targetAlt) low = mid else high = mid
            }
        }
        return kotlinx.datetime.Instant.fromEpochMilliseconds((low + high) / 2).toLocalDateTime(TimeZone.currentSystemDefault())
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun findNextMajorEvent(date: LocalDateTime, location: LocationData): LunarEvent? {
        val startInstant = date.toInstant(TimeZone.currentSystemDefault())
        var currentMs = startInstant.toEpochMilliseconds()
        val stepMs = 6 * 60 * 60 * 1000L 
        
        val maxSteps = (45 * 24) / 6 // Increased range to ensure we find the next Full/Dark moon
        
        var prevPhase = kotlinx.datetime.Instant.fromEpochMilliseconds(currentMs).toKastro().calculateLunarIllumination().phase
        
        for (i in 0 until maxSteps) {
            val nextMs = currentMs + stepMs
            val nextPhase = kotlinx.datetime.Instant.fromEpochMilliseconds(nextMs).toKastro().calculateLunarIllumination().phase
            
            val crossedEvent = checkCrossingAndRefine(prevPhase, nextPhase, currentMs, nextMs)
            if (crossedEvent != null && (crossedEvent.type == EventType.NEW_MOON || crossedEvent.type == EventType.FULL_MOON)) {
                return crossedEvent
            }
            
            currentMs = nextMs
            prevPhase = nextPhase
        }
        
        return null
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    override fun getLunarEvents(year: Int, month: Int, location: LocationData): List<LunarEvent> {
        val startInstant = LocalDateTime(year, month, 1, 0, 0).toInstant(TimeZone.currentSystemDefault())
        val endInstant = if (month == 12) {
            LocalDateTime(year + 1, 1, 1, 0, 0)
        } else {
            LocalDateTime(year, month + 1, 1, 0, 0)
        }.toInstant(TimeZone.currentSystemDefault())

        val events = mutableListOf<LunarEvent>()
        
        var currentMs = startInstant.toEpochMilliseconds()
        val endMs = endInstant.toEpochMilliseconds()
        val stepMs = 6 * 60 * 60 * 1000L 
        
        var prevPhase = kotlinx.datetime.Instant.fromEpochMilliseconds(currentMs).toKastro().calculateLunarIllumination().phase
        
        while (currentMs < endMs) {
            val nextMs = currentMs + stepMs
            val nextPhase = kotlinx.datetime.Instant.fromEpochMilliseconds(nextMs).toKastro().calculateLunarIllumination().phase
            
            val crossedEvent = checkCrossingAndRefine(prevPhase, nextPhase, currentMs, nextMs)
            if (crossedEvent != null && crossedEvent.dateTime.toInstant(TimeZone.currentSystemDefault()) < endInstant) {
                val moonData = getMoonData(crossedEvent.dateTime, location)
                events.add(crossedEvent.copy(moonData = moonData))
            }
            
            currentMs = nextMs
            prevPhase = nextPhase
        }

        return events.sortedBy { it.dateTime }
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun checkCrossingAndRefine(p1: Double, p2: Double, t1: Long, t2: Long): LunarEvent? {
        val target: Double = when {
            p1 < 90.0 && p2 >= 90.0 -> 90.0
            p1 < 180.0 && p2 >= 180.0 -> 180.0
            p1 < 270.0 && p2 >= 270.0 -> 270.0
            p2 < p1 && p1 > 270.0 && p2 < 90.0 -> 360.0 
            else -> return null
        }

        var low = t1
        var high = t2
        for (i in 0 until 6) { 
            val mid = (low + high) / 2
            var phase = kotlinx.datetime.Instant.fromEpochMilliseconds(mid).toKastro().calculateLunarIllumination().phase
            if (target == 360.0 && phase < 180.0) phase += 360.0 
            
            if (phase < target) low = mid else high = mid
        }
        
        val refinedMs = (low + high) / 2
        val refinedPhase = target % 360.0
        val eventType = when (refinedPhase) {
            90.0 -> EventType.FIRST_QUARTER
            180.0 -> EventType.FULL_MOON
            270.0 -> EventType.LAST_QUARTER
            else -> EventType.NEW_MOON
        }

        return LunarEvent(
            type = eventType,
            dateTime = kotlinx.datetime.Instant.fromEpochMilliseconds(refinedMs).toLocalDateTime(TimeZone.currentSystemDefault()),
            isSuperMoon = false,
            isMicroMoon = false
        )
    }

    private fun mapAngleToMoonPhase(angle: Double): MoonPhase {
        val normalized = angle % 360.0
        return when {
            normalized < 11.25 || normalized >= 348.75 -> MoonPhase.NEW
            normalized < 78.75 -> MoonPhase.WAXING_CRESCENT
            normalized < 101.25 -> MoonPhase.FIRST_QUARTER
            normalized < 168.75 -> MoonPhase.WAXING_GIBBOUS
            normalized < 191.25 -> MoonPhase.FULL
            normalized < 258.75 -> MoonPhase.WANING_GIBBOUS
            normalized < 281.25 -> MoonPhase.LAST_QUARTER
            else -> MoonPhase.WANING_CRESCENT
        }
    }

    private fun calculateMoonAge(phaseAngle: Double): Double {
        var normalized = (phaseAngle + 180.0) / 360.0
        while (normalized < 0) normalized += 1.0
        while (normalized >= 1) normalized -= 1.0
        
        return normalized * 29.530589
    }
}
