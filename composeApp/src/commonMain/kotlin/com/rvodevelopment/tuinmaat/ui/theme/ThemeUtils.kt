package com.rvodevelopment.tuinmaat.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp

expect fun Modifier.neumorphicShadow(
    shape: Shape = RoundedCornerShape(20.dp),
    baseColor: Color = ZachtBeige
): Modifier
