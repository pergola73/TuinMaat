package com.rvodevelopment.tuinmaat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.random.Random

private val DarkColorScheme = darkColorScheme(
    primary = DonkerGroen,
    secondary = GrasGroen,
    tertiary = BladGroen,
    background = Color(0xFF1B2B22),
    surface = DonkerGroen,
    onPrimary = ZachtBeige,
    onSecondary = ZachtBeige,
    onTertiary = ZachtBeige,
    onBackground = ZachtBeige,
    onSurface = ZachtBeige
)

private val LightColorScheme = lightColorScheme(
    primary = DonkerGroen,
    secondary = GrasGroen,
    tertiary = BladGroen,
    background = ZachtBeige,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = DonkerGroen,
    onTertiary = DonkerGroen,
    onBackground = DonkerGroen,
    onSurface = DonkerGroen
)

@Composable
fun TuinAchtergrond(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AchtergrondGroenLicht,
                        AchtergrondGroenMidden
                    )
                )
            )
    ) {
        content()
    }
}

private fun DrawScope.drawPlantCluster(x: Float, y: Float, random: Random, kleur: Color) {
    val path = Path()
    path.moveTo(x, y)

    val stengelLengte = 50f + random.nextFloat() * 150f
    val kromming = 20f + random.nextFloat() * 60f
    path.cubicTo(
        x + kromming, y - stengelLengte / 2,
        x - kromming, y - stengelLengte / 1.5f,
        x, y - stengelLengte
    )

    drawPath(
        path = path,
        color = kleur,
        style = Stroke(width = 1.5f + random.nextFloat() * 2f, cap = StrokeCap.Round)
    )

    for (j in 0..2) {
        val bladPath = Path()
        val bladX = x + (random.nextFloat() - 0.5f) * 40f
        val bladY = y - (random.nextFloat() * stengelLengte)

        bladPath.moveTo(bladX, bladY)
        bladPath.cubicTo(
            bladX + 20f, bladY - 10f,
            bladX + 10f, bladY - 30f,
            bladX, bladY - 25f
        )
        bladPath.cubicTo(
            bladX - 10f, bladY - 30f,
            bladX - 20f, bladY - 10f,
            bladX, bladY
        )

        drawPath(
            path = bladPath,
            color = kleur.copy(alpha = 0.6f),
            style = Fill
        )
    }
}

@Composable
fun TuinMaatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
