package com.rvodevelopment.tuinmaat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import com.rvodevelopment.tuinmaat.service.AuthService
import com.rvodevelopment.tuinmaat.service.BiometricService
import com.rvodevelopment.tuinmaat.repository.UserRepository
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.theme.ZachtBeige
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SecurityWrapper(
    authService: AuthService = koinInject(),
    userRepository: UserRepository = koinInject(),
    biometricService: BiometricService = koinInject(),
    content: @Composable () -> Unit
) {
    val currentUser by authService.currentUser.collectAsState(initial = null)
    
    var isLocked by remember { mutableStateOf(false) }
    var securityType by remember { mutableStateOf("NONE") }
    var savedPin by remember { mutableStateOf("") }
    
    // Gebruik een Ref-achtig object om de lastActiveTime bij te houden zonder recomposities te triggeren
    val activityState = remember { object { var lastActiveTime = 0L } }

    val timeoutMillis = 5 * 60 * 1000

    LaunchedEffect(currentUser) {
        val uid = currentUser?.uid
        if (uid != null) {
            userRepository.getUserData(uid).collect { userData ->
                securityType = userData?.securityType ?: "NONE"
                savedPin = userData?.securityPin ?: ""
            }
        } else {
            securityType = "NONE"
            savedPin = ""
            isLocked = false
        }
    }

    LaunchedEffect(currentUser, securityType, isLocked) {
        if (currentUser != null && securityType != "NONE" && !isLocked) {
            if (activityState.lastActiveTime == 0L) {
                activityState.lastActiveTime = com.rvodevelopment.tuinmaat.currentTimeMillis()
            }
            
            while (true) {
                delay(10000)
                if (com.rvodevelopment.tuinmaat.currentTimeMillis() - activityState.lastActiveTime > timeoutMillis) {
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
                        // We luisteren op de Main pass om de UI niet te blokkeren
                        awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Main)
                        activityState.lastActiveTime = com.rvodevelopment.tuinmaat.currentTimeMillis()
                    }
                }
            }
    ) {
        content()

        if (isLocked && currentUser != null && securityType != "NONE") {
            LockScreen(
                securityType = securityType,
                savedPin = savedPin,
                biometricService = biometricService,
                onUnlock = {
                    isLocked = false
                    activityState.lastActiveTime = com.rvodevelopment.tuinmaat.currentTimeMillis()
                }
            )
        }
    }
}

@Composable
fun LockScreen(
    securityType: String,
    savedPin: String,
    biometricService: BiometricService,
    onUnlock: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

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
                            biometricService.authenticate().onSuccess { onUnlock() }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DonkerGroen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nu ontgrendelen")
                }
                
                LaunchedEffect(Unit) {
                    biometricService.authenticate().onSuccess { onUnlock() }
                }
            }
        }
    }
}
