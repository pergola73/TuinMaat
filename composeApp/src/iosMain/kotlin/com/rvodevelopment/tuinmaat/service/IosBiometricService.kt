package com.rvodevelopment.tuinmaat.service

import kotlinx.coroutines.*
import platform.LocalAuthentication.*
import platform.Foundation.NSError
import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class IosBiometricService : BiometricService {
    override fun isBiometricAvailable(): Boolean {
        val context = LAContext()
        return context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, null)
    }

    override suspend fun authenticate(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val context = LAContext()
        
        // Zorg dat we de biometrie dialoog direct aanroepen
        context.evaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            localizedReason = "Ontgrendel TuinMaat",
            reply = { success, error ->
                // Gebruik de main dispatcher om terug te keren naar de coroutine
                // Dit zorgt ervoor dat Compose state updates op de juiste thread gebeuren
                CoroutineScope(Dispatchers.Main).launch {
                    if (success) {
                        if (continuation.isActive) continuation.resume(Result.success(Unit))
                    } else {
                        val message = when (error?.code) {
                            LAErrorAuthenticationFailed -> "Authenticatie mislukt"
                            LAErrorUserCancel -> "Geannuleerd door gebruiker"
                            LAErrorUserFallback -> "Gebruiker koos voor wachtwoord"
                            LAErrorBiometryNotAvailable -> "Face ID/Touch ID niet beschikbaar"
                            LAErrorBiometryNotEnrolled -> "Face ID/Touch ID niet ingesteld"
                            LAErrorBiometryLockout -> "Face ID/Touch ID is geblokkeerd"
                            else -> error?.localizedDescription ?: "Biometrische authenticatie mislukt"
                        }
                        if (continuation.isActive) continuation.resume(Result.failure(Exception(message)))
                    }
                }
            }
        )
    }
}
