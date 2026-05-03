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
