package com.rvodevelopment.tuinmaat.premium.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.theme.GrasGroen

@Composable
fun HandmatigeTaakDialog(
    titel: String,
    omschrijving: String,
    onFieldsChange: (String, String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nieuwe Taak ✍️", color = DonkerGroen, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = titel,
                    onValueChange = { onFieldsChange(it, omschrijving) },
                    label = { Text("Wat ga je doen?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = omschrijving,
                    onValueChange = { onFieldsChange(titel, it) },
                    label = { Text("Notitie (optioneel)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = DonkerGroen),
                enabled = titel.isNotBlank()
            ) {
                Text("Toevoegen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuleren", color = Color.Gray)
            }
        }
    )
}

@Composable
fun NotificatieInstellingenDialog(
    frequentie: String,
    tijd: String,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFreq by remember { mutableStateOf(frequentie) }
    var selectedTime by remember { mutableStateOf(tijd) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Meldingen Instellen 🔔", color = DonkerGroen, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Hoe vaak wilt u een overzicht van uw tuintaken ontvangen?", style = MaterialTheme.typography.bodySmall)
                
                Column {
                    listOf("NONE" to "Geen meldingen", "DAILY" to "Elke dag", "WEEKLY" to "Eén keer per week").forEach { (value, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedFreq = value }) {
                            RadioButton(selected = selectedFreq == value, onClick = { selectedFreq = value }, colors = RadioButtonDefaults.colors(selectedColor = GrasGroen))
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                if (selectedFreq != "NONE") {
                    Text("Op welk tijdstip?", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("08:00", "09:00", "10:00", "19:00", "20:00").forEach { t ->
                            FilterChip(
                                selected = selectedTime == t,
                                onClick = { selectedTime = t },
                                label = { Text(t) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GrasGroen, selectedLabelColor = Color.White)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedFreq, selectedTime); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = DonkerGroen)) {
                Text("Opslaan")
            }
        }
    )
}
