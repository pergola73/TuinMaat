package com.rvodevelopment.tuinmaat.premium.health

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rvodevelopment.tuinmaat.ui.components.*
import com.rvodevelopment.tuinmaat.ui.theme.*
import com.rvodevelopment.tuinmaat.ui.viewmodel.InstellingenViewModel
import org.koin.compose.koinInject

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
            TuinMaatHeader(
                titel = "Dr. Tuinmaat 🩺",
                subtitel = plantName ?: "Diagnose & Gezondheid",
                onBackClick = onNavigateBack
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
            // Informatie kaart
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).neumorphicShadow(shape = RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null, tint = DonkerGroen)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Maak een duidelijke foto van de bladeren voor een nauwkeurige diagnose.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Foto Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .neumorphicShadow(shape = RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (state.geselecteerdeFoto != null) {
                    val saturation by animateFloatAsState(
                        targetValue = if (state.isScanning) 0.3f else 1f,
                        animationSpec = tween(durationMillis = 2000)
                    )
                    
                    AsyncImage(
                        model = state.geselecteerdeFoto,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.colorMatrix(
                            androidx.compose.ui.graphics.ColorMatrix().apply {
                                setToSaturation(saturation)
                            }
                        )
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
                Text("Bezig met visuele scan...", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = GrasGroen)
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), color = GrasGroen)
            } else if (state.isLaden) {
                CircularProgressIndicator(color = DonkerGroen)
            } else if (state.resultaat == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { viewModel.maakFoto(plantName) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DonkerGroen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Camera")
                    }
                    
                    OutlinedButton(
                        onClick = { viewModel.kiesFoto(plantName) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DonkerGroen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Galerij")
                    }
                }
            }

            state.resultaat?.let { res ->
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(500)
                }
                Spacer(modifier = Modifier.height(32.dp))
                
                Surface(
                    color = Color(0xFFD8EED8),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = DonkerGroen)
                        Spacer(Modifier.width(12.dp))
                        Text("Analyse voltooid! 🌱", color = DonkerGroen, fontWeight = FontWeight.Bold)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HealthAndSafety, null, tint = GrasGroen, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Gezondheidsrapport", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = DonkerGroen)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        AnalysisSection(
                            title = "Diagnose",
                            content = res.ziekteNaam,
                            description = res.omschrijving,
                            icon = Icons.Default.BugReport,
                            color = Color(0xFFE57373)
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AnalysisChip(Icons.Default.WaterDrop, "Water", res.waterAnalyse, Color(0xFF4FC3F7), Modifier.weight(1f))
                            AnalysisChip(Icons.Default.WbSunny, "Licht", res.lichtAnalyse, Color(0xFFFFD54F), Modifier.weight(1f))
                            AnalysisChip(Icons.Default.Agriculture, "Voeding", res.voedingAnalyse, Color(0xFF81C784), Modifier.weight(1f))
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        if (isPremium) {
                            Surface(color = Color(0xFFF7F9F6), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Hersteladvies", fontWeight = FontWeight.ExtraBold, color = DonkerGroen)
                                    Text(res.advies, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.toonPaywall() },
                                colors = ButtonDefaults.buttonColors(containerColor = GrasGroen),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Bekijk hersteladvies")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                TextButton(onClick = { viewModel.reset() }, modifier = Modifier.fillMaxWidth()) {
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
fun AnalysisSection(title: String, content: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
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
fun AnalysisChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, status: String, color: Color, modifier: Modifier = Modifier) {
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            Text(status, style = MaterialTheme.typography.labelSmall, color = DonkerGroen, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun VisualScannerOverlay() {
    val infiniteTransition = rememberInfiniteTransition()
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Reverse)
    )
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.015f).align(Alignment.TopCenter).offset(y = 250.dp * yOffset)
                .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color(0xFF4CAF50), Color.Transparent)))
        )
    }
}
