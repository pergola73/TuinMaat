package com.rvodevelopment.tuinmaat.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Registry om platform-specifieke UI (zoals AdMob op iOS) te tonen vanuit Swift.
 */
object PlatformViewRegistry {
    var bannerFactory: ((String) -> Any)? = null
    var nativeAdFactory: ((String) -> Any)? = null
}
