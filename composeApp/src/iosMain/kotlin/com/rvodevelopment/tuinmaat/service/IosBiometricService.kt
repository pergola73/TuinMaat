package com.rvodevelopment.tuinmaat.service

class IosBiometricService : BiometricService {
    override fun isBiometricAvailable(): Boolean = false
    override suspend fun authenticate(): Result<Unit> = Result.failure(Exception("Not implemented for iOS"))
}
