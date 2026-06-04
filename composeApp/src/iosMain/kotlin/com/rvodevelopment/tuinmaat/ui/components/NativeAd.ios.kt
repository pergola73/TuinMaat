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
actual fun NativeAd(adUnitId: String, modifier: Modifier, isMedium: Boolean) {
    val factory = PlatformViewRegistry.nativeAdFactory
    val height = if (isMedium) 260.dp else 100.dp
    if (factory != null) {
        UIKitView(
            factory = { factory(adUnitId, isMedium) as UIView },
            modifier = modifier.fillMaxWidth().height(height)
        )
    } else {
        Box(modifier = modifier.fillMaxWidth().height(height))
    }
}
