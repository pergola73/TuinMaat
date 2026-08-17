package com.rvodevelopment.tuinmaat.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun TuinDelenScherm(
    navController: NavController,
    viewModel: InstellingenViewModel = koinInject()
) {
    val userData by viewModel.userData.collectAsState()
    val viewersData by viewModel.viewersData.collectAsState()
    val isGekoppeld = userData?.sharedGardenId != null
    var showJoinDialog by remember { mutableStateOf(false) }
    var joinCode by remember { mutableStateOf("") }

    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("Koppel een tuin") },
            text = {
                Column {
                    Text("Plak hier de code die je van de andere tuinder hebt ontvangen.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = joinCode,
                        onValueChange = { joinCode = it },
                        label = { Text("Tuin Code") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val code = joinCode.substringAfter("gardenId=").trim()
                    viewModel.joinGardenManually(code)
                    showJoinDialog = false
                    joinCode = ""
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D5A36))) {
                    Text("Koppelen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("Annuleren")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TuinMaatHeader(
                titel = "Tuin Delen",
                subtitel = "Samen tuinieren",
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = ZachtBeige
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp)) {
            Text(
                "Nodig iemand uit om mee te helpen in jouw tuin of sluit je aan bij een andere tuin.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Button(
                onClick = { viewModel.shareInvitation() },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D5A36))
            ) {
                Icon(Icons.Default.Share, null, tint = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Deel mijn Tuin Code", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showJoinDialog = true },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF2D5A36))
            ) {
                Text("Ik heb een code ontvangen", color = Color(0xFF2D5A36), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(48.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(32.dp))

            if (isGekoppeld) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Je bent momenteel gekoppeld aan een gedeelde tuin.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { viewModel.unlinkGarden() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
                        ) {
                            Text("Stop met delen", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Text(
                    "Je beheert momenteel je eigen tuin. Zodra je een uitnodiging accepteert, kun je hier ook hun tuin zien.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}
