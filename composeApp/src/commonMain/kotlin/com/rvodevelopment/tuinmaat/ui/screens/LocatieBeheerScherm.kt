package com.rvodevelopment.tuinmaat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.rvodevelopment.tuinmaat.ui.components.*
import com.rvodevelopment.tuinmaat.ui.theme.neumorphicShadow

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

    Scaffold(
        topBar = {
            TuinMaatHeader(
                titel = "Locaties",
                subtitel = "Beheer je tuinplekken",
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = ZachtBeige
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = nieuweLocatie,
                    onValueChange = { nieuweLocatie = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nieuwe plek...") },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DonkerGroen,
                        unfocusedTextColor = DonkerGroen,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = DonkerGroen,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                FilledIconButton(
                    onClick = {
                        if (nieuweLocatie.isNotBlank()) {
                            val updatedLocaties = locaties + nieuweLocatie
                            locaties = updatedLocaties
                            viewModel.updateLocaties(updatedLocaties, standaardLocatie)
                            nieuweLocatie = ""
                        }
                    },
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF2D5A36))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Toevoegen", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            locaties.forEach { loc ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = loc,
                            modifier = Modifier.weight(1f),
                            fontWeight = if (loc == standaardLocatie) FontWeight.Bold else FontWeight.Medium,
                            color = if (loc == standaardLocatie) Color(0xFF2D5A36) else DonkerGroen
                        )
                        IconButton(onClick = {
                            val updatedLocaties = locaties - loc
                            locaties = updatedLocaties
                            val newStandaard = if (standaardLocatie == loc) "" else standaardLocatie
                            standaardLocatie = newStandaard
                            viewModel.updateLocaties(updatedLocaties, newStandaard)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Verwijderen", tint = Color.Red.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        }
    }
}
