package com.rvodevelopment.tuinmaat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val code = joinCode.substringAfter("gardenId=").trim()
                    viewModel.joinGardenManually(code)
                    showJoinDialog = false
                    joinCode = ""
                }) {
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

    Column(modifier = Modifier.fillMaxSize().background(ZachtBeige).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DonkerGroen)
            }
            Text("Tuin Delen", style = MaterialTheme.typography.headlineMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
            Text("Samen tuinieren", style = MaterialTheme.typography.titleLarge, color = DonkerGroen, fontWeight = FontWeight.Bold)
            Text(
                "Stuur een uitnodiging naar iemand anders om samen in jouw tuin te werken.",
                style = MaterialTheme.typography.bodyMedium,
                color = DonkerGroen.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.shareInvitation() },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DonkerGroen)
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
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DonkerGroen))
            ) {
                Text("Ik heb een code ontvangen", color = DonkerGroen, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(48.dp))
            HorizontalDivider(color = DonkerGroen.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(32.dp))

            if (isGekoppeld) {
                Text(
                    "Je bent momenteel gekoppeld aan een gedeelde tuin. Je kunt het delen op elk moment stoppen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DonkerGroen.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { viewModel.unlinkGarden() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text("Stop met delen", fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    "Je beheert momenteel je eigen tuin. Zodra je een uitnodiging van iemand anders accepteert, kun je hier ook hun tuin zien.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DonkerGroen.copy(alpha = 0.6f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}
