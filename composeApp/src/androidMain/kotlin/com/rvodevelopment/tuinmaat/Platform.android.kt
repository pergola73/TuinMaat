package com.rvodevelopment.tuinmaat

import com.rvodevelopment.tuinmaat.composeapp.BuildConfig

actual fun getPlatform(): PlatformType = PlatformType.ANDROID

actual val appVersion: String = "${BuildConfig.APP_VERSION} (${BuildConfig.BUILD_NUMBER})"

// AdMob Test IDs for Test Version 2.2.2
actual val admobNativeHomeId: String = "ca-app-pub-3940256099942544/2247696110" // Prod: ca-app-pub-8227056273089055/3080745478
actual val admobNativeListId: String = "ca-app-pub-3940256099942544/2247696110" // Prod: ca-app-pub-8227056273089055/8666290249
actual val admobNativeSnoeiId: String = "ca-app-pub-3940256099942544/2247696110" // Prod: ca-app-pub-8227056273089055/9037515241
actual val admobNativeInstellingenId: String = "ca-app-pub-3940256099942544/2247696110" // Prod: ca-app-pub-8227056273089055/5334073955
actual val admobBannerId: String = "ca-app-pub-3940256099942544/6300978111" // Prod: ca-app-pub-8227056273089055/7079383007
