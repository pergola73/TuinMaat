package com.rvodevelopment.tuinmaat.premium.planner

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rvodevelopment.tuinmaat.ui.components.*
import com.rvodevelopment.tuinmaat.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenPlannerScherm(
    viewModel: GardenPlannerViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TuinMaatHeader(
                titel = "Tuinontwerper",
                subtitel = "2D Plattegrond",
                onBackClick = onNavigateBack,
                extraContent = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = { viewModel.reset() }) {
                            Icon(Icons.Default.DeleteSweep, "Reset", tint = Color.White)
                        }
                    }
                }
            )
        },
        containerColor = ZachtBeige
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            // 1. Garden Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val xMeters = offset.x / state.scale
                            val yMeters = offset.y / state.scale
                            viewModel.placePlant(xMeters, yMeters)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    for (i in 0..(size.width / state.scale).toInt()) {
                        drawLine(
                            color = Color(0xFF2D5A36).copy(alpha = 0.1f),
                            start = Offset(i * state.scale, 0f),
                            end = Offset(i * state.scale, size.height),
                            strokeWidth = 1f
                        )
                    }
                    for (i in 0..(size.height / state.scale).toInt()) {
                        drawLine(
                            color = Color(0xFF2D5A36).copy(alpha = 0.1f),
                            start = Offset(0f, i * state.scale),
                            end = Offset(size.width, i * state.scale),
                            strokeWidth = 1f
                        )
                    }
                }

                state.placedPlanten.forEach { placed ->
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (placed.x * state.scale).dp - 20.dp,
                                y = (placed.y * state.scale).dp - 20.dp
                            )
                            .size(40.dp)
                            .neumorphicShadow(shape = CircleShape)
                            .background(Color.White, CircleShape)
                            .clickable { viewModel.removePlacedPlant(placed.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (placed.plant.fotoUri != null) {
                            AsyncImage(
                                model = placed.plant.fotoUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.LocalFlorist, null, tint = Color(0xFF2D5A36), modifier = Modifier.size(24.dp))
                        }
                    }
                }
                
                if (state.placedPlanten.isEmpty() && state.selectedPlant == null) {
                    Text(
                        "Tik onderaan op een plant en daarna op de kaart om te plaatsen.",
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // 2. Plant Selector
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = if (state.selectedPlant != null) "Plaats nu: ${state.selectedPlant?.naam}" else "Jouw Planten",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D5A36)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(state.availablePlanten) { plant ->
                            val isSelected = state.selectedPlant?.firestoreId == plant.firestoreId
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(80.dp)
                                    .clickable { viewModel.selectPlant(plant) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(
                                            if (isSelected) Color(0xFFD8EED8) else Color(0xFFF7F9F6),
                                            CircleShape
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = Color(0xFF2D5A36),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (plant.fotoUri != null) {
                                        AsyncImage(
                                            model = plant.fotoUri,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Default.LocalFlorist, null, tint = Color(0xFF2D5A36).copy(alpha = 0.3f))
                                    }
                                }
                                Text(
                                    plant.naam,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    modifier = Modifier.padding(top = 8.dp),
                                    color = if (isSelected) Color(0xFF2D5A36) else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
