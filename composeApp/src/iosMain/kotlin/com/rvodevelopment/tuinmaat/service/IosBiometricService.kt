package com.rvodevelopment.tuinmaat.service

import kotlinx.coroutines.suspendCancellableCoroutine
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
        if (context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, null)) {
            context.evaluatePolicy(
                LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                localizedReason = "Ontgrendel TuinMaat",
                reply = { success, error ->
                    if (success) {
                        continuation.resume(Result.success(Unit))
                    } else {
                        continuation.resume(Result.failure(Exception(error?.localizedDescription ?: "Biometrische authenticatie mislukt")))
                    }
                }
            )
        } else {
            continuation.resume(Result.failure(Exception("Biometrie niet beschikbaar")))
        }
    }
}
