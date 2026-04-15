package com.example.tuinmaat

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.tuinmaat.ui.theme.DonkerGroen
import com.example.tuinmaat.ui.theme.ZachtBeige
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay

/**
 * SecurityWrapper beheert de inactiviteit-timer (5 minuten).
 * Deze moet in MainActivity om de NavHost heen geplaatst worden.
 */
@Composable
fun SecurityWrapper(content: @Composable () -> Unit) {
    val auth = Firebase.auth
    val db = Firebase.firestore
    val lifecycleOwner = LocalLifecycleOwner.current

    // Gebruik expliciete types om 'type inference' fouten te voorkomen
    var isLocked by remember { mutableStateOf<Boolean>(false) }
    var securityType by remember { mutableStateOf<String>("NONE") }
    var savedPin by remember { mutableStateOf<String>("") }
    var lastActiveTime by remember { mutableStateOf(System.currentTimeMillis()) }

    val timeoutMillis = 5 * 60 * 1000

    // Real-time luisteren naar wijzigingen in beveiligingsinstellingen
    LaunchedEffect(auth.currentUser) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TuinMaat", "Firestore error in SecurityWrapper: ${error.message}")
                    return@addSnapshotListener
                }
                securityType = snapshot?.getString("securityType") ?: "NONE"
                savedPin = snapshot?.getString("securityPin") ?: ""
            }
        }
    }

    // Controleer op time-out wanneer de app weer op de voorgrond komt
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (securityType != "NONE" && auth.currentUser != null) {
                    if (System.currentTimeMillis() - lastActiveTime > timeoutMillis) {
                        isLocked = true
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Achtergrond loop die inactiviteit controleert terwijl de app open is
    LaunchedEffect(isLocked, securityType) {
        while (!isLocked && securityType != "NONE" && auth.currentUser != null) {
            delay(10000) // Check elke 10 seconden
            if (System.currentTimeMillis() - lastActiveTime > timeoutMillis) {
                isLocked = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                        // Reset de timer bij elke aanraking van het scherm
                        lastActiveTime = System.currentTimeMillis()
                    }
                }
            }
    ) {
        content()

        // Toon het vergrendelscherm over de content heen als de app op slot zit
        if (isLocked && auth.currentUser != null && securityType != "NONE") {
            LockScreen(
                securityType = securityType,
                savedPin = savedPin,
                onUnlock = {
                    isLocked = false
                    lastActiveTime = System.currentTimeMillis()
                }
            )
        }
    }
}

/**
 * Het scherm dat verschijnt bij vergrendeling.
 */
@Composable
fun LockScreen(securityType: String, savedPin: String, onUnlock: () -> Unit) {
    val context = LocalContext.current as FragmentActivity
    var enteredPin by remember { mutableStateOf<String>("") }

    // Blokkeer de terug-knop van Android zodat men niet 'terug' kan naar de app
    BackHandler { /* Geen actie toegestaan */ }

    Box(
        modifier = Modifier.fillMaxSize().background(ZachtBeige).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = DonkerGroen)
            Spacer(modifier = Modifier.height(16.dp))
            Text("App Vergrendeld", style = MaterialTheme.typography.headlineMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
            Text("Sessie verlopen door inactiviteit", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

            if (securityType == "PIN") {
                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                            enteredPin = it
                            if (it == savedPin) onUnlock()
                        }
                    },
                    label = { Text("Pincode") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else if (securityType == "BIOMETRIC") {
                Button(
                    onClick = { authenticateBiometric(context, onUnlock) },
                    colors = ButtonDefaults.buttonColors(containerColor = DonkerGroen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nu ontgrendelen")
                }
                // Probeer direct biometrie te starten bij verschijnen van scherm
                LaunchedEffect(Unit) { authenticateBiometric(context, onUnlock) }
            }
        }
    }
}

/**
 * Biometrische authenticatie logica (Vingerafdruk/Gezicht)
 */
private fun authenticateBiometric(context: FragmentActivity, onUnlock: () -> Unit) {
    val executor = ContextCompat.getMainExecutor(context)
    val biometricPrompt = BiometricPrompt(context, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onUnlock()
        }
    })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("TuinMaat Ontgrendelen")
        .setSubtitle("Gebruik je vingerafdruk of gezichtsscan")
        .setNegativeButtonText("Annuleren")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        .build()

    biometricPrompt.authenticate(promptInfo)
}