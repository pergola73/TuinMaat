package com.rvodevelopment.tuinmaat.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rvodevelopment.tuinmaat.model.Plant
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.theme.ZachtBeige
import com.rvodevelopment.tuinmaat.ui.theme.neumorphicShadow
import com.rvodevelopment.tuinmaat.ui.viewmodel.PlantDetailViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlantDetailScherm(
    viewModel: PlantDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Plant?>(null) }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = DonkerGroen)
        }
    } else {
        val pagerState = rememberPagerState(initialPage = state.initialIndex, pageCount = { state.planten.size })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            val p = state.planten[page]

            Box(modifier = Modifier.fillMaxSize().background(ZachtBeige)) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

                    // 1. Foto Header
                    Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                        if (p.fotoUri != null) {
                            AsyncImage(
                                model = p.fotoUri,
                                contentDescription = "Plant foto",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(DonkerGroen.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocalFlorist, null, tint = DonkerGroen, modifier = Modifier.size(80.dp))
                            }
                        }

                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.statusBarsPadding().padding(16.dp).background(Color.White.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DonkerGroen)
                        }

                        IconButton(
                            onClick = { showDeleteDialog = p },
                            modifier = Modifier.statusBarsPadding().align(Alignment.TopEnd).padding(16.dp).background(Color.White.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.8f))
                        }

                        if (p.locatie.isNotBlank()) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 16.dp, bottom = 46.dp),
                                color = DonkerGroen.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Place, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(p.locatie, color = Color.White, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }

                    // 2. Informatie Kaart
                    Surface(
                        modifier = Modifier.fillMaxSize().offset(y = (-30).dp),
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        color = ZachtBeige
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(text = p.naam, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = DonkerGroen)
                            
                            if (p.wetenschappelijkeNaam.isNotBlank()) {
                                Text(
                                    text = p.wetenschappelijkeNaam,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = DonkerGroen.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            if (p.omschrijving.isNotBlank()) {
                                Text(
                                    text = p.omschrijving,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DonkerGroen.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            SectionHeader("Verzorging")
                            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                VerzorgingItem(Icons.Default.CalendarMonth, "Snoeimaand", p.snoeiMaand)
                                VerzorgingItem(Icons.Default.ContentCut, "Snoeiadvies", p.snoeiAdvies)
                                VerzorgingItem(Icons.Default.WbSunny, "Licht", p.lichtBehoefte)
                                VerzorgingItem(Icons.Default.WaterDrop, "Water", p.waterBehoefte)
                                VerzorgingItem(Icons.Default.Agriculture, "Voeding", p.voedingAdvies)
                                VerzorgingItem(Icons.Default.ReportProblem, "EHBO", p.ehboSignaal)
                            }

                            Spacer(modifier = Modifier.height(120.dp))
                        }
                    }
                }

                Button(
                    onClick = { onNavigateToEdit(p.firestoreId) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(24.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .neumorphicShadow(shape = RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.9f),
                        contentColor = DonkerGroen
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Gegevens Bewerken", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Plant Verwijderen") },
                text = { Text("Weet je zeker dat je '${showDeleteDialog?.naam}' wilt verwijderen uit je tuin? Dit kan niet ongedaan worden gemaakt.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val plantToDelete = showDeleteDialog ?: return@TextButton
                            viewModel.deletePlant(plantToDelete)
                            showDeleteDialog = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Verwijderen", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Annuleren", color = DonkerGroen)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = DonkerGroen,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun VerzorgingItem(icoon: ImageVector, label: String, waarde: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            color = Color.White.copy(alpha = 0.5f)
        ) {
            Icon(icoon, null, tint = DonkerGroen, modifier = Modifier.padding(10.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = DonkerGroen.copy(alpha = 0.5f))
            Text(if (waarde.isBlank()) "Onbekend" else waarde, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DonkerGroen)
        }
    }
}
