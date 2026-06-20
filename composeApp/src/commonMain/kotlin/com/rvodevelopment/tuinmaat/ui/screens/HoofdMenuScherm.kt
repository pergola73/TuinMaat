package com.rvodevelopment.tuinmaat.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.rvodevelopment.tuinmaat.ui.viewmodel.InstellingenViewModel
import com.rvodevelopment.tuinmaat.ui.components.PremiumUpgradeDialog
import org.koin.compose.koinInject
import kotlinx.datetime.*

@Composable
fun HoofdMenuScherm(
    viewModel: HoofdMenuViewModel,
    onNavigate: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var toonTuintip by remember { mutableStateOf(value = true) }
    var toonPremiumDialog by remember { mutableStateOf(value = false) }
    val instellingenViewModel: InstellingenViewModel = koinInject()
    val sharingService: com.rvodevelopment.tuinmaat.service.SharingService = koinInject()
    val selectionService: com.rvodevelopment.tuinmaat.service.SelectionService = koinInject()
    val isPremium by instellingenViewModel.isPremium.collectAsState()
    val isPremiumLaden by instellingenViewModel.isLaden.collectAsState()

    LaunchedEffect(state.openReviewUrl) {
        state.openReviewUrl?.let { url ->
            sharingService.openUrl(url)
            viewModel.reviewUrlGeopend()
        }
    }

    TuinAchtergrond {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            // Garden Switcher
            if (state.gekoppeldeGid != null && (state.gekoppeldeGid != state.eigenGid)) {
                Surface(
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).neumorphicShadow(shape = RoundedCornerShape(12.dp)),
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
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

            Spacer(modifier = Modifier.height(16.dp))

            // Stats and Tuintip Toggle
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { onNavigate("lijst") },
                        color = Color(0xFFF5F5F0),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Park,
                                    contentDescription = null,
                                    tint = GrasGroen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Icon(
                                    Icons.Default.FiberManualRecord,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB6C1), // LightPink voor het roosje
                                    modifier = Modifier.size(6.dp).offset(y = (-2).dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                state.aantalPlanten.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = DonkerGroen
                            )
                        }
                    }

                    state.weerBericht?.let { weer ->
                        WeerCard(weer) {
                            selectionService.markeerSysteemActie()
                            sharingService.openUrl("https://www.google.com/search?q=weer+nederland")
                        }
                    }

                    if (!state.isPremium) {
                        Surface(
                            onClick = { toonPremiumDialog = true },
                            color = Color(0xFFF5F5F0),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFD4AF37),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Upgrade Premium",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DonkerGroen
                                )
                            }
                        }
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
                            Box {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    contentDescription = "Toon Tuintip",
                                    tint = DonkerGroen,
                                    modifier = Modifier.size(20.dp)
                                )
                                // Melding/attentie stipje voor nieuwe functies
                                Surface(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 2.dp, y = (-2).dp),
                                    color = GrasGroen,
                                    shape = CircleShape,
                                    border = BorderStroke(1.5.dp, Color.White)
                                ) {}
                            }
                        }
                    }
                }

                if (state.isPremium && toonTuintip) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TuintipCard(
                        tip = state.huidigeTip,
                        huidigeIndex = state.huidigeTipIndex,
                        totaalAantal = state.tuintips.size,
                        isLoading = state.isTuintipLaden,
                        onVolgende = { viewModel.volgendeTip() },
                        onVorige = { viewModel.vorigeTip() },
                    ) { toonTuintip = false }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // NIEUWE INDELING
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                if (!state.isPremium) {
                    NativeAd(
                        adUnitId = com.rvodevelopment.tuinmaat.admobNativeHomeId,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        isMedium = true
                    )
                }

                // 1. Mijn Planten
                FeatureCard(
                    tekst = "Mijn Planten",
                    subtekst = "Bekijk en verzorg je collectie",
                    icoon = {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Park, 
                                null, 
                                modifier = Modifier.size(20.dp).offset(x = (-4).dp),
                                tint = DonkerGroen
                            )
                            Icon(
                                Icons.Default.Park, 
                                null, 
                                modifier = Modifier.size(24.dp).offset(x = 4.dp),
                                tint = DonkerGroen
                            )
                            Icon(
                                Icons.Default.FiberManualRecord,
                                null,
                                tint = Color(0xFFFFB6C1), // LightPink voor het roosje
                                modifier = Modifier.size(6.dp).offset(y = (-2).dp)
                            )
                        }
                    },
                    containerColor = OrganischGroen,
                    contentColor = DonkerGroen,
                    modifier = Modifier.fillMaxWidth()
                ) { onNavigate("lijst") }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Tuin-Agenda met dynamisch icoon
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).dayOfMonth
                FeatureCard(
                    tekst = "Mijn Tuin-Agenda",
                    subtekst = "Wat moet er vandaag gebeuren?",
                    icoon = {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.CalendarToday, 
                                null, 
                                modifier = Modifier.size(28.dp),
                                tint = DonkerGroen
                            )
                            Text(
                                text = today.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = DonkerGroen,
                                fontSize = 10.sp,
                                modifier = Modifier.offset(y = 2.dp)
                            )
                        }
                    },
                    containerColor = OrganischGroen,
                    contentColor = DonkerGroen,
                    isPremium = !state.isPremium,
                    isNieuw = state.toonNieuwLabel,
                    isAttentie = state.heeftOngelezenBerichten,
                    modifier = Modifier.fillMaxWidth()
                ) { onNavigate("actiecentrum") }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Plant Toevoegen & Dr Tuinmaat (Naast elkaar)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActionCardSmall(
                        tekst = "Plant Toevoegen",
                        icoon = {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocalFlorist, null, modifier = Modifier.size(32.dp))
                                Icon(
                                    Icons.Default.AutoAwesome, 
                                    null, 
                                    modifier = Modifier.size(16.dp).align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp),
                                    tint = Color(0xFF8A2BE2) // Violet voor Gemini effect
                                )
                            }
                        },
                        containerColor = DonkerGroen,
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f)
                    ) { onNavigate("toevoegen") }

                    ActionCardSmall(
                        tekst = "Dr. Tuinmaat",
                        icoon = {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MedicalServices, null, modifier = Modifier.size(32.dp))
                                Icon(
                                    Icons.Default.Add, 
                                    null, 
                                    modifier = Modifier.size(12.dp).align(Alignment.Center),
                                    tint = Color.Red
                                )
                            }
                        },
                        containerColor = DonkerGroen,
                        contentColor = Color.White,
                        isPremium = !state.isPremium,
                        isNieuw = state.toonNieuwLabel,
                        modifier = Modifier.weight(1f)
                    ) { onNavigate("drtuinmaat") }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Tuinontwerp & Instellingen (Onderaan, compact)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (state.toonTuintekenaar) {
                        FeatureCard(
                            tekst = "Tuinontwerp",
                            subtekst = "Visualiseer je droomtuin",
                            icoon = { Icon(Icons.Default.Architecture, null, modifier = Modifier.size(24.dp), tint = Color.White) },
                            containerColor = DonkerGroen,
                            contentColor = Color.White,
                            isPremium = !state.isPremium,
                            isNieuw = state.toonNieuwLabel,
                            modifier = Modifier.fillMaxWidth()
                        ) { onNavigate("tuintekenaar") }
                    }

                    QuickActionKnop(
                        tekst = "Instellingen",
                        icoon = Icons.Default.Settings,
                        modifier = Modifier.fillMaxWidth()
                    ) { onNavigate("instellingen") }
                }
                
            }

            Spacer(modifier = Modifier.height(64.dp))
        }

        // Review Vraag Dialog
        if (state.toonReviewVraag) {
            AlertDialog(
                onDismissRequest = { viewModel.sluitReview() },
                title = { Text("TuinMaat Review ⭐", color = DonkerGroen, fontWeight = FontWeight.Bold) },
                text = { Text("Hoi! Je gebruikt TuinMaat nu al een tijdje. Zou je ons willen helpen door een korte review achter te laten? Zo kunnen we de app blijven verbeteren voor alle tuinders!", color = Color.Black) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.markReviewDone(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = DonkerGroen)
                    ) {
                        Text("Nu reviewen")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.markReviewDone(false) }) {
                        Text("Niet nu", color = Color.Gray)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (toonPremiumDialog) {
            PremiumUpgradeDialog(
                isPremium = isPremium,
                isLaden = isPremiumLaden,
                price = instellingenViewModel.getPremiumPrice(),
                onUpgrade = { instellingenViewModel.upgradeToPremium() },
                onRestore = { instellingenViewModel.restorePurchases() }
            ) {
                toonPremiumDialog = false
            }
        }
    }
}

@Composable
fun TuintipCard(
    tip: String,
    huidigeIndex: Int,
    totaalAantal: Int,
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
fun WeerCard(weer: WeerBericht, onClick: () -> Unit) {
    val weerIcoon = when (weer.icoon) {
        "Sunny" -> Icons.Default.WbSunny
        "Rain" -> Icons.Default.WaterDrop
        "Cloudy" -> Icons.Default.Cloud
        else -> Icons.Default.Air
    }

    Surface(
        onClick = onClick,
        color = Color(0xFFF5F5F0),
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
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
