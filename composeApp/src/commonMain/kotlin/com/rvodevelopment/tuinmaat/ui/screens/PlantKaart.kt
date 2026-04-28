package com.rvodevelopment.tuinmaat.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rvodevelopment.tuinmaat.model.Plant
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.theme.ZachtBeige
import com.rvodevelopment.tuinmaat.ui.theme.neumorphicShadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantKaart(plant: Plant, onNavigateToDetail: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .neumorphicShadow(shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = ZachtBeige,
        onClick = { onNavigateToDetail(plant.firestoreId) }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                // Image loading will be handled differently in KMP (e.g., using Coil3 or Compose ImageLoader)
                // For now, we'll use a placeholder
                Icon(
                    Icons.Default.LocalFlorist,
                    contentDescription = null,
                    tint = DonkerGroen.copy(alpha = 0.3f),
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = plant.naam,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DonkerGroen
                )
                if (plant.wetenschappelijkeNaam.isNotBlank()) {
                    Text(
                        text = plant.wetenschappelijkeNaam,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = DonkerGroen.copy(alpha = 0.8f)
                    )
                }
                Text(
                    text = plant.locatie,
                    style = MaterialTheme.typography.labelSmall,
                    color = DonkerGroen.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = DonkerGroen.copy(alpha = 0.2f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

