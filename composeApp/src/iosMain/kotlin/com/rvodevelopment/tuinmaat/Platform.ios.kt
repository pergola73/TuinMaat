package com.rvodevelopment.tuinmaat

import platform.Foundation.NSBundle

actual fun getPlatform(): PlatformType = PlatformType.IOS

actual val appVersion: String = run {
    val version = (NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String) ?: "2.3.0"
    val build = (NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String) ?: "30"
    "$version ($build)"
}

// AdMob Test IDs for Test Version 2.2.2 (iOS)
actual val admobNativeHomeId: String = "ca-app-pub-3940256099942544/3986624511" // Prod: ca-app-pub-8227056273089055/1599222975
actual val admobNativeListId: String = "ca-app-pub-3940256099942544/3986624511" // Prod: ca-app-pub-8227056273089055/4967777875
actual val admobNativeSnoeiId: String = "ca-app-pub-3940256099942544/3986624511" // Prod: ca-app-pub-8227056273089055/5512672970
actual val admobNativeInstellingenId: String = "ca-app-pub-3940256099942544/3986624511" // Prod: ca-app-pub-8227056273089055/8750574781
actual val admobBannerId: String = "ca-app-pub-3940256099942544/2934735716" // Prod: ca-app-pub-8227056273089055/1565156276
