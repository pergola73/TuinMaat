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
import com.rvodevelopment.tuinmaat.ui.components.*
import com.rvodevelopment.tuinmaat.ui.theme.*
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
                .navigationBarsPadding()
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

                    if (state.isPremium && !toonTuintip) {
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

                if (state.isPremium && toonTuintip) {
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

                if (!state.isPremium) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        onClick = { onNavigate("instellingen") },
                        color = GrasGroen.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().neumorphicShadow(shape = RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFD600), modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Ontgrendel TuinMaat Premium", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DonkerGroen)
                                Text("Verwijder advertenties en krijg meer tips", style = MaterialTheme.typography.labelSmall, color = DonkerGroen.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }

            if (!state.isPremium) {
                NativeAd(
                    adUnitId = com.rvodevelopment.tuinmaat.admobNativeHomeId,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

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
    Surface(
        color = Color.White.copy(alpha = 0.6f),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .neumorphicShadow(shape = RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(GrasGroen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = GrasGroen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Premium Tuintip", 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = DonkerGroen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD600),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        "Exclusief advies voor jouw tuin",
                        style = MaterialTheme.typography.labelSmall,
                        color = DonkerGroen.copy(alpha = 0.5f)
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp).align(Alignment.Top)) {
                    Icon(Icons.Default.Close, contentDescription = "Sluiten", tint = DonkerGroen.copy(alpha = 0.3f))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(modifier = Modifier.heightIn(min = 60.dp), contentAlignment = Alignment.CenterStart) {
                if (isLoading && tip.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DonkerGroen, strokeWidth = 2.dp)
                } else {
                    Text(
                        tip, 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = Color.Black.copy(alpha = 0.8f),
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = DonkerGroen.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "${huidigeIndex + 1} van $totaalAantal tips",
                        style = MaterialTheme.typography.labelSmall,
                        color = DonkerGroen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onVorige, 
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.3f), CircleShape),
                        enabled = huidigeIndex > 0
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft, 
                            null, 
                            tint = if (huidigeIndex > 0) DonkerGroen else DonkerGroen.copy(alpha = 0.2f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = onVolgende, 
                        modifier = Modifier
                            .size(36.dp)
                            .background(DonkerGroen.copy(alpha = 0.1f), CircleShape)
                    ) {
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
