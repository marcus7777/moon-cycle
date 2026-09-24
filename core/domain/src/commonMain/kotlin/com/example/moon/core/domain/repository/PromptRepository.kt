package com.example.moon.core.domain.repository

interface PromptRepository {
    fun loadCachedPrompts()
    suspend fun checkAndUpdatePrompts(): Boolean
}
