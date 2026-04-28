package com.rvodevelopment.tuinmaat.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
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
    val planten by viewModel.planten.collectAsState()

    val maanden = listOf("Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September", "Oktober", "November", "December")
    val currentMoment = kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
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

    Column(modifier = Modifier.fillMaxSize().background(ZachtBeige).statusBarsPadding()) {
        // Header (Titel & Terug knop)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp, 16.dp)) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DonkerGroen)
            }
            Text("Snoei Kalender", style = MaterialTheme.typography.headlineSmall, color = DonkerGroen, fontWeight = FontWeight.ExtraBold)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().navigationBarsPadding(),
            state = listState,
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // We lopen door de 12 maanden heen, beginnend bij de huidige
            gesorteerdeMaanden.forEach { maandNaam ->
                val plantenVoorMaand = planten.filter { it.snoeiMaand.contains(maandNaam, ignoreCase = true) }

                if (plantenVoorMaand.isNotEmpty()) {
                    stickyHeader(key = maandNaam) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ZachtBeige)
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                .neumorphicShadow(shape = RoundedCornerShape(12.dp)),
                            color = Color.White.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = if (maandNaam == maanden[huidigMaandIndex]) "$maandNaam (Nu)" else maandNaam,
                                modifier = Modifier.padding(16.dp, 10.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = DonkerGroen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    items(items = plantenVoorMaand, key = { "${it.firestoreId}-$maandNaam" }) { plant ->
                        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)) {
                            PlantKaart(plant, onNavigateToDetail = {
                                navController.navigate("detail/${plant.firestoreId}")
                            })
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}
