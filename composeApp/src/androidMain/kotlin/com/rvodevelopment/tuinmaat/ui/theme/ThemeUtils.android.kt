package com.rvodevelopment.tuinmaat.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp

actual fun Modifier.neumorphicShadow(
    shape: Shape,
    baseColor: Color
): Modifier = this.drawBehind {
    val shadowColor = Color.Black.copy(alpha = 0.1f)
    val highlightColor = Color.White.copy(alpha = 1f)

    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()

        // Donkere schaduw (rechtsonder)
        frameworkPaint.color = shadowColor.toArgb()
        frameworkPaint.setShadowLayer(25f, 12f, 12f, shadowColor.toArgb())
        canvas.drawOutline(shape.createOutline(size, layoutDirection, this), paint)

        // Lichte schaduw/glans (linksboven)
        frameworkPaint.color = highlightColor.toArgb()
        frameworkPaint.setShadowLayer(25f, -12f, -12f, highlightColor.toArgb())
        canvas.drawOutline(shape.createOutline(size, layoutDirection, this), paint)
    }
}
