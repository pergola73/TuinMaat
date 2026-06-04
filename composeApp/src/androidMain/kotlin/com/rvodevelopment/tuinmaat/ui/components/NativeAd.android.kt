package com.rvodevelopment.tuinmaat.ui.components

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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

@SuppressLint("MissingPermission")
@Composable
actual fun NativeAd(adUnitId: String, modifier: Modifier, isMedium: Boolean) {
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(adUnitId) {
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad -> nativeAd = ad }
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdClicked() { Log.e("TuinMaatAds", "KLIK!") }
                override fun onAdImpression() { Log.e("TuinMaatAds", "IMPRESSIE!") }
            })
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    if (nativeAd != null) {
        key(nativeAd) {
            AndroidView(
                modifier = modifier
                    .fillMaxWidth()
                    .height(if (isMedium) 240.dp else 100.dp),
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
    }
}
