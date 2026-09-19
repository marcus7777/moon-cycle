package com.example.moon.core.domain.provider

import com.example.moon.core.domain.model.MoonData
import kotlinx.coroutines.flow.Flow

interface MoonDataProvider {
    fun getMoonDataFlow(): Flow<MoonData>
    suspend fun getMoonData(): MoonData
}
