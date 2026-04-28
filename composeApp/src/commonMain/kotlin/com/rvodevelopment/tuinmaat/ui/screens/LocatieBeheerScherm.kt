package com.rvodevelopment.tuinmaat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.rvodevelopment.tuinmaat.ui.viewmodel.InstellingenViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocatieBeheerScherm(
    navController: NavController,
    viewModel: InstellingenViewModel = koinInject()
) {
    val userData by viewModel.userData.collectAsState()
    
    var locaties by remember(userData) { mutableStateOf(userData?.locaties ?: listOf("Tuin", "Balkon", "Kas")) }
    var standaardLocatie by remember(userData) { mutableStateOf(userData?.standaardLocatie ?: "Tuin") }
    var nieuweLocatie by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(ZachtBeige).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DonkerGroen)
            }
            Text("Locaties Beheren", style = MaterialTheme.typography.headlineMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = nieuweLocatie,
                    onValueChange = { nieuweLocatie = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nieuwe plek...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DonkerGroen,
                        unfocusedTextColor = DonkerGroen,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = DonkerGroen,
                        unfocusedBorderColor = DonkerGroen.copy(alpha = 0.5f)
                    )
                )
                IconButton(onClick = {
                    if (nieuweLocatie.isNotBlank()) {
                        val updatedLocaties = locaties + nieuweLocatie
                        locaties = updatedLocaties
                        viewModel.updateLocaties(updatedLocaties, standaardLocatie)
                        nieuweLocatie = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Toevoegen", tint = DonkerGroen)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            locaties.forEach { loc ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(12.dp),
                    onClick = { 
                        standaardLocatie = loc 
                        viewModel.updateLocaties(locaties, loc)
                    }
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (loc == standaardLocatie) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (loc == standaardLocatie) Color(0xFFFFD700) else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = loc,
                            modifier = Modifier.weight(1f),
                            fontWeight = if (loc == standaardLocatie) FontWeight.Bold else FontWeight.Normal,
                            color = DonkerGroen
                        )
                        IconButton(onClick = {
                            val updatedLocaties = locaties - loc
                            locaties = updatedLocaties
                            val newStandaard = if (standaardLocatie == loc) "" else standaardLocatie
                            standaardLocatie = newStandaard
                            viewModel.updateLocaties(updatedLocaties, newStandaard)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Verwijderen", tint = Color.Red.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}
