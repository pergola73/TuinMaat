package com.rvodevelopment.tuinmaat

enum class PlatformType {
    ANDROID, IOS
}

expect fun getPlatform(): PlatformType

expect val appVersion: String

expect val admobNativeHomeId: String
expect val admobNativeListId: String
expect val admobNativeSnoeiId: String
expect val admobNativeInstellingenId: String
expect val admobBannerId: String
