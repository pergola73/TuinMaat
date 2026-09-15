package com.rvodevelopment.tuinmaat.repository

interface ConfigRepository {
    suspend fun getGeminiModel(): String
}
