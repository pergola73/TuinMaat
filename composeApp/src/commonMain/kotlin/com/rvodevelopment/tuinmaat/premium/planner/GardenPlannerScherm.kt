package com.rvodevelopment.tuinmaat.premium.planner

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.theme.ZachtBeige

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenPlannerScherm(
    viewModel: GardenPlannerViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("2D Tuintekenaar 📐", color = DonkerGroen, fontWeight = FontWeight.Bold) },
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
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Hier komt de 2D Tuintekenaar", color = DonkerGroen)
                Text("(Onder ontwikkeling)", style = MaterialTheme.typography.labelSmall, color = DonkerGroen.copy(alpha = 0.5f))
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Placeholder voor Canvas
                Surface(
                    modifier = Modifier.size(300.dp),
                    color = DonkerGroen.copy(alpha = 0.05f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DonkerGroen.copy(alpha = 0.1f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Canvas area", color = DonkerGroen.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}
