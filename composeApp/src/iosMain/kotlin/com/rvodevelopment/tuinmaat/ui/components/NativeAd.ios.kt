package com.rvodevelopment.tuinmaat.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeAd(adUnitId: String, modifier: Modifier) {
    val factory = PlatformViewRegistry.nativeAdFactory
    if (factory != null) {
        UIKitView(
            factory = { factory(adUnitId) as UIView },
            modifier = modifier.fillMaxWidth().height(100.dp)
        )
    } else {
        Box(modifier = modifier.fillMaxWidth().height(100.dp))
    }
}
