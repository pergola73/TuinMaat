package com.rvodevelopment.tuinmaat

import platform.Foundation.NSBundle

actual fun getPlatform(): PlatformType = PlatformType.IOS

actual val appVersion: String = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "3.0.1"
