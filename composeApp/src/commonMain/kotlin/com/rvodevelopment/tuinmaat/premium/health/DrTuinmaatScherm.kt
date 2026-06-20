package com.rvodevelopment.tuinmaat.premium.health

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rvodevelopment.tuinmaat.ui.components.PremiumUpgradeDialog
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.viewmodel.InstellingenViewModel
import org.koin.compose.koinInject
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.rvodevelopment.tuinmaat.ui.theme.ZachtBeige
import com.rvodevelopment.tuinmaat.ui.theme.GrasGroen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrTuinmaatScherm(
    viewModel: DrTuinmaatViewModel,
    plantName: String?,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val scrollState = rememberScrollState()
    val instellingenViewModel: InstellingenViewModel = koinInject()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dr. Tuinmaat 🩺", color = DonkerGroen, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DonkerGroen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ZachtBeige)
            )
        },
        containerColor = ZachtBeige
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Informatie kaart voor losse analyse (hoofdmenu)
            if (plantName == null) {
                Surface(
                    color = DonkerGroen.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DonkerGroen.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = DonkerGroen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Je voert een losse analyse uit. Voor een specifiekere diagnose kun je Dr. Tuinmaat ook direct vanuit het detailscherm van een plant in je lijst openen.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DonkerGroen.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Text(
                text = "Stel een diagnose voor ${plantName ?: "je plant"}",
                style = MaterialTheme.typography.headlineSmall,
                color = DonkerGroen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Maak een duidelijke foto van de bladeren of plekken waar je de ziekte vermoedt.",
                style = MaterialTheme.typography.bodyMedium,
                color = DonkerGroen.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Foto Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(DonkerGroen.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (state.geselecteerdeFoto != null) {
                    AsyncImage(
                        model = state.geselecteerdeFoto,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    if (state.isScanning) {
                        VisualScannerOverlay()
                    }
                } else {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = DonkerGroen.copy(alpha = 0.2f),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isScanning) {
                Text(
                    "Bezig met visuele scan...",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = GrasGroen
                )
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    color = GrasGroen,
                    trackColor = GrasGroen.copy(alpha = 0.1f)
                )
            } else if (state.isLaden) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = DonkerGroen)
                    Text(
                        "AI analyseert gezondheidsprofiel...", 
                        modifier = Modifier.padding(top = 8.dp), 
                        color = DonkerGroen,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else if (state.resultaat == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { viewModel.maakFoto(plantName) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DonkerGroen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Camera")
                    }
                    
                    OutlinedButton(
                        onClick = { viewModel.kiesFoto(plantName) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DonkerGroen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Galerij")
                    }
                }
            }

            state.error?.let {
                Spacer(modifier = Modifier.height(24.dp))
                Text(it, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
            }

            state.resultaat?.let { res ->
                Spacer(modifier = Modifier.height(32.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HealthAndSafety, null, tint = GrasGroen, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Gezondheidsrapport",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = DonkerGroen
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Diagnose Sectie
                        AnalysisSection(
                            title = "Mogelijke Aandoening",
                            content = res.ziekteNaam,
                            description = res.omschrijving,
                            icon = Icons.Default.BugReport,
                            color = Color(0xFFE57373)
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

                        // Omgevingsfactoren
                        Text(
                            "Verzorgingsanalyse",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = DonkerGroen,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AnalysisChip(Icons.Default.WaterDrop, "Water", res.waterAnalyse, Color(0xFF4FC3F7), Modifier.weight(1f))
                            AnalysisChip(Icons.Default.WbSunny, "Licht", res.lichtAnalyse, Color(0xFFFFD54F), Modifier.weight(1f))
                            AnalysisChip(Icons.Default.Agriculture, "Voeding", res.voedingAnalyse, Color(0xFF81C784), Modifier.weight(1f))
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        if (isPremium) {
                            Surface(
                                color = DonkerGroen.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Lightbulb, null, tint = Color(0xFFFFD600), modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Hersteladvies", fontWeight = FontWeight.ExtraBold, color = DonkerGroen)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(res.advies, style = MaterialTheme.typography.bodyMedium, color = Color.Black.copy(alpha = 0.8f))

                                    res.referentieFoto?.let { foto ->
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("Referentiebeeld van gezonde bladeren:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        AsyncImage(
                                            model = foto,
                                            contentDescription = "Referentie",
                                            modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 8.dp).clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        } else {
                            // Premium Paywall compact
                            Button(
                                onClick = { viewModel.toonPaywall() },
                                colors = ButtonDefaults.buttonColors(containerColor = GrasGroen),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Bekijk volledig hersteladvies")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                TextButton(
                    onClick = { viewModel.reset() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Andere plant analyseren", color = Color.Gray)
                }
            }
        }

        if (state.toonPremiumDialog) {
            val isLadenPremium by instellingenViewModel.isLaden.collectAsState()
            PremiumUpgradeDialog(
                isPremium = isPremium,
                isLaden = isLadenPremium,
                price = instellingenViewModel.getPremiumPrice(),
                onUpgrade = { instellingenViewModel.upgradeToPremium() },
                onRestore = { instellingenViewModel.restorePurchases() },
                onDismiss = { viewModel.sluitPaywall() }
            )
        }
    }
}

@Composable
fun AnalysisSection(
    title: String,
    content: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        }
        Text(content, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DonkerGroen, modifier = Modifier.padding(vertical = 4.dp))
        Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Black.copy(alpha = 0.7f))
    }
}

@Composable
fun AnalysisChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    status: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(status, style = MaterialTheme.typography.labelSmall, color = DonkerGroen, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun VisualScannerOverlay() {
    val infiniteTransition = rememberInfiniteTransition()
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Scanning line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.015f)
                .align(Alignment.TopCenter)
                .offset(y = 250.dp * yOffset)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, GrasGroen, Color.Transparent)
                    )
                )
        )
        
        // Pulse effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(GrasGroen.copy(alpha = 0.1f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(x = 500f, y = 1000f * yOffset)
                    )
                )
        )
    }
}
