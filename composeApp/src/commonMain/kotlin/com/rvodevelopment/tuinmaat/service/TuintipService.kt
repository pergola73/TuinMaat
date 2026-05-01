package com.rvodevelopment.tuinmaat.service

data class WeerBericht(
    val temperatuur: Int,
    val conditie: String,
    val icoon: String, // "Sunny", "Rain", "Cloudy", "Windy"
    val advies: String
)

interface TuintipService {
    suspend fun getTuintips(): Result<List<String>>
    suspend fun getWeerBericht(): Result<WeerBericht>
    suspend fun getActueelTuintip(plantNames: List<String> = emptyList()): Result<WeerBericht>
}
