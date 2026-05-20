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

                        state.eigenaarNaam?.let { naam ->
                            Surface(
                                modifier = Modifier
                                    .statusBarsPadding()
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp),
                                color = Color.White.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Tuin van $naam",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DonkerGroen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = p.naam,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DonkerGroen,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onNavigateToEdit(p.firestoreId) },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(DonkerGroen.copy(alpha = 0.1f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Bewerken", tint = DonkerGroen, modifier = Modifier.size(20.dp))
                                }
                            }
                            
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
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge, // Zelfde grootte als de gewone tekst
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
            color = DonkerGroen.copy(alpha = 0.2f) // Iets donkerder groen voor meer contrast
        ) {
            Icon(icoon, null, tint = DonkerGroen, modifier = Modifier.padding(10.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = label, 
                style = MaterialTheme.typography.labelSmall, 
                color = DonkerGroen, 
                fontWeight = FontWeight.Bold // Titel nu dikgedrukt
            )
            Text(
                text = if (waarde.isBlank()) "Onbekend" else waarde, 
                style = MaterialTheme.typography.bodyMedium, 
                fontWeight = FontWeight.Normal, // Waarde nu normaal
                color = DonkerGroen.copy(alpha = 0.7f)
            )
        }
    }
}
