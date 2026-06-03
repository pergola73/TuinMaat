package com.rvodevelopment.tuinmaat.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun AdBanner(modifier: Modifier) {
    val factory = PlatformViewRegistry.bannerFactory
    if (factory != null) {
        val bannerId = "ca-app-pub-3940256099942544/2934735716" // Test banner ID
        UIKitView(
            factory = { factory(bannerId) as UIView },
            modifier = modifier.fillMaxWidth().height(50.dp)
        )
    } else {
        Box(modifier = modifier.fillMaxWidth().height(50.dp))
    }
}
