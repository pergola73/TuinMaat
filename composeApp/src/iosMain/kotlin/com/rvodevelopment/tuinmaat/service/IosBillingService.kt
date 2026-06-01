package com.rvodevelopment.tuinmaat.service

class IosBillingService : BillingService {
    override fun purchasePremium(onSuccess: () -> Unit, onError: (String) -> Unit) {
        // iOS implementatie met StoreKit volgt later
        onError("Nog niet beschikbaar op iOS")
    }

    override fun restorePurchases(onSuccess: () -> Unit, onError: (String) -> Unit) {
        // iOS implementatie met StoreKit volgt later
        onError("Nog niet beschikbaar op iOS")
    }

    override fun getProductPrice(productId: String): String? {
        return null
    }
}
