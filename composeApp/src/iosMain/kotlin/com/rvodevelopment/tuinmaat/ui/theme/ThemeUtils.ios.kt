package com.rvodevelopment.tuinmaat.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

actual fun Modifier.neumorphicShadow(
    shape: Shape,
    baseColor: Color
): Modifier = this.shadow(
    elevation = 4.dp,
    shape = shape,
    clip = false,
    ambientColor = ShadowDark,
    spotColor = ShadowDark
)
