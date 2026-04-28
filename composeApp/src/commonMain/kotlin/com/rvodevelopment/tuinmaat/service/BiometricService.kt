package com.rvodevelopment.tuinmaat.service

interface BiometricService {
    fun isBiometricAvailable(): Boolean
    suspend fun authenticate(): Result<Unit>
}
