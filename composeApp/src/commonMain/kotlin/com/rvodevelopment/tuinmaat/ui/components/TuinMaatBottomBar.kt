package com.rvodevelopment.tuinmaat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.theme.GrasGroen
import com.rvodevelopment.tuinmaat.ui.theme.neumorphicShadow

@Composable
fun TuinMaatBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .neumorphicShadow(shape = RoundedCornerShape(24.dp)),
        color = Color.White.copy(alpha = 0.95f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Links: Kalender & Dr Tuinmaat
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BottomNavItem(
                    icon = Icons.Default.CalendarToday,
                    isSelected = currentRoute == "actiecentrum",
                    onClick = { onNavigate("actiecentrum") }
                )
                BottomNavItem(
                    icon = Icons.Default.MedicalServices,
                    isSelected = currentRoute?.startsWith("drtuinmaat") == true,
                    onClick = { onNavigate("drtuinmaat") }
                )
            }

            // Midden: Grote Toevoegen Knop
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .offset(y = (-12).dp)
                    .neumorphicShadow(shape = CircleShape)
                    .background(DonkerGroen, CircleShape)
                    .clickable { onNavigate("toevoegen") },
                contentAlignment = Alignment.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = "Toevoegen",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFE0B0FF), // Lichter paars voor Gemini effect
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                    )
                }
            }

            // Rechts: Plantenlijst & Instellingen
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BottomNavItem(
                    icon = Icons.Default.LocalFlorist,
                    isSelected = currentRoute == "lijst",
                    onClick = { onNavigate("lijst") }
                )
                BottomNavItem(
                    icon = Icons.Default.Settings,
                    isSelected = currentRoute == "instellingen",
                    onClick = { onNavigate("instellingen") }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isSelected) GrasGroen else DonkerGroen.copy(alpha = 0.4f),
            modifier = Modifier.size(26.dp)
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(4.dp)
                    .background(GrasGroen, CircleShape)
            )
        }
    }
}

