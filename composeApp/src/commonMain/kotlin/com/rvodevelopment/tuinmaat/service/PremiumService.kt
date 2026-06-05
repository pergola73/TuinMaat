package com.rvodevelopment.tuinmaat.service

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.rvodevelopment.tuinmaat.getPlatform
import com.rvodevelopment.tuinmaat.PlatformType

interface PremiumService {
    val isPremium: StateFlow<Boolean>
    val premiumProductId: String get() = if (getPlatform() == PlatformType.IOS) "premium_unlock_ios" else "premium_unlock"
    fun setPremium(enabled: Boolean)
}

class DefaultPremiumService : PremiumService {
    private val settings = Settings()
    private val _isPremium = MutableStateFlow(settings.getBoolean("is_premium", false))
    override val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    override fun setPremium(enabled: Boolean) {
        settings["is_premium"] = enabled
        _isPremium.value = enabled
    }
}
