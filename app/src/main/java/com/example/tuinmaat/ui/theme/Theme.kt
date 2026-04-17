package com.example.tuinmaat.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

private val DarkColorScheme = darkColorScheme(
    primary = DonkerGroen,
    secondary = GrasGroen,
    tertiary = BladGroen,
    background = Color(0xFF1B2B22), // Iets donkerder voor diepte in dark mode
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
    background = ZachtBeige,     // Cruciaal voor Neumorphisme
    surface = Color.White,       // Knoppen steken nu heel licht af
    onPrimary = Color.White,
    onSecondary = DonkerGroen,
    onTertiary = DonkerGroen,
    onBackground = DonkerGroen,
    onSurface = DonkerGroen
)
@Composable
fun TuinAchtergrond(content: @Composable () -> Unit) {
    val achtergrondKleur = Color(0xFFF2F7F2)
    val plantLijnKleur = Color(0xFF2D4739)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(achtergrondKleur)
    ) {
        // Alpha verhoogd naar 0.15f voor betere zichtbaarheid
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.15f)) {
            val random = java.util.Random(42)
            for (i in 0..40) {
                val x = random.nextFloat() * size.width
                val y = random.nextFloat() * size.height
                drawPlantCluster(x, y, random, plantLijnKleur)
            }
        }

        // De content komt bovenop het canvas
        content()
    }
}

// Hulpopdracht om een willekeurige plantenvorm te tekenen
private fun DrawScope.drawPlantCluster(x: Float, y: Float, random: java.util.Random, kleur: Color) {
    val path = Path()
    path.moveTo(x, y)

    // Een eenvoudige organische stengel
    val stengelLengte = 50f + random.nextFloat() * 150f
    val kromming = 20f + random.nextFloat() * 60f
    path.cubicTo(
        x + kromming, y - stengelLengte / 2,
        x - kromming, y - stengelLengte / 1.5f,
        x, y - stengelLengte
    )

    // Teken de stengel
    drawPath(
        path = path,
        color = kleur,
        style = Stroke(width = 1.5f + random.nextFloat() * 2f, cap = StrokeCap.Round)
    )

    // Teken een paar blaadjes langs de stengel
    for (j in 0..2) {
        val bladPath = Path()
        val bladX = x + (random.nextFloat() - 0.5f) * 40f
        val bladY = y - (random.nextFloat() * stengelLengte)

        // Simpele bladvorm met bezier curves
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
            style = Fill // Blaadjes zijn gevuld
        )
    }
}
@Composable
fun TuinMaatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We zetten dynamicColor standaard op false om je eigen kleuren te behouden
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}