package com.rvodevelopment.tuinmaat.ui.components

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.rvodevelopment.tuinmaat.composeapp.R
import com.rvodevelopment.tuinmaat.service.AnalyticsService
import org.koin.compose.koinInject

@SuppressLint("MissingPermission")
@Composable
actual fun NativeAd(adUnitId: String, modifier: Modifier, isMedium: Boolean) {
    val analyticsService: AnalyticsService = koinInject()
    val isLocked = LocalIsLocked.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val height = if (isMedium) 240.dp else 100.dp

    LaunchedEffect(adUnitId) {
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad -> nativeAd = ad }
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdClicked() { 
                    Log.e("TuinMaatAds", "KLIK!") 
                    analyticsService.logEvent("ad_click_android", mapOf("ad_unit_id" to adUnitId))
                }
                override fun onAdImpression() { 
                    Log.e("TuinMaatAds", "IMPRESSIE!") 
                    analyticsService.logAdImpression(adUnitId, "Native_Android")
                }
            })
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    if (nativeAd != null && !isLocked) {
        key(nativeAd) {
            AndroidView(
                modifier = modifier
                    .fillMaxWidth()
                    .height(height),
                factory = { ctx ->
                    val adView = LayoutInflater.from(ctx).inflate(R.layout.native_ad_layout, null, false) as NativeAdView
                    
                    adView.headlineView = adView.findViewById(R.id.ad_headline)
                    adView.bodyView = adView.findViewById(R.id.ad_body)
                    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
                    adView.iconView = adView.findViewById(R.id.ad_app_icon)
                    adView.mediaView = adView.findViewById(R.id.ad_media)

                    (adView.headlineView as TextView).text = nativeAd?.headline
                    (adView.bodyView as TextView).text = nativeAd?.body
                    (adView.callToActionView as TextView).text = nativeAd?.callToAction
                    
                    if (nativeAd?.icon != null) {
                        (adView.iconView as ImageView).setImageDrawable(nativeAd?.icon?.drawable)
                        adView.iconView?.visibility = View.VISIBLE
                    } else {
                        adView.iconView?.visibility = View.GONE
                    }

                    if (isMedium) {
                        adView.mediaView?.visibility = View.VISIBLE
                    } else {
                        adView.mediaView?.visibility = View.GONE
                    }

                    adView.setNativeAd(nativeAd!!)
                    adView
                }
            )
        }
    } else {
        Box(modifier = modifier.fillMaxWidth().height(height))
    }
}
