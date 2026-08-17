package com.rvodevelopment.tuinmaat.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rvodevelopment.tuinmaat.ui.components.*
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.theme.GrasGroen
import com.rvodevelopment.tuinmaat.ui.theme.TuinAchtergrond
import com.rvodevelopment.tuinmaat.ui.theme.ZachtBeige
import com.rvodevelopment.tuinmaat.ui.theme.neumorphicShadow
import com.rvodevelopment.tuinmaat.ui.viewmodel.PlantToevoegenViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
fun PlantToevoegenScherm(
    viewModel: PlantToevoegenViewModel,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit = onNavigateBack
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val maandenLijst = listOf("Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September", "Oktober", "November", "December")
    
    var laatLocatieMenuZien by remember { mutableStateOf(false) }
    var laatFotoMenuZien by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.infoBericht) {
        state.infoBericht?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        containerColor = ZachtBeige,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TuinMaatHeader(
                titel = if (state.plant.firestoreId.isNotEmpty()) "Bewerken" else "Toevoegen",
                subtitel = state.eigenaarNaam?.let { "In tuin van $it" } ?: "Nieuwe Plant",
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.plant.firestoreId.isNotEmpty()) {
                        FilledIconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color.Red)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Verwijderen")
                        }
                    }

                    Button(
                        onClick = { if (state.plant.naam.isNotBlank() && !state.isLaden) viewModel.savePlant { onSaveSuccess() } },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D5A36)),
                        enabled = state.plant.naam.isNotBlank() && !state.isLaden
                    ) {
                        if (state.isLaden) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Plant Opslaan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(scrollState)
        ) {
            // Foto Sectie
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .clickable { laatFotoMenuZien = true },
                contentAlignment = Alignment.Center
            ) {
                if (state.selectedImageBytes != null) {
                    AsyncImage(
                        model = state.selectedImageBytes,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (state.plant.fotoUri != null) {
                    AsyncImage(
                        model = state.plant.fotoUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Text("Tik om foto toe te voegen", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    }
                }

                Row(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledIconButton(onClick = { viewModel.takePhoto() }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF2D5A36))) {
                        Icon(Icons.Default.PhotoCamera, null, tint = Color.White)
                    }
                    FilledIconButton(onClick = { viewModel.pickImage() }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF2D5A36))) {
                        Icon(Icons.Default.PhotoLibrary, null, tint = Color.White)
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                if (state.isAIBezig) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), color = Color(0xFF2D5A36))
                }

                InvoerVeldMetIcoon(
                    label = "Naam",
                    waarde = state.plant.naam,
                    onWaardeChange = { viewModel.updatePlant { p -> p.copy(naam = it) } },
                    icoon = Icons.Default.LocalFlorist,
                    trailingIcon = {
                        IconButton(onClick = { viewModel.identifyByName() }) {
                            Icon(Icons.Default.AutoAwesome, "AI", tint = Color(0xFF2D5A36))
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Locatie Selector
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFF2D5A36), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Locatie", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color(0xFF2D5A36))
                    }
                    ExposedDropdownMenuBox(
                        expanded = laatLocatieMenuZien,
                        onExpandedChange = { laatLocatieMenuZien = !laatLocatieMenuZien },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = state.plant.locatie,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = laatLocatieMenuZien) },
                            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2D5A36), unfocusedBorderColor = Color.LightGray)
                        )
                        ExposedDropdownMenu(expanded = laatLocatieMenuZien, onDismissRequest = { laatLocatieMenuZien = false }) {
                            state.beschikbareLocaties.forEach { loc ->
                                DropdownMenuItem(text = { Text(loc) }, onClick = { viewModel.updatePlant { it.copy(locatie = loc) }; laatLocatieMenuZien = false })
                            }
                        }
                    }
                }

                InvoerVeldMetIcoon("Wetenschappelijk", state.plant.wetenschappelijkeNaam, { viewModel.updatePlant { p -> p.copy(wetenschappelijkeNaam = it) } }, Icons.Default.Science)
                InvoerVeldMetIcoon("Notitie", state.plant.persoonlijkeNotitie, { viewModel.updatePlant { p -> p.copy(persoonlijkeNotitie = it) } }, Icons.Default.EditNote, isMultiLine = true)

                Spacer(modifier = Modifier.height(16.dp))
                Text("Snoei informatie", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color(0xFF2D5A36))
                FlowRow(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    maandenLijst.forEach { maand ->
                        FilterChip(
                            selected = state.geselecteerdeMaanden.contains(maand),
                            onClick = { viewModel.toggleMaand(maand) },
                            label = { Text(maand) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF2D5A36), selectedLabelColor = Color.White)
                        )
                    }
                }
                
                InvoerVeldMetIcoon("Snoeiadvies", state.plant.snoeiAdvies, { viewModel.updatePlant { p -> p.copy(snoeiAdvies = it) } }, Icons.Default.ContentCut, isMultiLine = true)
                InvoerVeldMetIcoon("Water", state.plant.waterBehoefte, { viewModel.updatePlant { p -> p.copy(waterBehoefte = it) } }, Icons.Default.WaterDrop)
                InvoerVeldMetIcoon("Licht", state.plant.lichtBehoefte, { viewModel.updatePlant { p -> p.copy(lichtBehoefte = it) } }, Icons.Default.WbSunny)

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Verwijderen") },
                text = { Text("Weet je zeker dat je '${state.plant.naam}' wilt verwijderen?") },
                confirmButton = { TextButton(onClick = { viewModel.deletePlant { onSaveSuccess() }; showDeleteConfirm = false }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) { Text("Verwijderen") } },
                dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Annuleren") } }
            )
        }
    }
}
