package com.rvodevelopment.tuinmaat

actual fun getPlatform(): PlatformType = PlatformType.ANDROID

actual val appVersion: String = com.rvodevelopment.tuinmaat.composeapp.BuildConfig.APP_VERSION
