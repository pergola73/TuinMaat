package com.rvodevelopment.tuinmaat.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rvodevelopment.tuinmaat.getPlatform
import com.rvodevelopment.tuinmaat.PlatformType
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.theme.GrasGroen
import com.rvodevelopment.tuinmaat.ui.theme.neumorphicShadow
import org.jetbrains.compose.resources.painterResource
import com.rvodevelopment.tuinmaat.composeapp.generated.resources.*

@Composable
fun TuinMaatLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.tuin_logo),
        contentDescription = "TuinMaat Logo",
        modifier = modifier.size(64.dp).clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Fit
    )
}

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun InvoerVeldMetIcoon(
    label: String,
    waarde: String,
    onWaardeChange: (String) -> Unit,
    icoon: ImageVector,
    modifier: Modifier = Modifier,
    isMultiLine: Boolean = false,
    placeholder: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    autofillTypes: List<AutofillType>? = null
) {
    val autofill = LocalAutofill.current
    val autofillTree = LocalAutofillTree.current
    val autofillNode = if (autofillTypes != null) {
        remember(autofillTypes) {
            AutofillNode(
                onFill = onWaardeChange,
                autofillTypes = autofillTypes
            )
        }
    } else null

    if (autofillNode != null) {
        DisposableEffect(autofillNode) {
            autofillTree.children[autofillNode.id] = autofillNode
            onDispose {
                autofillTree.children.remove(autofillNode.id)
            }
        }
    }

    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icoon, contentDescription = null, tint = DonkerGroen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = DonkerGroen, fontWeight = FontWeight.Bold)
        }
        
        OutlinedTextField(
            value = waarde,
            onValueChange = onWaardeChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .then(
                    if (autofillNode != null) {
                        Modifier
                            .onGloballyPositioned {
                                autofillNode.boundingBox = it.boundsInWindow()
                            }
                            .onFocusChanged { focusState ->
                                try {
                                    autofill?.let {
                                        if (focusState.isFocused) {
                                            it.requestAutofillForNode(autofillNode)
                                        } else {
                                            it.cancelAutofillForNode(autofillNode)
                                        }
                                    }
                                } catch (e: Exception) {
                                    println("Autofill error: ${e.message}")
                                }
                            }
                    } else Modifier
                ),
            minLines = if (isMultiLine) 3 else 1,
            placeholder = placeholder?.let { { Text(it, style = MaterialTheme.typography.bodyMedium, color = DonkerGroen.copy(alpha = 0.4f)) } },
            shape = RoundedCornerShape(12.dp),
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions.copy(
                autoCorrectEnabled = false
            ),
            keyboardActions = keyboardActions,
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

@Composable
fun MenuKnop(
    tekst: String,
    icoon: ImageVector,
    isPremium: Boolean = false,
    isNieuw: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(56.dp)
            .neumorphicShadow(shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF5F5F0)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = DonkerGroen.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icoon, contentDescription = null, tint = DonkerGroen, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    tekst,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = DonkerGroen
                )
                if (isPremium || isNieuw) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isPremium) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "PREMIUM",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFD4AF37),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 8.sp
                            )
                        }
                        if (isPremium && isNieuw) {
                            Text(
                                " • ",
                                style = MaterialTheme.typography.labelSmall,
                                color = GrasGroen.copy(alpha = 0.5f)
                            )
                        }
                        if (isNieuw) {
                            Text(
                                "NIEUW",
                                style = MaterialTheme.typography.labelSmall,
                                color = GrasGroen,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = DonkerGroen.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun LocationChip(selected: Boolean, label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent
    ) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = { Text(label) },
            modifier = Modifier.height(40.dp),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = DonkerGroen,
                selectedLabelColor = Color(0xFFF5F5F0),
                containerColor = Color(0xFFF5F5F0),
                labelColor = DonkerGroen
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = Color.Transparent,
                selectedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(Icons.Default.Check, null, tint = DonkerGroen, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun PremiumUpgradeDialog(
    isPremium: Boolean,
    isLaden: Boolean,
    price: String,
    onUpgrade: () -> Unit,
    onRestore: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isPremium) "Je bent Premium!" else "Upgrade naar Premium") },
        text = {
            Column {
                Text("Met TuinMaat Premium geniet je van:")
                Spacer(modifier = Modifier.height(8.dp))
                BulletPoint("Geen advertenties")
                BulletPoint("Onbeperkt aantal planten")
                BulletPoint("Exclusieve tuintips")
                
                if (isLaden) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DonkerGroen)
                } else if (isPremium) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Bedankt voor je steun!", fontWeight = FontWeight.Bold, color = DonkerGroen)
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = DonkerGroen.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val platform = getPlatform()
                        val promoText = if (platform == PlatformType.ANDROID) {
                            "Heb je een promotiecode? Klik op 'Nu upgraden' en kies 'Code inwisselen' bij de betaalmethoden van Google."
                        } else {
                            "Heb je een promotiecode? Wissel deze in via de App Store of via de link die je hebt ontvangen."
                        }
                        Text(
                            promoText,
                            style = MaterialTheme.typography.labelSmall,
                            color = DonkerGroen,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!isPremium) {
                Button(
                    onClick = onUpgrade,
                    colors = ButtonDefaults.buttonColors(containerColor = DonkerGroen)
                ) {
                    Text(
                        text = "Nu upgraden ($price)",
                        maxLines = 1,
                        softWrap = false
                    )
                }
            } else {
                Button(onClick = onDismiss) {
                    Text("Sluiten")
                }
            }
        },
        dismissButton = {
            if (!isPremium) {
                Row {
                    TextButton(onClick = onDismiss) {
                        Text("Annuleren", color = Color.Gray)
                    }
                    TextButton(onClick = onRestore) {
                        Text("Aankopen herstellen")
                    }
                }
            }
        }
    )
}

