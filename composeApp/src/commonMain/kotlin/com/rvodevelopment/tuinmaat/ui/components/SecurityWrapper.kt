package com.rvodevelopment.tuinmaat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rvodevelopment.tuinmaat.getPlatform
import com.rvodevelopment.tuinmaat.PlatformType
import com.rvodevelopment.tuinmaat.service.AuthService
import com.rvodevelopment.tuinmaat.service.BiometricService
import com.rvodevelopment.tuinmaat.repository.UserRepository
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.theme.ZachtBeige
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

val LocalIsLocked = compositionLocalOf { false }

@Composable
fun SecurityWrapper(
    authService: AuthService = koinInject(),
    userRepository: UserRepository = koinInject(),
    biometricService: BiometricService = koinInject(),
    selectionService: com.rvodevelopment.tuinmaat.service.SelectionService = koinInject(),
    content: @Composable () -> Unit
) {
    val currentUser by authService.currentUser.collectAsState(initial = null)
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    var isLocked by remember { mutableStateOf(false) }
    var hasCheckedInitialLock by remember { mutableStateOf(false) }
    var securityType by remember { mutableStateOf("NONE") }
    var savedPin by remember { mutableStateOf("") }
    
    // Gebruik een Ref-achtig object om de status bij te houden zonder recomposities te triggeren
    val activityState = remember { 
        object { 
            var lastActiveTime = com.rvodevelopment.tuinmaat.currentTimeMillis()
            var lastPauseTime = 0L
        } 
    }

    val inactivityTimeout = 5 * 60 * 1000 // 5 minuten
    val resumeGracePeriod = 2000 // 2 seconden (tegen flikkering/snelle switches)

    DisposableEffect(lifecycleOwner, securityType) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    activityState.lastPauseTime = com.rvodevelopment.tuinmaat.currentTimeMillis()
                }
                Lifecycle.Event.ON_RESUME -> {
                    val now = com.rvodevelopment.tuinmaat.currentTimeMillis()
                    val isSysteemActie = selectionService.isVerwachtSysteemActie
                    
                    // Alleen vergrendelen als:
                    // 1. Er een beveiliging is ingesteld
                    // 2. We niet al vergrendeld zijn
                    // 3. Het GEEN bewuste systeem actie is (camera/galerij)
                    // 4. We langer dan de grace period weg zijn geweest (voorkomt lock bij snelle UI glitches)
                    if (securityType != "NONE" && !isLocked && !isSysteemActie) {
                        val timeAway = if (activityState.lastPauseTime > 0) now - activityState.lastPauseTime else 10000
                        if (timeAway > resumeGracePeriod) {
                            isLocked = true
                            activityState.lastActiveTime = now
                        }
                    }
                    
                    // Reset de vlag na consumptie met een kleine vertraging
                    // om meerdere snelle Resume-cycles (bijv. bij foutmeldingen) op te vangen.
                    if (isSysteemActie) {
                        scope.launch {
                            delay(3000) 
                            selectionService.resetSysteemActie()
                        }
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Gebruikersgegevens ophalen (securityType laden)
    LaunchedEffect(currentUser) {
        val uid = currentUser?.uid
        if (uid != null) {
            userRepository.getUserData(uid)
                .catch { emit(null) }
                .collect { userData ->
                    securityType = userData?.securityType ?: "NONE"
                    savedPin = userData?.securityPin ?: ""
                    
                    // Initiële check bij koude start
                    if (!hasCheckedInitialLock && securityType != "NONE") {
                        isLocked = true
                        hasCheckedInitialLock = true
                    }
                }
        } else {
            securityType = "NONE"
            isLocked = false
            hasCheckedInitialLock = false
        }
    }

    // Inactiviteits timer (als de app OPEN is maar niet wordt aangeraakt)
    LaunchedEffect(currentUser, securityType, isLocked) {
        if (currentUser != null && securityType != "NONE" && !isLocked) {
            while (true) {
                delay(30000) // Check elke 30 sec
                if (com.rvodevelopment.tuinmaat.currentTimeMillis() - activityState.lastActiveTime > inactivityTimeout) {
                    isLocked = true
                    break
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Main)
                        activityState.lastActiveTime = com.rvodevelopment.tuinmaat.currentTimeMillis()
                    }
                }
            }
    ) {
        CompositionLocalProvider(LocalIsLocked provides isLocked) {
            content()

            if (isLocked && currentUser != null && securityType != "NONE") {
                LockScreen(
                    securityType = securityType,
                    savedPin = savedPin,
                    biometricService = biometricService,
                    selectionService = selectionService,
                    onUnlock = {
                        isLocked = false
                        activityState.lastActiveTime = com.rvodevelopment.tuinmaat.currentTimeMillis()
                    }
                )
            }
        }
    }
}

@Composable
fun LockScreen(
    securityType: String,
    savedPin: String,
    biometricService: BiometricService,
    selectionService: com.rvodevelopment.tuinmaat.service.SelectionService,
    onUnlock: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val platform = getPlatform()
    val biometrieNaam = if (platform == PlatformType.IOS) "FaceID" else "Biometrie"
    val biometrieIcon = if (platform == PlatformType.IOS) Icons.Default.Face else Icons.Default.Fingerprint

    Box(
        modifier = Modifier.fillMaxSize().background(ZachtBeige).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = DonkerGroen)
            Spacer(modifier = Modifier.height(16.dp))
            Text("App Vergrendeld", style = MaterialTheme.typography.headlineMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
            Text("Beveiligd met $biometrieNaam", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

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
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = DonkerGroen,
                        unfocusedBorderColor = DonkerGroen.copy(alpha = 0.5f)
                    )
                )
            } else if (securityType == "BIOMETRIC") {
                Button(
                    onClick = {
                        scope.launch {
                            selectionService.markeerSysteemActie()
                            biometricService.authenticate().onSuccess { onUnlock() }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DonkerGroen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(biometrieIcon, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ontgrendel met $biometrieNaam")
                }
                
                LaunchedEffect(Unit) {
                    selectionService.markeerSysteemActie()
                    biometricService.authenticate().onSuccess { onUnlock() }
                }
            }
        }
    }
}
