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
import com.rvodevelopment.tuinmaat.ui.components.InvoerVeldMetIcoon
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.theme.GrasGroen
import com.rvodevelopment.tuinmaat.ui.theme.ZachtBeige
import com.rvodevelopment.tuinmaat.ui.viewmodel.PlantToevoegenViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
fun PlantToevoegenScherm(
    viewModel: PlantToevoegenViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val maandenLijst = listOf("Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September", "Oktober", "November", "December")
    
    var laatLocatieMenuZien by remember { mutableStateOf(false) }
    var laatFotoMenuZien by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ZachtBeige,
        topBar = {
            TopAppBar(
                title = { Text(if (state.plant.firestoreId.isNotEmpty()) "Plant Bewerken" else "Plant Toevoegen", color = DonkerGroen, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ZachtBeige)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                Button(
                    onClick = { viewModel.savePlant { onNavigateBack() } },
                    enabled = state.plant.naam.isNotBlank() && !state.isLaden,
                    colors = ButtonDefaults.buttonColors(containerColor = DonkerGroen),
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isLaden) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Opslaan in Collectie", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(scrollState).background(ZachtBeige)
        ) {
            // Foto Sectie
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color.LightGray)
                    .clickable { laatFotoMenuZien = true },
                contentAlignment = Alignment.Center
            ) {
                if (state.selectedImageBytes != null) {
                    AsyncImage(
                        model = state.selectedImageBytes,
                        contentDescription = "Geselecteerde plant foto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (state.plant.fotoUri != null) {
                    AsyncImage(
                        model = state.plant.fotoUri,
                        contentDescription = "Plant foto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (state.isAIBezig) {
                            CircularProgressIndicator(color = DonkerGroen)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Plant herkennen...", color = DonkerGroen)
                        } else {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                            Text("Tik om foto toe te voegen", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                // Knoppen voor Camera en Galerij over de foto heen
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledIconButton(
                        onClick = { viewModel.takePhoto() },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = DonkerGroen.copy(alpha = 0.7f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Camera")
                    }
                    FilledIconButton(
                        onClick = { viewModel.pickImage() },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = DonkerGroen.copy(alpha = 0.7f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Galerij")
                    }
                }
            }

            if (laatFotoMenuZien) {
                ModalBottomSheet(onDismissRequest = { laatFotoMenuZien = false }) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text("Foto toevoegen", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        ListItem(
                            headlineContent = { Text("Camera") },
                            leadingContent = { Icon(Icons.Default.PhotoCamera, null) },
                            modifier = Modifier.clickable {
                                viewModel.takePhoto()
                                laatFotoMenuZien = false
                            }
                        )
                        ListItem(
                            headlineContent = { Text("Galerij") },
                            leadingContent = { Icon(Icons.Default.PhotoLibrary, null) },
                            modifier = Modifier.clickable {
                                viewModel.pickImage()
                                laatFotoMenuZien = false
                            }
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                InvoerVeldMetIcoon(
                    label = "Naam",
                    waarde = state.plant.naam,
                    onWaardeChange = { viewModel.updatePlant { p -> p.copy(naam = it) } },
                    icoon = Icons.Default.LocalFlorist
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Locatie Selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = DonkerGroen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Locatie", style = MaterialTheme.typography.bodyMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = laatLocatieMenuZien,
                        onExpandedChange = { laatLocatieMenuZien = !laatLocatieMenuZien }
                    ) {
                        OutlinedTextField(
                            value = state.plant.locatie,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = laatLocatieMenuZien) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = DonkerGroen,
                                unfocusedBorderColor = DonkerGroen.copy(alpha = 0.5f)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = laatLocatieMenuZien,
                            onDismissRequest = { laatLocatieMenuZien = false }
                        ) {
                            state.beschikbareLocaties.forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text(loc) },
                                    onClick = {
                                        viewModel.updatePlant { it.copy(locatie = loc) }
                                        laatLocatieMenuZien = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                InvoerVeldMetIcoon("Omschrijving", state.plant.omschrijving, { viewModel.updatePlant { p -> p.copy(omschrijving = it) } }, Icons.Default.Info, isMultiLine = true)
                
                // Snoeimaand Chips
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = DonkerGroen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Beste snoeimaand", style = MaterialTheme.typography.labelLarge, color = DonkerGroen, fontWeight = FontWeight.Bold)
                }
                
                FlowRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    maandenLijst.forEach { maand ->
                        FilterChip(
                            selected = state.geselecteerdeMaanden.contains(maand),
                            onClick = { viewModel.toggleMaand(maand) },
                            label = { Text(maand) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GrasGroen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                
                InvoerVeldMetIcoon("Snoeiadvies", state.plant.snoeiAdvies, { viewModel.updatePlant { p -> p.copy(snoeiAdvies = it) } }, Icons.Default.ContentCut, isMultiLine = true)
                InvoerVeldMetIcoon("Lichtbehoefte", state.plant.lichtBehoefte, { viewModel.updatePlant { p -> p.copy(lichtBehoefte = it) } }, Icons.Default.WbSunny)
                InvoerVeldMetIcoon("Waterbehoefte", state.plant.waterBehoefte, { viewModel.updatePlant { p -> p.copy(waterBehoefte = it) } }, Icons.Default.WaterDrop)
                InvoerVeldMetIcoon("Voedingsadvies", state.plant.voedingAdvies, { viewModel.updatePlant { p -> p.copy(voedingAdvies = it) } }, Icons.Default.Agriculture)
                InvoerVeldMetIcoon("Bemesting", state.plant.bemesting, { viewModel.updatePlant { p -> p.copy(bemesting = it) } }, Icons.Outlined.Spa)
                InvoerVeldMetIcoon("EHBO Signaal", state.plant.ehboSignaal, { viewModel.updatePlant { p -> p.copy(ehboSignaal = it) } }, Icons.Default.ReportProblem)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
