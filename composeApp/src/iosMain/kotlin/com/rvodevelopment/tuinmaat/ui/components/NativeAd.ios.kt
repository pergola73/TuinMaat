package com.rvodevelopment.tuinmaat.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.ui.interop.UIKitView
import androidx.compose.runtime.LaunchedEffect
import com.rvodevelopment.tuinmaat.service.AnalyticsService
import org.koin.compose.koinInject
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeAd(adUnitId: String, modifier: Modifier, isMedium: Boolean) {
    val analyticsService: AnalyticsService = koinInject()
    val isLocked = LocalIsLocked.current
    val factory = PlatformViewRegistry.nativeAdFactory
    val height = if (isMedium) 260.dp else 100.dp
    
    LaunchedEffect(adUnitId) {
        if (!isLocked) {
            analyticsService.logAdImpression(adUnitId, "Native_iOS")
        }
    }
    
    if (factory != null && !isLocked) {
        UIKitView(
            factory = { factory(adUnitId, isMedium) as UIView },
            modifier = modifier.fillMaxWidth().height(height)
        )
    } else {
        Box(modifier = modifier.fillMaxWidth().height(height))
    }
}
