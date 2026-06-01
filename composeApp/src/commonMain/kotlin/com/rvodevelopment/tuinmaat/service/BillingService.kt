package com.rvodevelopment.tuinmaat.service

interface BillingService {
    fun purchasePremium(onSuccess: () -> Unit, onError: (String) -> Unit)
    fun restorePurchases(onSuccess: () -> Unit, onError: (String) -> Unit)
    fun getProductPrice(productId: String): String?
}
