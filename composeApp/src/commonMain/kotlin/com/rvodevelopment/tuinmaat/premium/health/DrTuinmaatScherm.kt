package com.rvodevelopment.tuinmaat.premium.health

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                    .height(200.dp)
                    .background(DonkerGroen.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (state.geselecteerdeFoto != null) {
                    AsyncImage(
                        model = state.geselecteerdeFoto,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
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

            if (state.isLaden) {
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(color = DonkerGroen)
                Text("Analyseert ziektebeeld...", modifier = Modifier.padding(top = 8.dp), color = DonkerGroen)
            }

            state.error?.let {
                Spacer(modifier = Modifier.height(24.dp))
                Text(it, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
            }

            state.resultaat?.let { res ->
                Spacer(modifier = Modifier.height(32.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BugReport, null, tint = DonkerGroen)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Diagnose: ${res.ziekteNaam}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = DonkerGroen
                            )
                        }

                        if (res.eppoCode.isNotEmpty()) {
                            Text(
                                text = "EPPO Code: ${res.eppoCode}",
                                style = MaterialTheme.typography.labelSmall,
                                color = DonkerGroen.copy(alpha = 0.5f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(res.omschrijving, style = MaterialTheme.typography.bodyMedium)
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        if (isPremium) {
                            Text("Hersteladvies:", fontWeight = FontWeight.Bold, color = GrasGroen)
                            Text(res.advies, style = MaterialTheme.typography.bodyMedium)

                            res.referentieFoto?.let { foto ->
                                Spacer(modifier = Modifier.height(20.dp))
                                Text("Referentiebeeld:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                AsyncImage(
                                    model = foto,
                                    contentDescription = "Referentie",
                                    modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 4.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        } else {
                            // Premium Paywall Card voor Hersteladvies
                            Surface(
                                color = GrasGroen.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, GrasGroen.copy(alpha = 0.2f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = GrasGroen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Hersteladvies Vergrendeld",
                                        fontWeight = FontWeight.Bold,
                                        color = DonkerGroen
                                    )
                                    Text(
                                        "Upgrade naar Premium om het volledige stappenplan te zien en je plant te redden.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DonkerGroen.copy(alpha = 0.7f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                    Button(
                                        onClick = { viewModel.toonPaywall() },
                                        colors = ButtonDefaults.buttonColors(containerColor = GrasGroen),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Bekijk Hersteladvies")
                                    }
                                }
                            }
                        }
                    }
                }

                if (isPremium && state.historie.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        "Medisch Dossier (Historie)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DonkerGroen,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    state.historie.forEach { diagnosis ->
                        val date = Instant.fromEpochMilliseconds(diagnosis.timestamp)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                        val dateStr = "${date.dayOfMonth}-${date.monthNumber}-${date.year}"
                        
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.History, null, tint = DonkerGroen.copy(alpha = 0.5f))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(diagnosis.ziekteNaam, fontWeight = FontWeight.Bold)
                                    Text(dateStr, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.reset() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Nieuwe check")
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
