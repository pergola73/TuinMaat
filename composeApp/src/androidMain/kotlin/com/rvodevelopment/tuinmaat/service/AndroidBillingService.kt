package com.rvodevelopment.tuinmaat.service

import android.app.Activity
import com.android.billingclient.api.*
import com.rvodevelopment.tuinmaat.util.ActivityProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AndroidBillingService(
    private val premiumService: PremiumService
) : BillingService, PurchasesUpdatedListener {

    private var billingClient: BillingClient? = null
    private var productDetails: ProductDetails? = null

    init {
        val activity = ActivityProvider.getCurrentActivity()
        if (activity != null) {
            setupBillingClient(activity)
        }
    }

    private fun setupBillingClient(activity: Activity) {
        billingClient = BillingClient.newBuilder(activity)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Probeer later opnieuw indien nodig
            }
        })
    }

    private fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient?.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPremium = purchases.any { purchase ->
                    purchase.products.contains(premiumService.premiumProductId) && 
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (hasPremium) {
                    premiumService.setPremium(true)
                }
            }
        }

        // Fetch product details for price display
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(premiumService.premiumProductId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val detailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        billingClient?.queryProductDetailsAsync(detailsParams) { _, detailsList ->
            if (detailsList.isNotEmpty()) {
                productDetails = detailsList[0]
            }
        }
    }

    override fun purchasePremium(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val activity = ActivityProvider.getCurrentActivity() ?: run {
            onError("Activity niet beschikbaar")
            return
        }

        if (billingClient == null || billingClient?.isReady == false) {
            setupBillingClient(activity)
            onError("Verbinding met Google Play wordt hersteld. Probeer het over een paar seconden opnieuw.")
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(premiumService.premiumProductId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                
                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    ))
                    .build()

                billingClient?.launchBillingFlow(activity, flowParams)
            } else {
                onError("Product details niet gevonden. Code: ${billingResult.responseCode}, Bericht: ${billingResult.debugMessage}")
            }
        }
    }

    override fun restorePurchases(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient?.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPremium = purchases.any { purchase ->
                    purchase.products.contains(premiumService.premiumProductId) && 
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (hasPremium) {
                    premiumService.setPremium(true)
                    onSuccess()
                } else {
                    onError("Geen eerdere aankopen gevonden.")
                }
            } else {
                onError("Fout bij herstellen: ${billingResult.debugMessage}")
            }
        }
    }

    override fun getProductPrice(productId: String): String? {
        return productDetails?.oneTimePurchaseOfferDetails?.formattedPrice
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.products.contains(premiumService.premiumProductId) && 
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    
                    // Bevestig de aankoop (Acknowledge)
                    if (!purchase.isAcknowledged) {
                        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient?.acknowledgePurchase(acknowledgeParams) { result ->
                            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                                premiumService.setPremium(true)
                            }
                        }
                    } else {
                        premiumService.setPremium(true)
                    }
                }
            }
        }
    }
}
