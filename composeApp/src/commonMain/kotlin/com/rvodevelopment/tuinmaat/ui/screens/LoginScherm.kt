package com.rvodevelopment.tuinmaat.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rvodevelopment.tuinmaat.ui.components.InvoerVeldMetIcoon
import com.rvodevelopment.tuinmaat.ui.components.TuinAchtergrond
import com.rvodevelopment.tuinmaat.ui.theme.*
import com.rvodevelopment.tuinmaat.ui.viewmodel.LoginViewModel
import org.jetbrains.compose.resources.painterResource
import com.rvodevelopment.tuinmaat.composeapp.generated.resources.*
import com.rvodevelopment.tuinmaat.getPlatform
import com.rvodevelopment.tuinmaat.PlatformType

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScherm(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val email by viewModel.email.collectAsState()
    val wachtwoord by viewModel.wachtwoord.collectAsState()
    val voornaam by viewModel.voornaam.collectAsState()
    val achternaam by viewModel.achternaam.collectAsState()
    val isRegistreren by viewModel.isRegistreren.collectAsState()
    val isLaden by viewModel.isLaden.collectAsState()
    val foutMelding by viewModel.foutMelding.collectAsState()
    val wachtwoordZichtbaar by viewModel.wachtwoordZichtbaar.collectAsState()
    val onthoudEmail by viewModel.onthoudEmail.collectAsState()
    val biometrieIngeschakeld by viewModel.biometrieIngeschakeld.collectAsState()
    val platform = remember { getPlatform() }

    LaunchedEffect(Unit) {
        viewModel.tryAutoBiometric(onLoginSuccess)
    }

    TuinAchtergrond {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Surface(
                modifier = Modifier.size(100.dp).neumorphicShadow(shape = CircleShape),
                shape = CircleShape,
                color = Color.White
            ) {
                Image(
                    painter = painterResource(Res.drawable.tuin_logo),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(12.dp).clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isRegistreren) "Nieuw Account" else "TuinMaat",
                style = MaterialTheme.typography.headlineLarge,
                color = DonkerGroen,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isRegistreren) {
                InvoerVeldMetIcoon(
                    label = "Voornaam",
                    waarde = voornaam,
                    onWaardeChange = { viewModel.onVoornaamChanged(it) },
                    icoon = Icons.Default.Person
                )
                InvoerVeldMetIcoon(
                    label = "Achternaam",
                    waarde = achternaam,
                    onWaardeChange = { viewModel.onAchternaamChanged(it) },
                    icoon = Icons.Default.Person
                )
            }

            InvoerVeldMetIcoon(
                label = "E-mailadres",
                waarde = email,
                onWaardeChange = { viewModel.onEmailChanged(it) },
                icoon = Icons.Default.Email,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                    autoCorrectEnabled = false
                )
            )

            InvoerVeldMetIcoon(
                label = "Wachtwoord",
                waarde = wachtwoord,
                onWaardeChange = { viewModel.onWachtwoordChanged(it) },
                icoon = Icons.Default.Lock,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (wachtwoordZichtbaar) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Password,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                    autoCorrectEnabled = false
                ),
                trailingIcon = {
                    IconButton(onClick = { viewModel.toggleWachtwoordZichtbaarheid() }) {
                        Icon(
                            if (wachtwoordZichtbaar) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = DonkerGroen
                        )
                    }
                }
            )

            if (!isRegistreren) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = onthoudEmail,
                            onCheckedChange = { viewModel.toggleOnthoudEmail(it) },
                            colors = CheckboxDefaults.colors(checkedColor = DonkerGroen)
                        )
                        Text("Onthouden", style = MaterialTheme.typography.bodySmall, color = DonkerGroen)
                    }
                    TextButton(onClick = { viewModel.herstelWachtwoord() }) {
                        Text("Wachtwoord vergeten?", style = MaterialTheme.typography.bodySmall, color = DonkerGroen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (foutMelding != null) {
                Text(
                    text = foutMelding!!,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = { viewModel.voerActieUit(onLoginSuccess) },
                modifier = Modifier.fillMaxWidth().height(56.dp).neumorphicShadow(shape = RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = GrasGroen),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLaden
            ) {
                if (isLaden) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (isRegistreren) "Account Aanmaken" else "Inloggen", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            if (platform == PlatformType.ANDROID) {
                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = DonkerGroen.copy(alpha = 0.2f))
                    Text(
                        " of ", 
                        style = MaterialTheme.typography.bodySmall, 
                        color = DonkerGroen.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = DonkerGroen.copy(alpha = 0.2f))
                }

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = { viewModel.loginWithGoogle(onLoginSuccess) },
                    modifier = Modifier.fillMaxWidth().height(56.dp).neumorphicShadow(shape = RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DonkerGroen),
                    enabled = !isLaden
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = DonkerGroen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Inloggen met Google", color = DonkerGroen, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = { viewModel.toggleRegistreren() },
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    if (isRegistreren) "Al een account? Log hier in" else "Nog geen account? Registreer hier",
                    color = DonkerGroen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
