package com.rvodevelopment.tuinmaat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.rvodevelopment.tuinmaat.ui.viewmodel.InstellingenViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfielBewerkenScherm(
    navController: NavController,
    viewModel: InstellingenViewModel = koinInject()
) {
    val userData by viewModel.userData.collectAsState()
    
    var voornaam by remember(userData) { mutableStateOf(userData?.voornaam ?: "") }
    var achternaam by remember(userData) { mutableStateOf(userData?.achternaam ?: "") }
    var tuinnaam by remember(userData) { mutableStateOf(userData?.tuinnaam ?: "") }
    val isLaden by viewModel.isLaden.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(ZachtBeige).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DonkerGroen)
            }
            Text("Profiel Bewerken", style = MaterialTheme.typography.headlineMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
            OutlinedTextField(
                value = voornaam,
                onValueChange = { voornaam = it },
                label = { Text("Voornaam", color = DonkerGroen) },
                modifier = Modifier.fillMaxWidth(),
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
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = achternaam,
                onValueChange = { achternaam = it },
                label = { Text("Achternaam", color = DonkerGroen) },
                modifier = Modifier.fillMaxWidth(),
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
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = tuinnaam,
                onValueChange = { tuinnaam = it },
                label = { Text("Tuinnaam", color = DonkerGroen) },
                modifier = Modifier.fillMaxWidth(),
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

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.updateProfile(voornaam, achternaam, tuinnaam)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DonkerGroen),
                enabled = !isLaden
            ) {
                if (isLaden) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Opslaan")
            }
        }
    }
}
