package com.rvodevelopment.tuinmaat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.rvodevelopment.tuinmaat.ui.theme.GrasGroen
import com.rvodevelopment.tuinmaat.ui.theme.ZachtBeige

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
    val isLaden by viewModel.isLaden.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(ZachtBeige).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DonkerGroen)
            }
            Text("Beveiliging", style = MaterialTheme.typography.headlineMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
        }

        if (isLaden) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DonkerGroen)
            }
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Biometrische beveiliging",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = DonkerGroen
                    )
                    Switch(
                        checked = isBiometrieIngeschakeld,
                        onCheckedChange = { ingeschakeld ->
                            viewModel.updateBiometrie(ingeschakeld)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GrasGroen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Gebruik vingerafdruk of gezichtsherkenning om de app te openen bij inactiviteit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DonkerGroen.copy(alpha = 0.6f)
                )
            }
        }
    }
}
