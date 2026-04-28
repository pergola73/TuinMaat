package com.rvodevelopment.tuinmaat.service

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidBiometricService(private val activityProvider: () -> FragmentActivity?) : BiometricService {

    override fun isBiometricAvailable(): Boolean {
        val activity = activityProvider() ?: return false
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    override suspend fun authenticate(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val activity = activityProvider()
        if (activity == null) {
            continuation.resume(Result.failure(Exception("Activity not available")))
            return@suspendCancellableCoroutine
        }
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(Exception(errString.toString())))
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    if (continuation.isActive) {
                        continuation.resume(Result.success(Unit))
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Usually we don't resume here as the user can try again
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometrische Inlog")
            .setSubtitle("Log in met je vingerafdruk of gezichtsherkenning")
            .setNegativeButtonText("Annuleren")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
