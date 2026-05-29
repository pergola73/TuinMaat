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
import com.rvodevelopment.tuinmaat.ui.components.InvoerVeldMetIcoon
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.theme.GrasGroen
import com.rvodevelopment.tuinmaat.ui.theme.ZachtBeige
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
            TopAppBar(
                title = { Text(if (state.plant.firestoreId.isNotEmpty()) "Plant Bewerken" else "Plant Toevoegen", color = DonkerGroen, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ZachtBeige)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Opslaan knop (links)
                FilledIconButton(
                    onClick = { viewModel.savePlant { onSaveSuccess() } },
                    modifier = Modifier.size(56.dp),
                    enabled = state.plant.naam.isNotBlank() && !state.isLaden,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = DonkerGroen.copy(alpha = 0.7f),
                        contentColor = Color.White
                    ),
                    shape = CircleShape
                ) {
                    if (state.isLaden) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = "Opslaan")
                    }
                }

                // Verwijder knop (rechts, alleen bij bewerken)
                if (state.plant.firestoreId.isNotEmpty()) {
                    FilledIconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(56.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = DonkerGroen.copy(alpha = 0.7f),
                            contentColor = Color.White
                        ),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Verwijderen")
                    }
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

            // AI Verrijk Knop (onder de foto)
            if (state.selectedImageBytes != null || state.plant.fotoUri != null) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
                    if (state.isAIBezig) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DonkerGroen.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = DonkerGroen, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("AI haalt plantinfo op...", color = DonkerGroen, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.reIdentify() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DonkerGroen),
                            border = BorderStroke(1.dp, DonkerGroen.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Vul gegevens aan met AI", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                InvoerVeldMetIcoon(
                    label = "Naam",
                    waarde = state.plant.naam,
                    onWaardeChange = { viewModel.updatePlant { p -> p.copy(naam = it) } },
                    icoon = Icons.Default.LocalFlorist,
                    trailingIcon = {
                        IconButton(onClick = { viewModel.identifyByName() }) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "Vul aan met AI",
                                tint = DonkerGroen
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                InvoerVeldMetIcoon(
                    label = "Wetenschappelijke naam",
                    waarde = state.plant.wetenschappelijkeNaam,
                    onWaardeChange = { viewModel.updatePlant { p -> p.copy(wetenschappelijkeNaam = it) } },
                    icoon = Icons.Default.Science
                )

                if (state.plant.bron.isNotEmpty()) {
                    Text(
                        text = "Bron: ${state.plant.bron}",
                        style = MaterialTheme.typography.labelSmall,
                        color = DonkerGroen.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }

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

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Plant Verwijderen") },
                text = { Text("Weet je zeker dat je '${state.plant.naam}' wilt verwijderen? Dit kan niet ongedaan worden gemaakt.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deletePlant { onSaveSuccess() }
                            showDeleteConfirm = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Verwijderen", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Annuleren", color = DonkerGroen)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (state.toonEersteTip) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissEersteTip() },
                title = { Text("Welkom bij TuinMaat! 🌱") },
                text = { Text("Wist je dat je alleen maar een foto hoeft te maken van je plant? De AI vult daarna automatisch alle gegevens voor je in. Super handig toch?") },
                confirmButton = {
                    Button(onClick = { viewModel.dismissEersteTip() }, colors = ButtonDefaults.buttonColors(containerColor = DonkerGroen)) {
                        Text("Top, ga ik doen!")
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (state.toonLocatieTip) {
            AlertDialog(
                onDismissRequest = { viewModel.handleLocatieTipDone(onSaveSuccess) },
                title = { Text("Plant opgeslagen! 🎉") },
                text = { Text("Goed bezig! Je plant is succesvol toegevoegd aan je lijst.") },
                confirmButton = {
                    Button(onClick = { viewModel.handleLocatieTipDone(onSaveSuccess) }, colors = ButtonDefaults.buttonColors(containerColor = DonkerGroen)) {
                        Text("Top!")
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}
