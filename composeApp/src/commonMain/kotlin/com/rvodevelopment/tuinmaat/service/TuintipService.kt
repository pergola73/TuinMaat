package com.rvodevelopment.tuinmaat.service

interface TuintipService {
    suspend fun getTuintip(): Result<String>
}
