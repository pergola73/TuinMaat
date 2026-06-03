package com.rvodevelopment.tuinmaat.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rvodevelopment.tuinmaat.ui.components.AdBanner
import com.rvodevelopment.tuinmaat.ui.components.NativeAd
import com.rvodevelopment.tuinmaat.ui.components.LocationChip
import com.rvodevelopment.tuinmaat.ui.components.PlantKaart
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.theme.GrasGroen
import com.rvodevelopment.tuinmaat.ui.theme.TuinAchtergrond
import com.rvodevelopment.tuinmaat.ui.theme.neumorphicShadow
import com.rvodevelopment.tuinmaat.ui.viewmodel.PlantenLijstViewModel

@Composable
fun PlantenLijstScherm(
    viewModel: PlantenLijstViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToToevoegen: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    TuinAchtergrond {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 16.dp),
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DonkerGroen)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mijn Planten",
                        style = MaterialTheme.typography.headlineSmall,
                        color = DonkerGroen,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = state.tuinnaam,
                        style = MaterialTheme.typography.bodySmall,
                        color = DonkerGroen.copy(alpha = 0.7f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
                IconButton(onClick = { viewModel.toggleZoekveld() }) {
                    Icon(
                        if (state.isZoekenZichtbaar) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Zoeken",
                        tint = DonkerGroen
                    )
                }
            }

            // Locatie filters
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    LocationChip(
                        selected = state.geselecteerdeLocatie == "Alle",
                        label = "Alle"
                    ) { viewModel.onLocatieSelectie("Alle") }
                }
                items(state.locaties) { loc ->
                    LocationChip(
                        selected = state.geselecteerdeLocatie == loc,
                        label = loc
                    ) { viewModel.onLocatieSelectie(loc) }
                }
            }

            // Neumorphic Zoekbalk
            AnimatedVisibility(
                visible = state.isZoekenZichtbaar,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .neumorphicShadow(shape = RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.6f)
                ) {
                    TextField(
                        value = state.zoekTerm,
                        onValueChange = { viewModel.onZoekTermChange(it) },
                        placeholder = { Text("Zoek op naam...", color = DonkerGroen.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = DonkerGroen) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = DonkerGroen,
                            unfocusedTextColor = DonkerGroen
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lijst met planten
            if (state.gefilterdePlanten.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(state.gefilterdePlanten) { index, plant ->
                        PlantKaart(plant, onNavigateToDetail)
                        
                        if (!state.isPremium && (index == 3)) {
                            Spacer(modifier = Modifier.height(16.dp))
                            NativeAd(adUnitId = com.rvodevelopment.tuinmaat.admobNativeListId)
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (state.planten.isEmpty()) "Je hebt nog geen planten." else "Niets gevonden.",
                            color = DonkerGroen.copy(alpha = 0.5f)
                        )
                        if (state.planten.isEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToToevoegen,
                                colors = ButtonDefaults.buttonColors(containerColor = GrasGroen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(horizontal = 32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Voeg nu je eerste plant toe!", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (!state.isPremium) {
                AdBanner(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}


