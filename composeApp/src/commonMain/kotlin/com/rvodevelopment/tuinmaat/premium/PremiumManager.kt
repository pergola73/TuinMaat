package com.rvodevelopment.tuinmaat.premium

import com.rvodevelopment.tuinmaat.service.PremiumService
import kotlinx.coroutines.flow.StateFlow

/**
 * Centraal beheer voor alle Premium features in v2.3.0.
 * Dit is de toegangspoort voor de verschillende premium modules.
 */
class PremiumManager(
    private val premiumService: PremiumService
) {
    val isPremium: StateFlow<Boolean> = premiumService.isPremium

    fun canUseFeature(feature: PremiumFeature): Boolean {
        // Voor nu is alles premium-only, maar we kunnen hier tiers toevoegen
        return isPremium.value
    }
}

enum class PremiumFeature {
    DR_TUINMAAT,
    GARDEN_PLANNER,
    ACTION_CENTER,
    PERSONAL_NOTES_AI // Toekomstig: AI suggesties voor notities
}
