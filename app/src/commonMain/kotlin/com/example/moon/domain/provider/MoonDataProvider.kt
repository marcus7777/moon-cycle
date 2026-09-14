package com.example.moon.domain.provider

import com.example.moon.domain.model.MoonData
import kotlinx.coroutines.flow.Flow

interface MoonDataProvider {
    fun getMoonDataFlow(): Flow<MoonData>
    suspend fun getMoonData(): MoonData
}
