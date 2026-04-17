package com.example.tuinmaat.ui.theme

import androidx.compose.ui.graphics.Color

// --- Basis Kleurenpalet ---
val DonkerGroen = Color(0xFF2D4739)  // Het diepe, chique groen voor tekst en iconen
val GrasGroen = Color(0xFF7BA65D)    // Accentkleur voor acties
val BladGroen = Color(0xFF81C784)    // Zachtere groentint voor variatie
val ZachtBeige = Color(0xFFF5F5F0)   // De rustige achtergrondkleur

val OrganischGroen = Color(0xFFF2F7F2) // Heel licht, bijna wit-groen


// --- Neumorphic Kleuren (Cruciaal voor het diepte-effect) ---
// Deze kleuren moeten gebaseerd zijn op ZachtBeige om de knoppen te laten "versmelten"
val ShadowLight = Color(0xFFFFFFFF)  // Puur wit voor de lichtinval (linksboven)
val ShadowDark = Color(0xFFE2E2D9)   // Net iets donkerder dan ZachtBeige voor de schaduw (rechtsonder)

// --- Extra Details ---
// Semi-transparant wit voor de kleine badges zoals "8 planten"
val GlassyWhite = Color(0xFFFFFFFF).copy(alpha = 0.6f)