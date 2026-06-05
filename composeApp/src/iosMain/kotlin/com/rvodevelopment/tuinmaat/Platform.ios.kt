package com.rvodevelopment.tuinmaat

import platform.Foundation.NSBundle

actual fun getPlatform(): PlatformType = PlatformType.IOS

actual val appVersion: String = run {
    val version = (NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String) ?: "2.0.1"
    val build = (NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String) ?: "23"
    "$version ($build)"
}

actual val admobNativeHomeId: String = "ca-app-pub-8227056273089055/1599222975"
actual val admobNativeListId: String = "ca-app-pub-8227056273089055/4967777875"
actual val admobNativeSnoeiId: String = "ca-app-pub-8227056273089055/5512672970"
actual val admobNativeInstellingenId: String = "ca-app-pub-8227056273089055/8750574781"
actual val admobBannerId: String = "ca-app-pub-8227056273089055/1565156276"
