package com.rvodevelopment.tuinmaat.ui.screens

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rvodevelopment.tuinmaat.service.WeerBericht
import com.rvodevelopment.tuinmaat.ui.components.*
import com.rvodevelopment.tuinmaat.ui.theme.*
import com.rvodevelopment.tuinmaat.ui.viewmodel.HoofdMenuViewModel
import com.rvodevelopment.tuinmaat.ui.viewmodel.InstellingenViewModel
import com.rvodevelopment.tuinmaat.ui.components.PremiumUpgradeDialog
import org.koin.compose.koinInject
import kotlinx.datetime.*

// Kleuren voor het nieuwe ontwerp
val BotanischGroen = Color(0xFF2D5A36)
val ModernOffWhite = Color(0xFFF7F9F6)
val ZachtGroenAccent = Color(0xFFD8EED8)

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

    Scaffold(
        containerColor = ModernOffWhite,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()) // Alleen bottom padding van Scaffold/BottomBar
                .verticalScroll(rememberScrollState()),
        ) {
            // 1. Header Section (Green Container)
            TuinMaatHeader(
                titel = state.tuinnaam,
                subtitel = "Hallo ${state.voornaam}, welkom in",
            )

            // 2. Hero Image Section (Full Color Lavender)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1591857177580-dc82b9ac4e1e?q=80&w=1000",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Info Pills overlay op de foto
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.weerBericht?.let { weer ->
                        InfoPill(
                            icon = when (weer.icoon) {
                                "Sunny" -> Icons.Default.WbSunny
                                "Rain" -> Icons.Default.WaterDrop
                                else -> Icons.Default.Cloud
                            },
                            text = "${weer.temperatuur}°C",
                            onClick = {
                                selectionService.markeerSysteemActie()
                                sharingService.openUrl("https://www.google.com/search?q=weer+nederland")
                            }
                        )
                    }

                    if (!state.isPremium) {
                        InfoPill(
                            icon = Icons.Default.Star,
                            iconColor = Color(0xFFFFD700),
                            text = "Upgrade Premium",
                            onClick = { toonPremiumDialog = true }
                        )
                    }
                }
            }

            // 3. Main Dashboard Content
            Column(modifier = Modifier.padding(top = 16.dp)) {
                
                // Garden Switcher
                if (state.gekoppeldeGid != null && (state.gekoppeldeGid != state.eigenGid)) {
                    GardenSwitcher(state, viewModel)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Vandaag in de tuin
                SectionHeader(Icons.Default.CalendarToday, "Vandaag in de tuin")
                
                if (state.vandaagActies.isNotEmpty()) {
                    state.vandaagActies.forEach { actie ->
                        VandaagActionCard(actie) { onNavigate("actiecentrum") }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            "Lekker rustig vandaag! Geen taken gepland. 🌿",
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                // Minder ruimte tussen onderdelen zoals gevraagd
                Spacer(modifier = Modifier.height(16.dp))

                // Nieuw in je tuin (Carousel)
                Text(
                    text = "Nieuw in je tuin",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BotanischGroen,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 16.dp)
                ) {
                    if (state.recentPlanten.isNotEmpty()) {
                        items(state.recentPlanten.size) { index ->
                            val plant = state.recentPlanten[index]
                            PlantCarouselCard(
                                naam = plant.naam,
                                fotoUrl = plant.fotoUri,
                                onClick = { onNavigate("detail/${plant.firestoreId}") }
                            )
                        }
                    } else {
                        val fallbacks = listOf(
                            "https://images.unsplash.com/photo-1520412099561-64835287a95a?w=400",
                            "https://images.unsplash.com/photo-1530633762170-171eeadbb83a?w=400"
                        )
                        items(fallbacks.size) { index ->
                            PlantCarouselCard(
                                naam = "Inspiratie",
                                fotoUrl = fallbacks[index],
                                onClick = { onNavigate("toevoegen") }
                            )
                        }
                    }
                }

                // Tuintips & Features
                if (state.isPremium) {
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        if (toonTuintip) {
                            TuintipCard(
                                tip = state.huidigeTip,
                                huidigeIndex = state.huidigeTipIndex,
                                totaalAantal = state.tuintips.size,
                                isLoading = state.isTuintipLaden,
                                onVolgende = { viewModel.volgendeTip() },
                                onVorige = { viewModel.vorigeTip() },
                            ) { toonTuintip = false }
                        } else {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                IconButton(
                                    onClick = { toonTuintip = true },
                                    modifier = Modifier.align(Alignment.CenterEnd).background(Color.White, CircleShape)
                                ) {
                                    Icon(Icons.Default.Lightbulb, null, tint = BotanischGroen)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (state.toonTuintekenaar) {
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        FeatureCard(
                            tekst = "2D Tuinontwerp",
                            subtekst = "Teken je plattegrond en sleep planten",
                            icoon = { Icon(Icons.Default.Architecture, null, modifier = Modifier.size(24.dp), tint = Color.White) },
                            containerColor = BotanischGroen,
                            contentColor = Color.White,
                            isPremium = !state.isPremium,
                            isNieuw = state.toonNieuwLabel,
                            modifier = Modifier.fillMaxWidth()
                        ) { onNavigate("tuintekenaar") }
                    }
                }

                if (!state.isPremium) {
                    Spacer(modifier = Modifier.height(16.dp))
                    NativeAd(
                        adUnitId = com.rvodevelopment.tuinmaat.admobNativeHomeId,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        isMedium = true
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Review & Premium Dialogs
        if (state.toonReviewVraag) {
            ReviewDialog(viewModel)
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
fun InfoPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color = Color.White,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Black.copy(alpha = 0.4f), // Donkerdere pil voor leesbaarheid op foto
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = BotanischGroen, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BotanischGroen)
    }
}

@Composable
fun VandaagActionCard(
    actie: com.rvodevelopment.tuinmaat.premium.notifications.MaintenanceAction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp), // Minder verticale padding tussen kaarten
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = ZachtGroenAccent,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val icon = when (actie.type) {
                        com.rvodevelopment.tuinmaat.premium.notifications.ActionType.PRUNING -> Icons.Default.ContentCut
                        com.rvodevelopment.tuinmaat.premium.notifications.ActionType.WATERING -> Icons.Default.WaterDrop
                        else -> Icons.Default.Spa
                    }
                    Icon(icon, null, tint = BotanischGroen, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(actie.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BotanischGroen)
                Text(actie.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = ZachtGroenAccent, contentColor = BotanischGroen),
                shape = RoundedCornerShape(50.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = when (actie.type) {
                        com.rvodevelopment.tuinmaat.premium.notifications.ActionType.PRUNING -> "Snoeien"
                        com.rvodevelopment.tuinmaat.premium.notifications.ActionType.WATERING -> "Water"
                        else -> "Bekijken"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PlantCarouselCard(
    naam: String,
    fotoUrl: String?,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (fotoUrl != null) {
                    AsyncImage(
                        model = fotoUrl,
                        contentDescription = naam,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(ZachtGroenAccent), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LocalFlorist, null, tint = BotanischGroen.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                    }
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = naam,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = BotanischGroen,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun GardenSwitcher(state: com.rvodevelopment.tuinmaat.ui.viewmodel.HoofdMenuState, viewModel: HoofdMenuViewModel) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val isEigen = state.actieveGid == state.eigenGid
            SwitcherButton("Mijn Tuin", isEigen) { state.eigenGid?.let { viewModel.switchGarden(it) } }
            SwitcherButton("Gedeelde Tuin", !isEigen) { state.gekoppeldeGid?.let { viewModel.switchGarden(it) } }
        }
    }
}

@Composable
private fun RowScope.SwitcherButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f).height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) BotanischGroen else Color.Transparent,
            contentColor = if (isSelected) Color.White else BotanischGroen
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = null,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun ReviewDialog(viewModel: HoofdMenuViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.sluitReview() },
        title = { Text("TuinMaat Review ⭐", color = BotanischGroen, fontWeight = FontWeight.Bold) },
        text = { Text("Hoi! Je gebruikt TuinMaat nu al een tijdje. Zou je ons willen helpen door een korte review achter te laten?", color = Color.Black) },
        confirmButton = {
            Button(
                onClick = { viewModel.markReviewDone(true) },
                colors = ButtonDefaults.buttonColors(containerColor = BotanischGroen)
            ) { Text("Nu reviewen") }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.markReviewDone(false) }) { Text("Niet nu", color = Color.Gray) }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
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
        color = Color.White,
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
                        .background(BotanischGroen.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = BotanischGroen,
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
                            color = BotanischGroen
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
                        color = BotanischGroen.copy(alpha = 0.5f)
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp).align(Alignment.Top)) {
                    Icon(Icons.Default.Close, contentDescription = "Sluiten", tint = BotanischGroen.copy(alpha = 0.3f))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(modifier = Modifier.heightIn(min = 60.dp), contentAlignment = Alignment.CenterStart) {
                if (isLoading && tip.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BotanischGroen, strokeWidth = 2.dp)
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
                    color = BotanischGroen.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "${huidigeIndex + 1} van $totaalAantal tips",
                        style = MaterialTheme.typography.labelSmall,
                        color = BotanischGroen,
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
                            tint = if (huidigeIndex > 0) BotanischGroen else BotanischGroen.copy(alpha = 0.2f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = onVolgende, 
                        modifier = Modifier
                            .size(36.dp)
                            .background(BotanischGroen.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.ChevronRight, null, tint = BotanischGroen)
                    }
                }
            }
        }
    }
}
