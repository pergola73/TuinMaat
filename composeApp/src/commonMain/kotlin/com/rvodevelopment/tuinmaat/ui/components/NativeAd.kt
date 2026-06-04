package com.rvodevelopment.tuinmaat.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun NativeAd(adUnitId: String, modifier: Modifier = Modifier, isMedium: Boolean = true)
