package com.rvodevelopment.tuinmaat.service

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.models.*
import com.revenuecat.purchases.kmp.ktx.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class RevenueCatService(
    private val premiumService: PremiumService,
    private val apiKey: String,
) : BillingService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _price = MutableStateFlow<String?>(null)

    init {
        if (apiKey.isNotEmpty()) {
            try {
                Purchases.logLevel = LogLevel.DEBUG
                Purchases.configure(apiKey = apiKey)
            } catch (e: Exception) {
                println("RevenueCat initialization failed: ${e.message}")
            }
        }

        scope.launch {
            // Periodically check status if listener is hard to find
            while (isActive) {
                try {
                    val customerInfo = Purchases.sharedInstance.awaitCustomerInfo()
                    checkPremiumStatus(customerInfo)
                } catch (_: Exception) {
                    // Ignore background errors
                }
                delay(30000) // Every 30 seconds
            }
        }
        
        scope.launch {
            fetchOfferings()
        }
    }

    private fun checkPremiumStatus(customerInfo: CustomerInfo) {
        // Als er een actieve entitlement is, beschouwen we de gebruiker als premium
        val hasPremium = customerInfo.entitlements.active.isNotEmpty()
        premiumService.setPremium(hasPremium)
    }

    private suspend fun fetchOfferings() {
        try {
            val offerings = Purchases.sharedInstance.awaitOfferings()
            val current = offerings.current
            _price.value = current?.availablePackages?.firstOrNull()?.storeProduct?.price?.formatted
        } catch (e: Exception) {
            println("RevenueCat: Failed to fetch offerings: ${e.message}")
        }
    }

    override fun purchasePremium(onSuccess: () -> Unit, onError: (String) -> Unit) {
        scope.launch {
            try {
                val offerings = Purchases.sharedInstance.awaitOfferings()
                val offering = offerings.current
                if (offering != null) {
                    val packageToPurchase = offering.availablePackages.firstOrNull()
                    if (packageToPurchase != null) {
                        val result = Purchases.sharedInstance.awaitPurchase(packageToPurchase)
                        checkPremiumStatus(result.customerInfo)
                        onSuccess()
                    } else {
                        onError("Geen pakket gevonden")
                    }
                } else {
                    onError("Geen aanbiedingen beschikbaar")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Aankoop mislukt")
            }
        }
    }

    override fun restorePurchases(onSuccess: () -> Unit, onError: (String) -> Unit) {
        scope.launch {
            try {
                val customerInfo = Purchases.sharedInstance.awaitRestore()
                checkPremiumStatus(customerInfo)
                if (customerInfo.entitlements.active.containsKey("premium")) {
                    onSuccess()
                } else {
                    onError("Geen premium toegang gevonden")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Herstel mislukt")
            }
        }
    }

    override fun getProductPrice(productId: String): String? {
        return _price.value
    }
}
