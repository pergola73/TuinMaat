package com.rvodevelopment.tuinmaat.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

/**
 * Basis ViewModel voor alle Premium features.
 * Handelt centraal de check af of een gebruiker toegang heeft.
 */
abstract class BasePremiumViewModel(
    protected val premiumManager: PremiumManager
) : ViewModel() {
    
    val isPremium: StateFlow<Boolean> = premiumManager.isPremium
    
    // Gemeenschappelijke logica voor paywall events etc.
    fun onFeatureLockedClick() {
        // Logica om paywall te tonen
    }
}
