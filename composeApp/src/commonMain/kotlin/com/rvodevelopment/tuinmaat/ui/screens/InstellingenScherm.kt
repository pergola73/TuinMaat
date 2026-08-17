package com.rvodevelopment.tuinmaat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.rvodevelopment.tuinmaat.getPlatform
import com.rvodevelopment.tuinmaat.PlatformType
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.theme.TuinAchtergrond
import com.rvodevelopment.tuinmaat.ui.theme.ZachtBeige
import com.rvodevelopment.tuinmaat.ui.theme.neumorphicShadow
import com.rvodevelopment.tuinmaat.ui.components.*
import com.rvodevelopment.tuinmaat.ui.viewmodel.InstellingenViewModel
import com.rvodevelopment.tuinmaat.appVersion
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstellingenScherm(
    navController: NavController,
    viewModel: InstellingenViewModel = koinInject(),
) {
    val isPremium by viewModel.isPremium.collectAsState()
    val foutMelding by viewModel.foutMelding.collectAsState()
    val isLaden by viewModel.isLaden.collectAsState()
    var toonVerwijderDialoog by remember { mutableStateOf(value = false) }
    var toonBevestigDialoog by remember { mutableStateOf(value = false) }
    var toonPremiumDialoog by remember { mutableStateOf(value = false) }
    var geselecteerdeReden by remember { mutableStateOf("") }
    val redenen = listOf(
        "Ik gebruik de app niet meer",
        "De app mist functies die ik nodig heb",
        "Ik heb een nieuw account aangemaakt",
        "Andere reden",
    )

    Scaffold(
        topBar = {
            TuinMaatHeader(
                titel = "Instellingen",
                subtitel = "Beheer je account",
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = ZachtBeige
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp)) {
            if (foutMelding != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = foutMelding!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            if (!isPremium) {
                Surface(
                    onClick = { toonPremiumDialoog = true },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF2D5A36), // BotanischGroen
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, tint = Color.Yellow)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Word Premium", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text("Geen advertenties & alle functies", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            InstellingItem("Profiel bewerken", Icons.Default.Person) { navController.navigate("profiel_bewerken") }
            InstellingItem("Tuin delen", Icons.Default.Share) { navController.navigate("tuin_delen") }
            InstellingItem("Locaties beheren", Icons.Default.Place) { navController.navigate("locatiebeheer") }
            InstellingItem("Beveiliging", Icons.Default.Security) { navController.navigate("beveiliging") }
            InstellingItem("Informatie", Icons.Default.Info) { navController.navigate("info") }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    navController.navigate("login") { popUpTo(0) }
                    viewModel.signOut()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = DonkerGroen)
            ) {
                Text("Uitloggen", fontWeight = FontWeight.Bold)
            }

            TextButton(
                onClick = { toonVerwijderDialoog = true },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.6f))
            ) {
                Text("Account verwijderen")
            }
        }
    }

    if (toonPremiumDialoog) {
        PremiumUpgradeDialog(
            isPremium = isPremium,
            isLaden = isLaden,
            price = viewModel.getPremiumPrice(),
            onUpgrade = { viewModel.upgradeToPremium() },
            onRestore = { viewModel.restorePurchases() },
            onDismiss = { toonPremiumDialoog = false }
        )
    }

    if (toonVerwijderDialoog) {
        AlertDialog(
            onDismissRequest = { toonVerwijderDialoog = false },
            title = { Text("Account verwijderen?") },
            text = {
                Column {
                    Text("Weet je zeker dat je je account wilt verwijderen? Al je planten en foto's gaan verloren.")
                    Spacer(modifier = Modifier.height(16.dp))
                    redenen.forEach { reden ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = geselecteerdeReden == reden, onClick = { geselecteerdeReden = reden })
                            Text(reden, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { toonVerwijderDialoog = false; toonBevestigDialoog = true }, enabled = geselecteerdeReden.isNotEmpty(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Verwijderen")
                }
            },
            dismissButton = { TextButton(onClick = { toonVerwijderDialoog = false }) { Text("Annuleren") } }
        )
    }

    if (toonBevestigDialoog) {
        AlertDialog(
            onDismissRequest = { toonBevestigDialoog = false },
            title = { Text("Écht zeker?") },
            confirmButton = {
                Button(onClick = { viewModel.deleteAccount(geselecteerdeReden) { navController.navigate("login") { popUpTo(0) } } }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Ja, verwijder definitief")
                }
            },
            dismissButton = { TextButton(onClick = { toonBevestigDialoog = false }) { Text("Annuleren") } }
        )
    }
}

@Composable
fun InstellingItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(64.dp).neumorphicShadow(shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Row(modifier = Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color(0xFF2D5A36))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge, color = DonkerGroen, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray.copy(alpha = 0.4f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScherm(
    navController: NavController,
    viewModel: InstellingenViewModel = koinInject()
) {
    val userData by viewModel.userData.collectAsState()
    var tapCount by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TuinMaatHeader(
                titel = "Informatie",
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = ZachtBeige
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth().neumorphicShadow(shape = RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.clickable { tapCount++; if(tapCount>=5) { viewModel.devResetPremium(); tapCount=0 } }) {
                        TuinMaatLogo(modifier = Modifier.size(80.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("TuinMaat", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Versie $appVersion", color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(24.dp))

                    InfoRow("Gebruiker", "${userData?.voornaam} ${userData?.achternaam}")
                    InfoRow("E-mail", userData?.email ?: "")
                    InfoRow("Tuin ID", userData?.sharedGardenId ?: userData?.id ?: "")
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Support: rvanoel@etik.com", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = DonkerGroen)
    }
}
