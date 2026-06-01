package com.rvodevelopment.tuinmaat.ui.components

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.rvodevelopment.tuinmaat.composeapp.BuildConfig
import com.rvodevelopment.tuinmaat.composeapp.R

@SuppressLint("MissingPermission")
@Composable
actual fun NativeAd(adUnitId: String, modifier: Modifier) {
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(adUnitId) {
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad : NativeAd ->
                nativeAd = ad
            }
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    DisposableEffect(nativeAd) {
        onDispose {
            nativeAd?.destroy()
        }
    }

    if (nativeAd != null) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            factory = { ctx ->
                // We laden een XML layout voor de Native Ad
                val view = LayoutInflater.from(ctx).inflate(R.layout.native_ad_layout, null) as NativeAdView
                populateNativeAdView(nativeAd!!, view)
                view
            },
            update = { view ->
                populateNativeAdView(nativeAd!!, view)
            }
        )
    }
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
    adView.iconView = adView.findViewById(R.id.ad_app_icon)

    (adView.headlineView as TextView).text = nativeAd.headline
    
    if (nativeAd.body == null) {
        adView.bodyView?.visibility = View.INVISIBLE
    } else {
        adView.bodyView?.visibility = View.VISIBLE
        (adView.bodyView as TextView).text = nativeAd.body
    }

    if (nativeAd.callToAction == null) {
        adView.callToActionView?.visibility = View.INVISIBLE
    } else {
        adView.callToActionView?.visibility = View.VISIBLE
        (adView.callToActionView as Button).text = nativeAd.callToAction
    }

    if (nativeAd.icon == null) {
        adView.iconView?.visibility = View.GONE
    } else {
        (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
        adView.iconView?.visibility = View.VISIBLE
    }

    adView.setNativeAd(nativeAd)
}
