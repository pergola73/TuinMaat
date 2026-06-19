package com.rvodevelopment.tuinmaat.premium.planner

import com.rvodevelopment.tuinmaat.premium.BasePremiumViewModel
import com.rvodevelopment.tuinmaat.premium.PremiumManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GardenPlannerState(
    val isLoading: Boolean = false
)

class GardenPlannerViewModel(
    premiumManager: PremiumManager
) : BasePremiumViewModel(premiumManager) {
    private val _state = MutableStateFlow(GardenPlannerState())
    val state: StateFlow<GardenPlannerState> = _state.asStateFlow()
}
