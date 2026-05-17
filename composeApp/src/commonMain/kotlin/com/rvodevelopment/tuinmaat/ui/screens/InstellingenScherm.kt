package com.rvodevelopment.tuinmaat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.theme.ZachtBeige
import com.rvodevelopment.tuinmaat.ui.viewmodel.InstellingenViewModel
import org.koin.compose.koinInject

@Composable
fun InstellingenScherm(
    navController: NavController,
    viewModel: InstellingenViewModel = koinInject()
) {
    val userData by viewModel.userData.collectAsState()
    var toonVerwijderDialoog by remember { mutableStateOf(false) }
    var toonBevestigDialoog by remember { mutableStateOf(false) }
    var geselecteerdeReden by remember { mutableStateOf("") }
    val redenen = listOf(
        "Ik gebruik de app niet meer",
        "De app mist functies die ik nodig heb",
        "Ik heb een nieuw account aangemaakt",
        "Ik maak me zorgen over mijn privacy",
        "Andere reden"
    )

    Column(modifier = Modifier.fillMaxSize().background(ZachtBeige).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = { navController.popBackStack() }) { 
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DonkerGroen) 
            }
            Text("Instellingen", style = MaterialTheme.typography.headlineMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            InstellingItem("Profiel bewerken", Icons.Default.Person) { navController.navigate("profiel_bewerken") }
            InstellingItem("Tuin delen", Icons.Default.Share) { navController.navigate("tuin_delen") }
            InstellingItem("Locaties beheren", Icons.Default.Place) { navController.navigate("locatiebeheer") }
            InstellingItem("Beveiliging", Icons.Default.Security) { navController.navigate("beveiliging") }
            InstellingItem("Info", Icons.Default.Info) { navController.navigate("info") }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    navController.navigate("login") { 
                        popUpTo(0)
                        launchSingleTop = true
                    }
                    viewModel.signOut()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.5f), contentColor = DonkerGroen)
            ) {
                Text("Uitloggen")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { toonVerwijderDialoog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.7f))
            ) {
                Text("Account verwijderen")
            }
        }
    }

    if (toonVerwijderDialoog) {
        AlertDialog(
            onDismissRequest = { toonVerwijderDialoog = false },
            title = { Text("Account verwijderen?", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("We vinden het jammer dat je gaat. Je account en alle geregistreerde gegevens (planten, foto's, locaties) zullen definitief worden verwijderd. Dit kan niet ongedaan worden gemaakt.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Waarom wil je je account verwijderen?", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    
                    redenen.forEach { reden ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = geselecteerdeReden == reden,
                                onClick = { geselecteerdeReden = reden }
                            )
                            Text(reden, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        toonVerwijderDialoog = false
                        toonBevestigDialoog = true
                    },
                    enabled = geselecteerdeReden.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Verwijderen")
                }
            },
            dismissButton = {
                TextButton(onClick = { toonVerwijderDialoog = false }) {
                    Text("Annuleren", color = Color.Gray)
                }
            }
        )
    }

    if (toonBevestigDialoog) {
        AlertDialog(
            onDismissRequest = { toonBevestigDialoog = false },
            title = { Text("Weet je het écht zeker?", fontWeight = FontWeight.Bold) },
            text = { Text("Deze actie kan niet ongedaan worden gemaakt. Al je planten en tuinhistorie worden definitief gewist.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount(geselecteerdeReden) {
                            navController.navigate("login") { popUpTo(0) }
                        }
                        toonBevestigDialoog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Ja, verwijder alles")
                }
            },
            dismissButton = {
                TextButton(onClick = { toonBevestigDialoog = false }) {
                    Text("Annuleren")
                }
            }
        )
    }
}

@Composable
fun InstellingItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = DonkerGroen)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge, color = DonkerGroen)
        }
    }
}

@Composable
fun InfoScherm(
    navController: NavController,
    viewModel: InstellingenViewModel = koinInject()
) {
    val userData by viewModel.userData.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(ZachtBeige).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = { navController.popBackStack() }) { 
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DonkerGroen) 
            }
            Text("Informatie", style = MaterialTheme.typography.headlineMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
        }

        Card(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Park, contentDescription = null, tint = DonkerGroen, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("TuinMaat", style = MaterialTheme.typography.headlineSmall, color = DonkerGroen, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Versie 2.0.0 (KMP)", style = MaterialTheme.typography.bodyMedium)
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = DonkerGroen.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                Text("${userData?.voornaam ?: ""} ${userData?.achternaam ?: ""}", style = MaterialTheme.typography.titleMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                InfoRow("E-mail", userData?.email ?: "Laden...")
                InfoRow("User ID", userData?.id ?: "Laden...")
                InfoRow("Tuin ID", userData?.sharedGardenId ?: userData?.id ?: "Laden...")
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = DonkerGroen.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                Text("Support", style = MaterialTheme.typography.titleMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("Copyright", "rvodevelopment")
                InfoRow("Support", "rvanoel@etik.com")
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
