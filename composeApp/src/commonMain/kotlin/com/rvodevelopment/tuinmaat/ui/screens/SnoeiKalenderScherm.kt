package com.rvodevelopment.tuinmaat.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rvodevelopment.tuinmaat.ui.components.LocationChip
import com.rvodevelopment.tuinmaat.ui.components.NativeAd
import com.rvodevelopment.tuinmaat.ui.components.PlantKaart
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.theme.GrasGroen
import com.rvodevelopment.tuinmaat.ui.theme.TuinAchtergrond
import com.rvodevelopment.tuinmaat.ui.theme.ZachtBeige
import com.rvodevelopment.tuinmaat.ui.viewmodel.SnoeiKalenderViewModel
import com.rvodevelopment.tuinmaat.ui.theme.neumorphicShadow
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalFoundationApi::class, ExperimentalTime::class)
@Composable
fun SnoeiKalenderScherm(
    navController: NavController,
    viewModel: SnoeiKalenderViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    val maanden = listOf("Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September", "Oktober", "November", "December")
    val currentMoment = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val huidigMaandIndex = currentMoment.monthNumber - 1 // 1-12 to 0-11

    // Sorteer de maanden zodat de huidige maand bovenaan staat en we precies 1 jaar tonen
    val gesorteerdeMaanden = remember(huidigMaandIndex) {
        val list = mutableListOf<String>()
        for (i in 0 until 12) {
            list.add(maanden[(huidigMaandIndex + i) % 12])
        }
        list
    }

    val listState = rememberLazyListState()

    TuinAchtergrond {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // Header (Titel & Terug knop)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp, 16.dp)) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DonkerGroen)
                }
                Column {
                    Text(
                        text = "Snoei Kalender",
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

            LazyColumn(
                modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                state = listState,
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                var totaalPlantenTeler = 0
                // We lopen door de 12 maanden heen, beginnend bij de huidige
                gesorteerdeMaanden.forEach { maandNaam ->
                    val plantenVoorMaand = state.gefilterdePlanten.filter { it.snoeiMaand.contains(maandNaam, ignoreCase = true) }

                    if (plantenVoorMaand.isNotEmpty()) {
                        stickyHeader(key = maandNaam) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                                    .neumorphicShadow(shape = RoundedCornerShape(12.dp)),
                                color = GrasGroen,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (maandNaam == maanden[huidigMaandIndex]) "$maandNaam (Nu)" else maandNaam,
                                    modifier = Modifier.padding(16.dp, 10.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFF5F5F0),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        itemsIndexed(items = plantenVoorMaand, key = { _, plant -> "${plant.firestoreId}-$maandNaam" }) { _, plant ->
                            totaalPlantenTeler++
                            Column {
                                Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)) {
                                    PlantKaart(plant, onNavigateToDetail = {
                                        navController.navigate("detail/${plant.firestoreId}")
                                    })
                                }

                                if (!state.isPremium && totaalPlantenTeler == 3) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    NativeAd(
                                        adUnitId = com.rvodevelopment.tuinmaat.admobNativeSnoeiId,
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                                        isMedium = true
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}
