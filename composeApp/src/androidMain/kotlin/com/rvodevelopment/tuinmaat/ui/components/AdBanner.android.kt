package com.rvodevelopment.tuinmaat.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@SuppressLint("MissingPermission")
@Composable
actual fun AdBanner(modifier: Modifier) {
    val isLocked = LocalIsLocked.current
    if (isLocked) {
        Box(modifier.fillMaxWidth().height(50.dp))
        return
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = com.rvodevelopment.tuinmaat.admobBannerId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
