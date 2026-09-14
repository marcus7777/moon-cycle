package com.example.moon.data.provider

import com.example.moon.domain.model.MoonData
import com.example.moon.domain.provider.MoonDataProvider
import com.example.moon.domain.repository.AstronomyRepository
import com.example.moon.domain.repository.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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
        return locationRepository.getLocationUpdates().map { location ->
            astronomyRepository.getMoonData(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), location)
        }.flowOn(Dispatchers.Default)
    }

    override suspend fun getMoonData(): MoonData = withContext(Dispatchers.Default) {
        val location = locationRepository.getCurrentLocation()
        astronomyRepository.getMoonData(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), location)
    }
}
