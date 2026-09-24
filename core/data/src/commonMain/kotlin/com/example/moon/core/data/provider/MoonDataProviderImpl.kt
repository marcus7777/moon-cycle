package com.example.moon.core.data.provider

import com.example.moon.core.domain.model.MoonData
import com.example.moon.core.domain.provider.MoonDataProvider
import com.example.moon.core.domain.repository.AstronomyRepository
import com.example.moon.core.domain.repository.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class MoonDataProviderImpl(
    private val locationRepository: LocationRepository,
    private val astronomyRepository: AstronomyRepository
) : MoonDataProvider {

    override fun getMoonDataFlow(): Flow<MoonData> {
        return combine(
            locationRepository.getLocationUpdates(),
            astronomyRepository.fullMoonOffsetMinutes
        ) { location, _ ->
            astronomyRepository.getMoonData(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), location)
        }.flowOn(Dispatchers.Default)
    }

    override suspend fun getMoonData(): MoonData = withContext(Dispatchers.Default) {
        val location = locationRepository.getCurrentLocation()
        astronomyRepository.getMoonData(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), location)
    }
}
