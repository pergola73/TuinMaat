package com.rvodevelopment.tuinmaat.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

import android.annotation.SuppressLint

@SuppressLint("MissingPermission")
@Composable
actual fun AdBanner(modifier: Modifier) {
    AndroidView<AdView>(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-3940256099942544/6300978111" // Test ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
