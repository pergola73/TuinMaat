package com.rvodevelopment.tuinmaat.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rvodevelopment.tuinmaat.service.WeerBericht
import com.rvodevelopment.tuinmaat.ui.components.MenuKnop
import com.rvodevelopment.tuinmaat.ui.components.TuinAchtergrond
import com.rvodevelopment.tuinmaat.ui.components.TuinMaatLogo
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.theme.GrasGroen
import com.rvodevelopment.tuinmaat.ui.theme.neumorphicShadow
import com.rvodevelopment.tuinmaat.ui.viewmodel.HoofdMenuViewModel

@Composable
fun HoofdMenuScherm(
    viewModel: HoofdMenuViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var toonTuintip by remember { mutableStateOf(true) }

    TuinAchtergrond {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Garden Switcher
            if (state.gekoppeldeGid != null && state.gekoppeldeGid != state.eigenGid) {
                Surface(
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).neumorphicShadow(shape = RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { state.eigenGid?.let { viewModel.switchGarden(it) } },
                            modifier = Modifier.weight(1f).height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.actieveGid == state.eigenGid) GrasGroen else Color.Transparent,
                                contentColor = if (state.actieveGid == state.eigenGid) Color.White else DonkerGroen
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Mijn Tuin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { state.gekoppeldeGid?.let { viewModel.switchGarden(it) } },
                            modifier = Modifier.weight(1f).height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.actieveGid == state.gekoppeldeGid) GrasGroen else Color.Transparent,
                                contentColor = if (state.actieveGid == state.gekoppeldeGid) Color.White else DonkerGroen
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Gedeelde Tuin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Header Section with Logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hallo ${state.voornaam}, welkom in",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DonkerGroen.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = state.tuinnaam,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = DonkerGroen,
                        lineHeight = 42.sp
                    )

                    if (state.eigenaarNaam != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = null,
                                tint = DonkerGroen.copy(alpha = 0.4f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tuin van ${state.eigenaarNaam}",
                                style = MaterialTheme.typography.bodySmall,
                                color = DonkerGroen.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                TuinMaatLogo(modifier = Modifier.padding(start = 16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stats and Tuintip Toggle
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.neumorphicShadow(shape = RoundedCornerShape(50.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.LocalFlorist,
                                contentDescription = null,
                                tint = DonkerGroen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "${state.aantalPlanten} Planten",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = DonkerGroen
                            )
                        }
                    }

                    state.weerBericht?.let { weer ->
                        WeerCard(weer)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (!toonTuintip) {
                        IconButton(
                            onClick = { toonTuintip = true },
                            modifier = Modifier
                                .size(40.dp)
                                .neumorphicShadow(shape = CircleShape)
                                .background(Color.White.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = "Toon Tuintip",
                                tint = DonkerGroen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                if (toonTuintip) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TuintipCard(
                        tip = state.huidigeTip,
                        huidigeIndex = state.huidigeTipIndex,
                        totaalAantal = state.tuintips.size,
                        maand = state.huidigeMaand,
                        isLoading = state.isTuintipLaden,
                        onVolgende = { viewModel.volgendeTip() },
                        onVorige = { viewModel.vorigeTip() },
                        onDismiss = { toonTuintip = false }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Menu Items
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                MenuKnop("Mijn Planten", Icons.AutoMirrored.Filled.List) { onNavigate("lijst") }
                MenuKnop("Plant Toevoegen", Icons.Default.Add) { onNavigate("toevoegen") }
                MenuKnop("Snoei Kalender", Icons.Default.CalendarToday) { onNavigate("snoeikalender") }
                MenuKnop("Instellingen", Icons.Default.Settings) { onNavigate("instellingen") }
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
fun TuintipCard(
    tip: String,
    huidigeIndex: Int,
    totaalAantal: Int,
    maand: Int,
    isLoading: Boolean,
    onVolgende: () -> Unit,
    onVorige: () -> Unit,
    onDismiss: () -> Unit
) {
    val seizoenKleur = when (maand) {
        in 3..5 -> Color(0xFFF1F8E9) // Lente
        in 6..8 -> Color(0xFFFFFDE7) // Zomer
        in 9..11 -> Color(0xFFFBE9E7) // Herfst
        else -> Color(0xFFE3F2FD) // Winter
    }

    Surface(
        color = seizoenKleur.copy(alpha = 0.8f),
        shape = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp),
        border = BorderStroke(1.dp, DonkerGroen.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .neumorphicShadow(shape = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFFFD600),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Team TuinMaat tip:", 
                        fontWeight = FontWeight.Bold, 
                        style = MaterialTheme.typography.labelMedium,
                        color = DonkerGroen
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Sluiten", tint = DonkerGroen.copy(alpha = 0.5f))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isLoading && tip.isEmpty()) {
                // Toon niets of een subtiele placeholder ipv zandloper bij de eerste keer laden
            } else {
                Text(
                    tip, 
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), 
                    color = Color.Black.copy(alpha = 0.8f)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DonkerGroen, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Text(
                        "${huidigeIndex + 1} / $totaalAantal",
                        style = MaterialTheme.typography.labelSmall,
                        color = DonkerGroen.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onVorige, 
                        modifier = Modifier.size(32.dp),
                        enabled = huidigeIndex > 0
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft, 
                            null, 
                            tint = if (huidigeIndex > 0) DonkerGroen else DonkerGroen.copy(alpha = 0.3f)
                        )
                    }
                    IconButton(onClick = onVolgende, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ChevronRight, null, tint = DonkerGroen)
                    }
                }
            }
        }
    }
}

@Composable
fun WeerCard(weer: WeerBericht) {
    val weerIcoon = when (weer.icoon) {
        "Sunny" -> Icons.Default.WbSunny
        "Rain" -> Icons.Default.WaterDrop
        "Cloudy" -> Icons.Default.Cloud
        else -> Icons.Default.Air
    }

    Surface(
        color = Color.White.copy(alpha = 0.5f),
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier.neumorphicShadow(shape = RoundedCornerShape(50.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                weerIcoon,
                contentDescription = null,
                tint = DonkerGroen,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "${weer.temperatuur}°C",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = DonkerGroen
            )
        }
    }
}
