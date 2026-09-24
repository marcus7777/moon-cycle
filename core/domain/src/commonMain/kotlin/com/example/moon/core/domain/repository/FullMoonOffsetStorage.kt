package com.example.moon.core.domain.repository

interface FullMoonOffsetStorage {
    fun getOffsetMinutes(): Long
    fun saveOffsetMinutes(minutes: Long)
}
