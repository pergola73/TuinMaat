package com.rvodevelopment.tuinmaat

actual fun getPlatform(): PlatformType = PlatformType.ANDROID

actual val appVersion: String = "2.0.0 (19)"

// Gebruik hardcoded test-IDs als fallback als BuildConfig nog niet is bijgewerkt
actual val admobNativeHomeId: String = "ca-app-pub-3940256099942544/2247696110"
actual val admobNativeListId: String = "ca-app-pub-3940256099942544/2247696110"
