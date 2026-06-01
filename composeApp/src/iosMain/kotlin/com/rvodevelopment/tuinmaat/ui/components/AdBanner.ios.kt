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

@Composable
actual fun AdBanner(modifier: Modifier) {
    // Voor iOS is verdere configuratie in Xcode nodig voor Google Mobile Ads
    Box(
        modifier = modifier.fillMaxWidth().height(50.dp),
        contentAlignment = Alignment.Center
    ) {
        // Placeholder
        // Text("Ad Banner iOS", fontSize = 10.sp)
    }
}
