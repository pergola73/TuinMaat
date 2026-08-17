package com.rvodevelopment.tuinmaat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    var email by remember(userData) { mutableStateOf(userData?.email ?: "") }
    val isLaden by viewModel.isLaden.collectAsState()

    Scaffold(
        topBar = {
            TuinMaatHeader(
                titel = "Profiel",
                subtitel = "Bewerken",
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = ZachtBeige
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp)) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mailadres") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DonkerGroen,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = voornaam,
                onValueChange = { voornaam = it },
                label = { Text("Voornaam") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DonkerGroen,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = achternaam,
                onValueChange = { achternaam = it },
                label = { Text("Achternaam") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DonkerGroen,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = tuinnaam,
                onValueChange = { tuinnaam = it },
                label = { Text("Tuinnaam") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DonkerGroen,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.updateProfile(voornaam, achternaam, tuinnaam, email)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D5A36)),
                enabled = !isLaden
            ) {
                if (isLaden) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                else Text("Opslaan", fontWeight = FontWeight.Bold)
            }
        }
    }
}
