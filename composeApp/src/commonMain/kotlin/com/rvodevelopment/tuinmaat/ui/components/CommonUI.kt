package com.rvodevelopment.tuinmaat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.theme.ZachtBeige
import com.rvodevelopment.tuinmaat.ui.theme.neumorphicShadow
import org.jetbrains.compose.resources.painterResource

import androidx.compose.ui.graphics.Brush
import com.rvodevelopment.tuinmaat.ui.theme.*

@Composable
fun TuinAchtergrond(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AchtergrondGroenLicht,
                        AchtergrondGroenMidden
                    )
                )
            )
    ) {
        content()
    }
}

@Composable
fun MenuKnop(tekst: String, icoon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .neumorphicShadow(shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = ZachtBeige,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = ZachtBeige,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.neumorphicShadow(shape = RoundedCornerShape(10.dp))
            ) {
                Icon(icoon, contentDescription = null, tint = DonkerGroen, modifier = Modifier.padding(8.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))
            Text(tekst, color = DonkerGroen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InvoerVeldMetIcoon(
    label: String,
    waarde: String,
    onWaardeChange: (String) -> Unit,
    icoon: ImageVector,
    isMultiLine: Boolean = false,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icoon, contentDescription = null, tint = DonkerGroen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = DonkerGroen, fontWeight = FontWeight.Bold)
        }
        OutlinedTextField(
            value = waarde,
            onValueChange = onWaardeChange,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            minLines = if (isMultiLine) 3 else 1,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = DonkerGroen,
                unfocusedBorderColor = DonkerGroen.copy(alpha = 0.5f)
            )
        )
    }
}
