package com.rvodevelopment.tuinmaat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rvodevelopment.tuinmaat.getPlatform
import com.rvodevelopment.tuinmaat.PlatformType
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.theme.GrasGroen
import com.rvodevelopment.tuinmaat.ui.theme.ZachtBeige
import com.rvodevelopment.tuinmaat.ui.components.*

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.rvodevelopment.tuinmaat.ui.viewmodel.InstellingenViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeveiligingScherm(
    navController: NavController,
    viewModel: InstellingenViewModel = koinInject()
) {
    val userData by viewModel.userData.collectAsState()
    val isBiometrieIngeschakeld = userData?.biometrieIngeschakeld ?: false
    val isBiometrieBeschikbaar by viewModel.isBiometrieBeschikbaar.collectAsState()
    val isLaden by viewModel.isLaden.collectAsState()
    val platform = getPlatform()
    val biometrieNaam = if (platform == PlatformType.IOS) "FaceID" else "Biometrie"

    Scaffold(
        topBar = {
            TuinMaatHeader(
                titel = "Beveiliging",
                subtitel = "App vergrendeling",
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = ZachtBeige
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            if (isLaden) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DonkerGroen)
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                biometrieNaam,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isBiometrieBeschikbaar) DonkerGroen else Color.Gray
                            )
                            Switch(
                                checked = isBiometrieIngeschakeld,
                                enabled = isBiometrieBeschikbaar,
                                onCheckedChange = { viewModel.updateBiometrie(it) },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = GrasGroen
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBiometrieBeschikbaar) 
                                "Gebruik vingerafdruk of gezichtsherkenning om de app te openen na inactiviteit."
                            else 
                                "Biometrische beveiliging is niet beschikbaar op dit toestel.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isBiometrieBeschikbaar) Color.Gray else Color.Red.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
