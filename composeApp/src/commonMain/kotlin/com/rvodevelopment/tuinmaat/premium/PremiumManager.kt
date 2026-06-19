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

    fun isFeatureVisible(feature: PremiumFeature): Boolean {
        return when (feature) {
            PremiumFeature.GARDEN_PLANNER -> {
                // TODO: Later op true zetten voor productie
                true
            }
            else -> true
        }
    }

    fun canUseFeature(feature: PremiumFeature): Boolean {
        // Voor nu is alles premium-only
        return isPremium.value
    }
}

enum class PremiumFeature {
    DR_TUINMAAT,
    GARDEN_PLANNER,
    ACTION_CENTER,
    PERSONAL_NOTES_AI // Toekomstig: AI suggesties voor notities
}
