package com.rvodevelopment.tuinmaat

import platform.Foundation.NSBundle

actual fun getPlatform(): PlatformType = PlatformType.IOS

actual val appVersion: String = run {
    val version = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "2.0.0"
    val build = NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String ?: "1"
    "$version ($build)"
}
