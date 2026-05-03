package com.rvodevelopment.tuinmaat.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Park
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.rvodevelopment.tuinmaat.ui.theme.*
import org.jetbrains.compose.resources.painterResource
import tuinmaat.resources.*

@Composable
fun TuinMaatLogo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(60.dp).neumorphicShadow(shape = CircleShape),
            shape = CircleShape,
            color = Color.White
        ) {
            Image(
                painter = painterResource(Res.drawable.tuin_logo),
                contentDescription = "TuinMaat Logo",
                modifier = Modifier.fillMaxSize().padding(8.dp).clip(CircleShape),
                contentScale = ContentScale.Fit
            )
        }
        Text(
            "TuinMaat",
            color = DonkerGroen,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
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
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    autofillTypes: List<AutofillType>? = null
) {
    val autofill = LocalAutofill.current
    val autofillNode = if (autofillTypes != null) {
        remember {
            AutofillNode(
                onFill = onWaardeChange,
                autofillTypes = autofillTypes
            )
        }
    } else null

    if (autofillNode != null) {
        LocalAutofillTree.current += autofillNode
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
                                autofill?.let {
                                    if (focusState.isFocused) {
                                        it.requestAutofillForNode(autofillNode)
                                    } else {
                                        it.cancelAutofillForNode(autofillNode)
                                    }
                                }
                            }
                    } else Modifier
                ),
            minLines = if (isMultiLine) 3 else 1,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions.copy(
                autoCorrectEnabled = false
            ),
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
