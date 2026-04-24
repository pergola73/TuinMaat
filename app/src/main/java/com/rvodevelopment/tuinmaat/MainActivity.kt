package com.rvodevelopment.tuinmaat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import java.util.Calendar
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.style.TextAlign
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rvodevelopment.tuinmaat.ui.theme.DonkerGroen
import com.rvodevelopment.tuinmaat.ui.theme.GrasGroen
import com.rvodevelopment.tuinmaat.ui.theme.TuinAchtergrond
import com.rvodevelopment.tuinmaat.ui.theme.TuinMaatTheme
import com.rvodevelopment.tuinmaat.ui.theme.ZachtBeige
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.google.firebase.vertexai.vertexAI
import com.google.firebase.vertexai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import com.google.firebase.firestore.FieldValue

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase App Check
        // Firebase is initialized automatically, but if you need manual initialization:
        // com.google.firebase.FirebaseApp.initializeApp(this)

        Firebase.appCheck.installAppCheckProviderFactory(
            if (BuildConfig.DEBUG) {
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
        )

        // Zorg ervoor dat de content achter de statusbalk kan lopen voor een modern uiterlijk
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            TuinMaatTheme {
                val view = LocalView.current

                // Globale instelling voor de statusbalk iconen
                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as Activity).window
                        // We zetten de kleur van de statusbalk op ZachtBeige
                        window.statusBarColor = ZachtBeige.toArgb()

                        // forceer DONKERE iconen (zwart) in de statusbalk
                        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                    }
                }

                val navController = rememberNavController()
                val auth = Firebase.auth
                val startDestination = if (auth.currentUser != null) "hoofdmenu" else "login"

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ZachtBeige
                ) {
                    SecurityWrapper {
                        NavHost(navController = navController, startDestination = startDestination) {
                            composable("login") { LoginScherm(navController) }
                            composable("hoofdmenu") { HoofdMenu(navController) }
                            composable("lijst") { PlantenLijstScherm(navController) }
                            composable("toevoegen?plantId={plantId}&focus={focus}") { backStackEntry ->
                                val id = backStackEntry.arguments?.getString("plantId")
                                val focus = backStackEntry.arguments?.getString("focus")
                                PlantToevoegenScherm(navController, bewerkPlantFirestoreId = id, initialFocus = focus)
                            }
                            composable("detail/{plantId}") { backStackEntry ->
                                val id = backStackEntry.arguments?.getString("plantId")
                                PlantDetailScherm(id, navController)
                            }
                            composable("kalender") { SnoeiKalenderScherm(navController) }
                            composable("instellingen") { InstellingenScherm(navController) }
                            composable("beveiliging") { BeveiligingsInstellingenScherm(navController) }
                            composable("locatiebeheer") { LocatieBeheerScherm(navController) }
                            composable("profiel_bewerken") { ProfielBewerkenScherm(navController) }
                            composable("tuin_delen") { TuinDelenScherm(navController) }
                            composable("info") { InfoScherm(navController) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScherm(navController: NavController) {
    var email by rememberSaveable { mutableStateOf("") }
    var wachtwoord by rememberSaveable { mutableStateOf("") }
    var voornaam by rememberSaveable { mutableStateOf("") }
    var isRegistreren by rememberSaveable { mutableStateOf(false) }
    var wachtwoordZichtbaar by rememberSaveable { mutableStateOf(false) }
    var isLaden by remember { mutableStateOf(false) }
    var foutMelding by rememberSaveable { mutableStateOf<String?>(null) }

    val auth = Firebase.auth
    val db = Firebase.firestore
    val context = LocalContext.current as FragmentActivity
    val focusManager = LocalFocusManager.current

    // Vertaalt technische Firebase fouten naar begrijpelijk Nederlands
    fun vertaalFoutmelding(exception: Exception?): String {
        Log.e("TuinMaatAuth", "Fout: ${exception?.message}")

        if (exception is FirebaseAuthInvalidCredentialsException) {
            return if (isRegistreren) "Er is iets misgegaan bij het aanmaken van je account."
            else "De combinatie van e-mailadres en wachtwoord is niet bekend."
        }

        if (exception !is FirebaseAuthException) {
            val msg = exception?.localizedMessage ?: ""
            return when {
                msg.contains("credential", ignoreCase = true) -> "Ongeldige inloggegevens."
                msg.contains("network", ignoreCase = true) -> "Controleer je internetverbinding."
                else -> "Er is een onbekende fout opgetreden."
            }
        }

        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> "Ongeldig e-mailadres."
            "ERROR_WRONG_PASSWORD" -> "Wachtwoord is onjuist."
            "ERROR_USER_NOT_FOUND" -> "Geen account gevonden met dit e-mailadres."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Dit e-mailadres is al in gebruik."
            "ERROR_WEAK_PASSWORD" -> "Het wachtwoord is te zwak."
            else -> "Actie mislukt. Controleer je gegevens."
        }
    }

    fun herstelWachtwoord() {
        val emailAdres = email.trim()
        if (emailAdres.isBlank()) {
            foutMelding = "Vul je e-mailadres in om een herstellink te ontvangen."
            return
        }
        isLaden = true
        // Gebruik de standaard methode zonder ActionCodeSettings om afhankelijkheid van Dynamic Links te vermijden
        auth.sendPasswordResetEmail(emailAdres).addOnCompleteListener { task ->
            isLaden = false
            if (task.isSuccessful) {
                Toast.makeText(context, "Instructies zijn verzonden naar $emailAdres", Toast.LENGTH_LONG).show()
                foutMelding = null
            } else {
                foutMelding = vertaalFoutmelding(task.exception)
            }
        }
    }

    fun voerActieUit() {
        if (isRegistreren) {
            if (email.isNotBlank() && wachtwoord.isNotBlank() && voornaam.isNotBlank()) {
                isLaden = true
                auth.createUserWithEmailAndPassword(email, wachtwoord)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            user?.sendEmailVerification()
                                ?.addOnCompleteListener { verificationTask ->
                                    if (verificationTask.isSuccessful) {
                                        Toast.makeText(context, "Bevestigingsmail verzonden naar ${user.email}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            val profile = hashMapOf(
                                "voornaam" to voornaam,
                                "email" to email,
                                "createdAt" to System.currentTimeMillis()
                            )
                            db.collection("users").document(user!!.uid).set(profile)
                                .addOnSuccessListener {
                                    // Maak ook een standaard tuin aan voor de nieuwe gebruiker
                                    val tuin = hashMapOf(
                                        "naam" to "Mijn Tuin",
                                        "locaties" to listOf("Tuin")
                                    )
                                    db.collection("tuinen").document(user.uid).set(tuin, SetOptions.merge())
                                    navController.navigate("hoofdmenu") { popUpTo(0) }
                                }
                        } else {
                            isLaden = false
                            foutMelding = vertaalFoutmelding(task.exception)
                        }
                    }
            } else {
                foutMelding = "Vul alle velden in."
            }
        } else {
            if (email.isNotBlank() && wachtwoord.isNotBlank()) {
                isLaden = true
                auth.signInWithEmailAndPassword(email, wachtwoord)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            navController.navigate("hoofdmenu") { popUpTo("login") { inclusive = true } }
                        } else {
                            isLaden = false
                            foutMelding = vertaalFoutmelding(task.exception)
                        }
                    }
            } else {
                foutMelding = "Vul e-mail en wachtwoord in."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZachtBeige)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Park, contentDescription = null, tint = DonkerGroen, modifier = Modifier.size(80.dp))
        Text(
            text = if (isRegistreren) "Nieuw Account" else "TuinMaat",
            style = MaterialTheme.typography.headlineLarge,
            color = DonkerGroen,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isRegistreren) {
            OutlinedTextField(
                value = voornaam,
                onValueChange = { voornaam = it },
                label = { Text("Voornaam") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = DonkerGroen,
                    unfocusedBorderColor = DonkerGroen.copy(alpha = 0.5f)
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; foutMelding = null },
            label = { Text("E-mailadres") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = DonkerGroen) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = DonkerGroen,
                unfocusedBorderColor = DonkerGroen.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = wachtwoord,
            onValueChange = { wachtwoord = it; foutMelding = null },
            label = { Text("Wachtwoord") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = if (wachtwoordZichtbaar) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                voerActieUit()
            }),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = DonkerGroen) },
            trailingIcon = {
                IconButton(onClick = { wachtwoordZichtbaar = !wachtwoordZichtbaar }) {
                    Icon(
                        if (wachtwoordZichtbaar) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = DonkerGroen
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = DonkerGroen,
                unfocusedBorderColor = DonkerGroen.copy(alpha = 0.5f)
            )
        )

        if (foutMelding != null) {
            Text(
                text = foutMelding!!,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (!isRegistreren) {
            TextButton(
                onClick = { herstelWachtwoord() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Wachtwoord vergeten?", color = DonkerGroen)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { voerActieUit() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DonkerGroen),
            enabled = !isLaden
        ) {
            if (isLaden) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text(if (isRegistreren) "Account Aanmaken" else "Inloggen", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { isRegistreren = !isRegistreren; foutMelding = null }) {
            Text(
                if (isRegistreren) "Heb je al een account? Log in" else "Nog geen account? Registreer hier",
                color = DonkerGroen
            )
        }
    }
}

@Composable
fun PlantKaart(plant: Plant, navController: NavController) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .neumorphicShadow(shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = ZachtBeige,
        onClick = { navController.navigate("detail/${plant.firestoreId}") }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // De container voor de afbeelding
            Surface(
                modifier = Modifier.size(60.dp), // Iets groter voor de foto
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                if (!plant.fotoUri.isNullOrEmpty()) { // Check of er een URL is
                    AsyncImage(
                        model = plant.fotoUri,
                        contentDescription = plant.naam,
                        contentScale = ContentScale.Crop, // Zorgt dat de foto het vakje vult
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback als er geen foto is
                    Icon(
                        Icons.Default.LocalFlorist,
                        contentDescription = null,
                        tint = DonkerGroen.copy(alpha = 0.3f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = plant.naam,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DonkerGroen
                )
                Text(
                    text = plant.locatie,
                    style = MaterialTheme.typography.bodySmall,
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
@Composable
fun HoofdMenu(navController: NavController) {
    val auth = Firebase.auth
    val db = Firebase.firestore
    val eigenaarNaam = remember { mutableStateOf<String?>(null) }
    val voornaam = remember { mutableStateOf("Tuinder") }
    val tuinnaam = remember { mutableStateOf("Mijn Tuin") }
    val aantalPlanten = remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            // Stap 1: Luister naar de eigen gebruikersgegevens
            db.collection("users").document(user.uid).addSnapshotListener { userDoc, error ->
                if (error != null) return@addSnapshotListener

                if (userDoc != null && userDoc.exists()) {
                    voornaam.value = userDoc.getString("voornaam") ?: "Tuinder"
                    val gid = userDoc.getString("sharedGardenId")

                    // Bepaal of we in een andermans tuin kijken
                    val isGedeeldeTuin = gid != null && gid != user.uid
                    val effectieveGid = gid ?: user.uid

                    // Stap 2: Haal de tuinnaam op
                    db.collection("tuinen").document(effectieveGid).addSnapshotListener { gardenDoc, _ ->
                        if (gardenDoc != null && gardenDoc.exists()) {
                            tuinnaam.value = gardenDoc.getString("naam") ?: "Mijn Tuin"
                        }
                    }

                    // Stap 3: Haal de naam van de eigenaar op als het een gedeelde tuin is
                    if (isGedeeldeTuin && gid != null) {
                        // We gebruiken een snapshotListener zodat wijzigingen direct zichtbaar zijn
                        db.collection("users").document(gid).addSnapshotListener { ownerDoc, _ ->
                            val naam = ownerDoc?.getString("voornaam")
                            eigenaarNaam.value = if (!naam.isNullOrBlank()) naam else "GedeeldeTuinMarker"

                            // Debug log om te checken of de naam binnenkomt
                            Log.d("TuinMaat", "Eigenaar naam opgehaald: $naam voor ID: $gid")
                        }
                    } else {
                        eigenaarNaam.value = null
                    }

                    // Stap 4: Planten teller
                    db.collection("tuinen").document(effectieveGid).collection("planten").addSnapshotListener { snapshot, _ ->
                        aantalPlanten.intValue = snapshot?.size() ?: 0
                    }
                }
            }
        }
    }

    // Gebruik de nieuwe botanische achtergrond
    TuinAchtergrond {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Logo (iets subtieler bovenaan)
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                Icon(
                    Icons.Default.Park,
                    contentDescription = null,
                    tint = DonkerGroen.copy(alpha = 0.2f),
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Prominente Header Sectie
            Column {
                // Gebruikersnaam subtiel
                Text(
                    text = "Hallo ${voornaam.value}, welkom in",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DonkerGroen.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )

                // Tuinnaam groot en dik
                Text(
                    text = tuinnaam.value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = DonkerGroen,
                    lineHeight = 42.sp
                )
                // Subtiele indicator voor de eigenaar
                if (eigenaarNaam.value != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            tint = DonkerGroen.copy(alpha = 0.4f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tuin van ${eigenaarNaam.value}",
                            style = MaterialTheme.typography.bodySmall,
                            color = DonkerGroen.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

               Spacer(modifier = Modifier.height(16.dp))

                // De Glass-effect Badges (Planten teller)
                Surface(
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(50.dp), // Pil-vorm
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    modifier = Modifier.neumorphicShadow(shape = RoundedCornerShape(50.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.LocalFlorist, contentDescription = null, tint = DonkerGroen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${aantalPlanten.intValue} Planten in je collectie",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = DonkerGroen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(56.dp))

            // 3. Actie titel
            Text(
                "Wat gaan we doen?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = DonkerGroen,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. De Knoppen (Menu items)
            // Tip: Zorg dat MenuKnop intern ook Color.White.copy(alpha = 0.7f) gebruikt!
            MenuKnop("Mijn Planten", Icons.AutoMirrored.Filled.List) { navController.navigate("lijst") }
            MenuKnop("Plant Toevoegen", Icons.Default.Add) { navController.navigate("toevoegen") }
            MenuKnop("Snoei Kalender", Icons.Default.CalendarToday) { navController.navigate("kalender") }
            MenuKnop("Instellingen", Icons.Default.Settings) { navController.navigate("instellingen") }
        }
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
        color = ZachtBeige, // MOET gelijk zijn aan achtergrond voor relief!
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icoon container ook met een beetje relief
            Surface(
                color = ZachtBeige,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.neumorphicShadow(shape = RoundedCornerShape(10.dp))
            ) {
                Icon(icoon, contentDescription = null, tint = DonkerGroen, modifier = Modifier.padding(8.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))
            Text(tekst, color = DonkerGroen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = DonkerGroen.copy(alpha = 0.2f), modifier = Modifier.size(14.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlantToevoegenScherm(
    navController: NavController,
    bewerkPlantFirestoreId: String? = null,
    initialFocus: String? = null
) {
    val db = Firebase.firestore
    val auth = Firebase.auth
    val userId = auth.currentUser?.uid ?: ""
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Focus logica
    val focusRequester = remember { FocusRequester() }
    var focusHasBeenSet by remember { mutableStateOf(false) }

    // State velden overeenkomstig met Plant detail model
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var bestaandeFotoUri by remember { mutableStateOf<String?>(null) }
    var naam by remember { mutableStateOf("") }
    var geselecteerdeLocatie by remember { mutableStateOf("") }
    var omschrijving by remember { mutableStateOf("") }
    val geselecteerdeMaanden = remember { mutableStateListOf<String>() }
    var snoeiAdvies by remember { mutableStateOf("") }
    var beschikbareLocaties by remember { mutableStateOf<List<String>>(emptyList()) }

    // UI State
    var isLaden by remember { mutableStateOf(false) }
    var laatLocatieMenuZien by remember { mutableStateOf(false) }

    val maandenLijst = listOf("Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September", "Oktober", "November", "December")

    // Load locations and existing plant
    LaunchedEffect(userId, bewerkPlantFirestoreId) {
        if (userId.isNotEmpty()) {
            try {
                val userDoc = db.collection("users").document(userId).get().await()
                val gardenId = userDoc.getString("sharedGardenId") ?: userId
                
                // Load locations
                val gardenDoc = db.collection("tuinen").document(gardenId).get().await()
                @Suppress("UNCHECKED_CAST")
                val locs = gardenDoc.get("locaties") as? List<String> ?: listOf("Tuin")
                beschikbareLocaties = locs
                
                if (bewerkPlantFirestoreId != null) {
                    isLaden = true
                    val doc = db.collection("tuinen").document(gardenId).collection("planten").document(bewerkPlantFirestoreId).get().await()
                    val plant = doc.toObject(Plant::class.java)
                    if (plant != null) {
                        naam = plant.naam
                        geselecteerdeLocatie = plant.locatie
                        omschrijving = plant.omschrijving
                        // Parse string back to list
                        geselecteerdeMaanden.clear()
                        if (plant.snoeiMaand.isNotBlank()) {
                            val opgeslagenMaanden = plant.snoeiMaand.split(", ")
                            geselecteerdeMaanden.addAll(opgeslagenMaanden.filter { it in maandenLijst })
                        }
                        snoeiAdvies = plant.snoeiAdvies
                        bestaandeFotoUri = plant.fotoUri
                    }
                } else if (geselecteerdeLocatie.isEmpty() && locs.isNotEmpty()) {
                    geselecteerdeLocatie = locs.first()
                }
            } catch (e: Exception) {
                Log.e("TuinMaat", "Fout bij laden data: ${e.message}")
            } finally {
                isLaden = false
            }
        }
    }

    // Gemini AI State
    var aiSuggesties by remember { mutableStateOf<List<GeminiPlantResult>>(emptyList()) }
    var laatSuggestiesZien by remember { mutableStateOf(false) }
    var isAIBezig by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { result -> if (result != null) bitmap = result }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val context = navController.context
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
            } catch (e: Exception) {
                Log.e("TuinMaat", "Error loading image: ${e.message}")
            }
        }
    }

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) cameraLauncher.launch(null)
        else Toast.makeText(context, "Camera toestemming nodig.", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        containerColor = ZachtBeige,
        topBar = {
            TopAppBar(
                title = { Text(if (bewerkPlantFirestoreId != null) "Plant Bewerken" else "Plant Toevoegen", color = DonkerGroen, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ZachtBeige)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLaden = true
                            try {
                                val userDoc = db.collection("users").document(userId).get().await()
                                val gardenId = userDoc.getString("sharedGardenId") ?: userId

                                val plantData = hashMapOf(
                                    "naam" to naam,
                                    "locatie" to geselecteerdeLocatie,
                                    "omschrijving" to omschrijving,
                                    "snoeiMaand" to geselecteerdeMaanden.joinToString(", "),
                                    "snoeiAdvies" to snoeiAdvies,
                                    "userId" to userId
                                )

                                val docRef = if (bewerkPlantFirestoreId != null) {
                                    db.collection("tuinen").document(gardenId).collection("planten").document(bewerkPlantFirestoreId)
                                } else {
                                    db.collection("tuinen").document(gardenId).collection("planten").document()
                                }

                                var downloadUrl = bestaandeFotoUri ?: ""
                                if (bitmap != null) {
                                    val storageRef = Firebase.storage.reference.child("planten/${docRef.id}.jpg")
                                    val baos = ByteArrayOutputStream()
                                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                                    val data = baos.toByteArray()
                                    storageRef.putBytes(data).await()
                                    downloadUrl = storageRef.downloadUrl.await().toString()
                                }

                                val finalData = plantData.toMutableMap()
                                finalData["firestoreId"] = docRef.id
                                finalData["fotoUri"] = downloadUrl

                                docRef.set(finalData, SetOptions.merge()).await()
                                navController.popBackStack()
                            } catch (e: Exception) {
                                Log.e("TuinMaat", "Fout bij opslaan: ${e.message}")
                            } finally {
                                isLaden = false
                            }
                        }
                    },
                    enabled = naam.isNotBlank() && !isLaden,
                    colors = ButtonDefaults.buttonColors(containerColor = DonkerGroen),
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLaden) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Opslaan in Collectie", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(scrollState).background(ZachtBeige)
        ) {
            // Foto Sectie
            Box(
                modifier = Modifier.fillMaxWidth().height(250.dp).background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(bitmap = bitmap!!.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else if (bestaandeFotoUri != null) {
                    AsyncImage(model = bestaandeFotoUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Text("Geen foto geselecteerd", color = Color.Gray)
                    }
                }

                Row(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                    SmallFloatingActionButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        containerColor = Color.White.copy(alpha = 0.8f)
                    ) { Icon(Icons.Default.PhotoLibrary, contentDescription = "Galerij", tint = DonkerGroen) }
                    Spacer(modifier = Modifier.width(8.dp))
                    SmallFloatingActionButton(
                        onClick = {
                            val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) cameraLauncher.launch(null)
                            else permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        containerColor = Color.White.copy(alpha = 0.8f)
                    ) { Icon(Icons.Default.PhotoCamera, contentDescription = "Camera", tint = DonkerGroen) }
                }
            }

            // AI Button
            if (bitmap != null || bestaandeFotoUri != null) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isAIBezig = true
                            try {
                                var bitmapToProcess = bitmap
                                if (bitmapToProcess == null && bestaandeFotoUri != null) {
                                    // Download en converteer bestaande foto naar bitmap voor AI
                                    val loader = ImageLoader(context)
                                    val request = ImageRequest.Builder(context)
                                        .data(bestaandeFotoUri)
                                        .allowHardware(false)
                                        .build()
                                    val result = loader.execute(request)
                                    bitmapToProcess = (result.drawable as? BitmapDrawable)?.bitmap
                                }

                                if (bitmapToProcess != null) {
                                    val resultaten = zoekPlantInfoMetAI(bitmapToProcess, context)
                                    if (resultaten.isNotEmpty()) {
                                        aiSuggesties = resultaten
                                        laatSuggestiesZien = true
                                    } else {
                                        Toast.makeText(context, "AI kon plant niet identificeren", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Kon foto niet laden voor AI", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e("TuinMaat", "AI Error: ${e.message}")
                            } finally {
                                isAIBezig = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GrasGroen),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isAIBezig
                ) {
                    if (isAIBezig) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Identificeer met Gemini AI", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (laatSuggestiesZien) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Text("Selecteer de juiste plant:", fontWeight = FontWeight.Bold, color = DonkerGroen)
                    aiSuggesties.forEach { suggestie ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                                naam = suggestie.naam
                                omschrijving = suggestie.omschrijving
                                snoeiAdvies = suggestie.snoeiAdvies
                                
                                // Parse AI maand suggesties naar chips
                                geselecteerdeMaanden.clear()
                                maandenLijst.forEach { maand ->
                                    if (suggestie.snoeiMaand.contains(maand, ignoreCase = true)) {
                                        geselecteerdeMaanden.add(maand)
                                    }
                                }
                                
                                laatSuggestiesZien = false
                            },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, GrasGroen.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Plant referentie afbeelding via loremflickr op basis van zoekTerm
                                Surface(
                                    modifier = Modifier.size(60.dp).neumorphicShadow(shape = RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp),
                                    color = ZachtBeige
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data("https://loremflickr.com/320/240/${suggestie.zoekTerm.replace(" ", ",")}")
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Referentiefoto",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(suggestie.naam, fontWeight = FontWeight.Bold, color = DonkerGroen)
                                    Text(suggestie.omschrijving, maxLines = 2, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    TextButton(onClick = { laatSuggestiesZien = false }) { Text("Annuleren", color = Color.Gray) }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                // 1. Naam (Nu met InvoerVeldMetIcoon)
                InvoerVeldMetIcoon(
                    label = "Naam",
                    waarde = naam,
                    onWaardeChange = { naam = it },
                    icoon = Icons.Default.LocalFlorist // Passend icoon voor een plantnaam
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Locatie (Handmatige opbouw om exact op InvoerVeldMetIcoon te lijken)
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Label en Icoon boven het veld (zoals InvoerVeldMetIcoon dat doet)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = DonkerGroen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Locatie",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DonkerGroen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Het dropdown veld zonder eigen icoon/label erin
                    ExposedDropdownMenuBox(
                        expanded = laatLocatieMenuZien,
                        onExpandedChange = { laatLocatieMenuZien = !laatLocatieMenuZien }
                    ) {
                        OutlinedTextField(
                            value = geselecteerdeLocatie,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = laatLocatieMenuZien) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = DonkerGroen,
                                unfocusedBorderColor = DonkerGroen.copy(alpha = 0.5f)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = laatLocatieMenuZien,
                            onDismissRequest = { laatLocatieMenuZien = false }
                        ) {
                            beschikbareLocaties.forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text(loc) },
                                    onClick = {
                                        geselecteerdeLocatie = loc
                                        laatLocatieMenuZien = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. De overige velden
                InvoerVeldMetIcoon("Omschrijving", omschrijving, { omschrijving = it }, Icons.Default.Info)
                
                // Snoeimaand Chips
                Column(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .focusRequester(focusRequester)
                        .onGloballyPositioned { coords ->
                            if (initialFocus == "snoeimaand" && !focusHasBeenSet) {
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(coords.positionInParent().y.toInt())
                                    focusRequester.requestFocus()
                                    focusHasBeenSet = true
                                }
                            }
                        }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = DonkerGroen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Beste snoeimaand", style = MaterialTheme.typography.labelLarge, color = DonkerGroen, fontWeight = FontWeight.Bold)
                    }
                    
                    FlowRow(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        maandenLijst.forEach { maand ->
                            FilterChip(
                                selected = geselecteerdeMaanden.contains(maand),
                                onClick = {
                                    if (geselecteerdeMaanden.contains(maand)) geselecteerdeMaanden.remove(maand)
                                    else geselecteerdeMaanden.add(maand)
                                },
                                label = { Text(maand) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GrasGroen,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = DonkerGroen
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = DonkerGroen.copy(alpha = 0.5f),
                                    selectedBorderColor = GrasGroen,
                                    enabled = true,
                                    selected = geselecteerdeMaanden.contains(maand)
                                )
                            )
                        }
                    }
                }
                
                InvoerVeldMetIcoon("Snoeiadvies", snoeiAdvies, { snoeiAdvies = it }, Icons.Default.ContentCut, true)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
@Composable
fun InvoerVeldMetIcoon(
    label: String,
    waarde: String,
    onWaardeChange: (String) -> Unit,
    icoon: ImageVector,
    isMultiLine: Boolean = false
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlantDetailScherm(initialPlantId: String?, navController: NavController) {
    val auth = Firebase.auth
    val db = Firebase.firestore
    var allePlanten by remember { mutableStateOf<List<Plant>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 1. Haal de collectie op om te kunnen swipen
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid ?: return@LaunchedEffect
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            val gardenId = userDoc.getString("sharedGardenId") ?: userId

            db.collection("tuinen").document(gardenId).collection("planten")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        allePlanten = snapshot.toObjects(Plant::class.java)
                        isLoading = false
                    }
                }
        } catch (e: Exception) {
            Log.e("TuinMaat", "Fout bij laden pager: ${e.message}")
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = DonkerGroen)
        }
    } else {
        // Bepaal de positie van de geselecteerde plant
        val startIndex = allePlanten.indexOfFirst { it.firestoreId == initialPlantId }.coerceAtLeast(0)
        val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { allePlanten.size })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            val p = allePlanten[page]

            Box(modifier = Modifier.fillMaxSize().background(ZachtBeige)) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

                    // 1. Foto Header
                    Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                        if (!p.fotoUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = p.fotoUri,
                                contentDescription = p.naam,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(DonkerGroen.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocalFlorist, null, tint = DonkerGroen, modifier = Modifier.size(80.dp))
                            }
                        }

                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.statusBarsPadding().padding(16.dp).background(Color.White.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DonkerGroen)
                        }
                    }

                    // ... (Bovenkant van het scherm en Pager blijven gelijk)

// 2. Informatie Kaart
                    Surface(
                        modifier = Modifier.fillMaxSize().offset(y = (-30).dp),
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        color = ZachtBeige
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(text = p.naam, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = DonkerGroen)
                            //Text(text = , style = MaterialTheme.typography.titleMedium, color = DonkerGroen.copy(alpha = 0.6f))

                            Spacer(modifier = Modifier.height(24.dp))

                            // Badges
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                DetailBadge(Icons.Default.CalendarMonth, "Snoeimaand", p.snoeiMaand, Modifier.weight(1f)) {
                                    navController.navigate("toevoegen?plantId=${p.firestoreId}&focus=snoeimaand")
                                }
                                DetailBadge(Icons.Default.Place, "Locatie", p.locatie, Modifier.weight(1f)) {
                                    navController.navigate("toevoegen?plantId=${p.firestoreId}")
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // Omschrijving
                            SectionHeader("Omschrijving")
                            Text(
                                text = if (p.omschrijving.isNotBlank()) p.omschrijving else "Geen omschrijving beschikbaar.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DonkerGroen.copy(alpha = 0.8f),
                                lineHeight = 22.sp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Snoeiadvies
                            SectionHeader("Snoeiadvies")
                            Text(
                                text = if (p.snoeiAdvies.isNotBlank()) p.snoeiAdvies else "Geen specifiek snoeiadvies bekend.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DonkerGroen.copy(alpha = 0.8f),
                                lineHeight = 22.sp
                            )

                            Spacer(modifier = Modifier.height(120.dp)) // Extra scrollruimte voor de vaste knop
                        }
                    }
                }

                // De Bewerken knop is nu altijd in beeld onderaan
                Button(
                    onClick = { navController.navigate("toevoegen?plantId=${p.firestoreId}") },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(24.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .neumorphicShadow(shape = RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.9f),
                        contentColor = DonkerGroen
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Gegevens Bewerken", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(titel: String) {
    Text(
        text = titel,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = DonkerGroen,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun DetailBadge(icoon: ImageVector, label: String, waarde: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
        modifier = modifier.neumorphicShadow(shape = RoundedCornerShape(16.dp)),
        color = Color.White.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icoon, null, tint = GrasGroen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(waarde, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = DonkerGroen, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun InfoSectie(titel: String, inhoud: String, icoon: ImageVector) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icoon, contentDescription = null, tint = DonkerGroen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(titel, style = MaterialTheme.typography.labelLarge, color = DonkerGroen, fontWeight = FontWeight.Bold)
        }
        Text(inhoud, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp, start = 28.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SnoeiKalenderScherm(navController: NavController) {
    val db = Firebase.firestore
    val auth = Firebase.auth
    val userId = auth.currentUser?.uid ?: ""
    var planten by remember { mutableStateOf<List<Plant>>(emptyList()) }

    val maanden = listOf("Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September", "Oktober", "November", "December")
    val huidigMaandIndex = Calendar.getInstance().get(Calendar.MONTH)

    // Sorteer de maanden zodat de huidige maand bovenaan staat en we precies 1 jaar tonen
    val gesorteerdeMaanden = mutableListOf<String>()
    for (i in 0 until 12) {
        gesorteerdeMaanden.add(maanden[(huidigMaandIndex + i) % 12])
    }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            val gardenId = userDoc.getString("sharedGardenId") ?: userId
            db.collection("tuinen").document(gardenId).collection("planten")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        planten = snapshot.toObjects(Plant::class.java)
                    }
                }
        } catch (e: Exception) {
            Log.e("TuinMaat", "Fout: ${e.message}")
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(ZachtBeige).statusBarsPadding()) {
        // Header (Titel & Terug knop)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp, 16.dp)) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DonkerGroen)
            }
            Text("Snoei Kalender", style = MaterialTheme.typography.headlineSmall, color = DonkerGroen, fontWeight = FontWeight.ExtraBold)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().navigationBarsPadding(),
            state = listState,
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // We lopen door de 12 maanden heen, beginnend bij de huidige
            gesorteerdeMaanden.forEach { maandNaam ->
                val plantenVoorMaand = planten.filter { it.snoeiMaand.contains(maandNaam, ignoreCase = true) }

                if (plantenVoorMaand.isNotEmpty()) {
                    stickyHeader(key = maandNaam) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ZachtBeige)
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                .neumorphicShadow(shape = RoundedCornerShape(12.dp)),
                            color = Color.White.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = if (maandNaam == maanden[huidigMaandIndex]) "$maandNaam (Nu)" else maandNaam,
                                modifier = Modifier.padding(16.dp, 10.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = DonkerGroen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    items(items = plantenVoorMaand, key = { "${it.firestoreId}-$maandNaam" }) { plant ->
                        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)) {
                            PlantKaart(plant, navController)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}


@Composable
fun InstellingenScherm(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().background(ZachtBeige).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("Instellingen", style = MaterialTheme.typography.headlineMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            InstellingItem("Profiel bewerken", Icons.Default.Person) { navController.navigate("profiel_bewerken") }
            InstellingItem("Tuin delen", Icons.Default.Share) { navController.navigate("tuin_delen") }
            InstellingItem("Locaties beheren", Icons.Default.Place) { navController.navigate("locatiebeheer") }
            InstellingItem("Beveiliging", Icons.Default.Security) { navController.navigate("beveiliging") }
            InstellingItem("Info", Icons.Default.Info) { navController.navigate("info") }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    Firebase.auth.signOut()
                    navController.navigate("login") { popUpTo(0) }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f), contentColor = Color.Red)
            ) {
                Text("Uitloggen")
            }
        }
    }
}

@Composable
fun InfoScherm(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().background(ZachtBeige).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("Informatie", style = MaterialTheme.typography.headlineMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
        }

        Card(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Park, contentDescription = null, tint = DonkerGroen, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("TuinMaat", style = MaterialTheme.typography.headlineSmall, color = DonkerGroen, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Roland van Oel", style = MaterialTheme.typography.bodyLarge)
                Text("rvanoel@etik.com", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Bedankt voor het gebruiken van TuinMaat!", style = MaterialTheme.typography.bodySmall, color = GrasGroen, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InstellingItem(titel: String, icoon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icoon, contentDescription = null, tint = DonkerGroen)
            Spacer(modifier = Modifier.width(16.dp))
            Text(titel, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun BeveiligingsInstellingenScherm(navController: NavController) {
    val db = Firebase.firestore
    val auth = Firebase.auth
    val userId = auth.currentUser?.uid ?: ""
    val scope = rememberCoroutineScope()
    val context = LocalContext.current as FragmentActivity

    var isBiometrieIngeschakeld by remember { mutableStateOf(false) }
    var isLaden by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            try {
                val doc = db.collection("users").document(userId).get().await()
                val securityType = doc.getString("securityType") ?: "NONE"
                isBiometrieIngeschakeld = securityType == "BIOMETRIC"
            } catch (e: Exception) {
                Log.e("TuinMaat", "Fout bij laden beveiliging: ${e.message}")
            } finally {
                isLaden = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(ZachtBeige).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("Beveiliging", style = MaterialTheme.typography.headlineMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
        }

        if (isLaden) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DonkerGroen)
            }
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Biometrische beveiliging", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = isBiometrieIngeschakeld,
                        onCheckedChange = { ingeschakeld ->
                            if (ingeschakeld) {
                                // Controleer of biometrie beschikbaar is op dit toestel
                                val biometricManager = androidx.biometric.BiometricManager.from(context)
                                val canAuthenticate = biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
                                
                                if (canAuthenticate == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
                                    isBiometrieIngeschakeld = true
                                    scope.launch {
                                        db.collection("users").document(userId).update("securityType", "BIOMETRIC")
                                    }
                                } else {
                                    Toast.makeText(context, "Biometrie is niet beschikbaar of niet ingesteld op dit toestel.", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                isBiometrieIngeschakeld = false
                                scope.launch {
                                    db.collection("users").document(userId).update("securityType", "NONE")
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = GrasGroen, checkedTrackColor = GrasGroen.copy(alpha = 0.5f))
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Gebruik vingerafdruk of gezichtsherkenning om de app te openen bij inactiviteit.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun LocatieBeheerScherm(navController: NavController) {
    val db = Firebase.firestore
    val auth = Firebase.auth
    val userId = auth.currentUser?.uid ?: ""
    var locaties by remember { mutableStateOf<List<String>>(emptyList()) }
    var nieuweLocatie by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            val gardenId = userDoc.getString("sharedGardenId") ?: userId
            db.collection("tuinen").document(gardenId).get().addOnSuccessListener { doc ->
                @Suppress("UNCHECKED_CAST")
                locaties = doc.get("locaties") as? List<String> ?: listOf("Tuin")
            }.addOnFailureListener { e ->
                Log.e("TuinMaat", "Fout bij laden locaties: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e("TuinMaat", "Fout bij ophalen gardenId voor locaties: ${e.message}")
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(ZachtBeige).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("Locaties Beheren", style = MaterialTheme.typography.headlineMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                OutlinedTextField(
                    value = nieuweLocatie,
                    onValueChange = { nieuweLocatie = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nieuwe plek...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = DonkerGroen,
                        unfocusedBorderColor = DonkerGroen.copy(alpha = 0.5f)
                    )
                )
                IconButton(onClick = {
                    if (nieuweLocatie.isNotBlank()) {
                        scope.launch {
                            val userDoc = db.collection("users").document(userId).get().await()
                            val gardenId = userDoc.getString("sharedGardenId") ?: userId
                            val updatedList = locaties + nieuweLocatie
                            db.collection("tuinen").document(gardenId).set(mapOf("locaties" to updatedList), SetOptions.merge())
                            locaties = updatedList
                            nieuweLocatie = ""
                        }
                    }
                }) { Icon(Icons.Default.Add, contentDescription = null) }
            }

            Spacer(modifier = Modifier.height(16.dp))

            locaties.forEach { loc ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(loc, modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            scope.launch {
                                val userDoc = db.collection("users").document(userId).get().await()
                                val gardenId = userDoc.getString("sharedGardenId") ?: userId
                                val updatedList = locaties - loc
                                db.collection("tuinen").document(gardenId).set(mapOf("locaties" to updatedList), SetOptions.merge())
                                locaties = updatedList
                            }
                        }) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfielBewerkenScherm(navController: NavController) {
    val db = Firebase.firestore
    val auth = Firebase.auth
    val user = auth.currentUser
    val userId = user?.uid ?: ""

    var voornaam by remember { mutableStateOf("") }
    var achternaam by remember { mutableStateOf("") }
    var tuinnaam by remember { mutableStateOf("") }
    var isLaden by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (user != null) {
            try {
                val userDoc = db.collection("users").document(user.uid).get().await()
                voornaam = userDoc.getString("voornaam") ?: ""
                achternaam = userDoc.getString("achternaam") ?: ""

                val gid = userDoc.getString("sharedGardenId") ?: user.uid
                val gardenDoc = db.collection("tuinen").document(gid).get().await()
                tuinnaam = gardenDoc.getString("naam") ?: "Mijn Tuin"
            } catch (e: Exception) {
                Log.e("TuinMaat", "Fout bij laden: ${e.message}")
            }
        }
    }

    TuinAchtergrond {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DonkerGroen)
                }
                Text("Profiel Bewerken", style = MaterialTheme.typography.headlineMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
            }

            Column(modifier = Modifier.padding(24.dp)) {
                // Gebruikersgegevens
                OutlinedTextField(
                    value = voornaam,
                    onValueChange = { voornaam = it },
                    label = { Text("Voornaam") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.8f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.8f)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = achternaam,
                    onValueChange = { achternaam = it },
                    label = { Text("Achternaam") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.8f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.8f)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = tuinnaam,
                    onValueChange = { tuinnaam = it },
                    label = { Text("Tuinnaam") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.8f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.8f)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            if (user != null) {
                                isLaden = true
                                try {
                                    val userDoc = db.collection("users").document(user.uid).get().await()
                                    val gid = userDoc.getString("sharedGardenId") ?: user.uid

                                    db.collection("users").document(user.uid).update(
                                        mapOf("voornaam" to voornaam, "achternaam" to achternaam)
                                    ).await()

                                    db.collection("tuinen").document(gid).set(
                                        mapOf("naam" to tuinnaam), SetOptions.merge()
                                    ).await()

                                    Toast.makeText(context, "Profiel bijgewerkt!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    Log.e("TuinMaat", "Fout: ${e.message}")
                                } finally { isLaden = false }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DonkerGroen),
                    enabled = !isLaden
                ) {
                    if (isLaden) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Opslaan")
                }
            }
        }
    }
}

@Composable
fun TuinDelenScherm(navController: NavController) {
    val db = Firebase.firestore
    val auth = Firebase.auth
    val user = auth.currentUser
    val userId = user?.uid ?: ""

    var gardenIdToJoin by remember { mutableStateOf("") }
    var isLaden by remember { mutableStateOf(false) }
    var isGekoppeld by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (user != null) {
            val userDoc = db.collection("users").document(user.uid).get().await()
            val gid = userDoc.getString("sharedGardenId")
            isGekoppeld = gid != null && gid != user.uid
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(ZachtBeige)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DonkerGroen)
                }
                Text("Tuin Delen", style = MaterialTheme.typography.headlineMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Text("Deel je tuin", style = MaterialTheme.typography.titleLarge, color = DonkerGroen, fontWeight = FontWeight.Bold)
                Text(
                    "Geef onderstaande ID aan iemand anders om samen in jouw tuin te werken.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DonkerGroen.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Surface(
                    color = Color.White.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                        SelectionContainer {
                            Text(userId, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = DonkerGroen)
                        }
                        IconButton(onClick = {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, userId)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        }) { Icon(Icons.Default.Share, "Deel ID", tint = DonkerGroen) }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(color = DonkerGroen.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(32.dp))

                Text("Koppel aan een andere tuin", style = MaterialTheme.typography.titleLarge, color = DonkerGroen, fontWeight = FontWeight.Bold)
                Text(
                    "Vul hier de ID in van de tuin die je wilt beheren.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DonkerGroen.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = gardenIdToJoin,
                    onValueChange = { gardenIdToJoin = it },
                    label = { Text("Voer Tuin ID in") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.8f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.8f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch {
                            if (gardenIdToJoin.isNotBlank()) {
                                isLaden = true
                                try {
                                    db.collection("users").document(userId)
                                        .update("sharedGardenId", gardenIdToJoin)
                                        .await()
                                    Toast.makeText(context, "Tuin gekoppeld!", Toast.LENGTH_SHORT).show()
                                    isGekoppeld = true
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "ID niet gevonden", Toast.LENGTH_SHORT).show()
                                } finally { isLaden = false }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DonkerGroen),
                    enabled = !isLaden
                ) {
                    if (isLaden) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Koppel Tuin", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(120.dp))
            }
        }

        // Bottom fixed "Stop met delen" button
        if (isGekoppeld) {
            Button(
                onClick = {
                    scope.launch {
                        try {
                            db.collection("users").document(userId)
                                .update("sharedGardenId", FieldValue.delete())
                                .await()
                            Toast.makeText(context, "Je gebruikt nu weer je eigen tuin.", Toast.LENGTH_SHORT).show()
                            isGekoppeld = false
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Log.e("TuinMaat", "Ontkoppel fout: ${e.message}")
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(24.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .neumorphicShadow(shape = RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.9f),
                    contentColor = Color.Red.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Stop met delen (eigen tuin gebruiken)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlantenLijstScherm(navController: NavController) {
    val db = Firebase.firestore
    val auth = Firebase.auth
    val userId = auth.currentUser?.uid ?: ""
    var allePlanten by remember { mutableStateOf<List<Plant>>(emptyList()) }
    var zoekTerm by remember { mutableStateOf("") }
    var tuinnaam by remember { mutableStateOf("Laden...") }

    LaunchedEffect(Unit) {
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            val gardenId = userDoc.getString("sharedGardenId") ?: userId

            // 1. Luister naar de tuinnaam
            db.collection("tuinen").document(gardenId).addSnapshotListener { gardenDoc, _ ->
                if (gardenDoc != null && gardenDoc.exists()) {
                    tuinnaam = gardenDoc.getString("naam") ?: "Mijn Tuin"
                } else {
                    tuinnaam = "Mijn Tuin"
                }
            }

            // 2. Luister naar de planten
            db.collection("tuinen").document(gardenId).collection("planten")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        allePlanten = snapshot.toObjects(Plant::class.java).sortedBy { it.naam }
                    }
                }
        } catch (e: Exception) {
            Log.e("TuinMaat", "Fout: ${e.message}")
            tuinnaam = "Mijn Tuin"
        }
    }

    val gefilterdePlanten = allePlanten.filter { plant ->
        plant.naam.contains(zoekTerm, ignoreCase = true) ||
                plant.locatie.contains(zoekTerm, ignoreCase = true)
    }

    // Gebruik de nieuwe wrapper voor de hele pagina
    TuinAchtergrond {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DonkerGroen)
                }
                Text(
                    text = tuinnaam,
                    style = MaterialTheme.typography.headlineSmall,
                    color = DonkerGroen,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Neumorphic Zoekbalk
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .neumorphicShadow(shape = RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                // Kleur aangepast naar semi-transparant wit voor organisch effect
                color = Color.White.copy(alpha = 0.6f)
            ) {
                TextField(
                    value = zoekTerm,
                    onValueChange = { zoekTerm = it },
                    placeholder = { Text("Zoek op naam of plek...", color = DonkerGroen.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = DonkerGroen) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = DonkerGroen,
                        unfocusedTextColor = DonkerGroen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lijst met planten
            if (gefilterdePlanten.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(gefilterdePlanten.size) { index ->
                        val plant = gefilterdePlanten[index]
                        PlantKaart(plant, navController)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (allePlanten.isEmpty()) "Je hebt nog geen planten." else "Niets gevonden.",
                            color = DonkerGroen.copy(alpha = 0.5f)
                        )
                        if (allePlanten.isEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { navController.navigate("toevoegen?plantId=&focus=") },
                                colors = ButtonDefaults.buttonColors(containerColor = GrasGroen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(horizontal = 32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Voeg nu je eerste plant toe!", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Data class voor Gemini AI resultaten
data class GeminiPlantResult(
    val naam: String,
    val omschrijving: String,
    val snoeiAdvies: String,
    val snoeiMaand: String,
    val zoekTerm: String
)

// Helperfunctie voor Gemini AI plantidentificatie met Vertex AI in Firebase
suspend fun zoekPlantInfoMetAI(bitmap: Bitmap, context: android.content.Context): List<GeminiPlantResult> {
    return try {
        // Gebruik Vertex AI van Firebase
        val vertexAI = Firebase.vertexAI
        val generativeModel = vertexAI.generativeModel(
            modelName = "gemini-2.5-flash-lite"
        )

        val prompt = content {
            image(bitmap)
            text("""
                Identificeer deze plant en geef 3 mogelijke resultaten terug.
                Geef het antwoord strikt in het volgende JSON formaat als een lijst van objecten:
                [
                  {
                    "naam": "Naam van de plant",
                    "omschrijving": "Korte omschrijving van de plant",
                    "snoeiAdvies": "Kort advies over hoe te snoeien",
                    "snoeiMaand": "De beste maand(en) om te snoeien, bijv. 'Maart - April'",
                    "zoekTerm": "Eén specifieke zoekterm voor Google Afbeeldingen (bijv. 'Monstera Deliciosa')"
                  }
                ]
                Gebruik alleen de JSON structuur in je antwoord, geen extra tekst.
            """.trimIndent())
        }

        val response = withContext(Dispatchers.IO) {
            generativeModel.generateContent(prompt)
        }

        val jsonText = response.text?.replace("```json", "")?.replace("```", "")?.trim() ?: "[]"
        
        // Simpele handmatige parsing of gebruik een JSON library als die beschikbaar is
        // Voor nu een simpele extractie om externe afhankelijkheden te beperken
        parseGeminiJson(jsonText)
    } catch (e: Exception) {
        Log.e("TuinMaat", "Gemini AI Fout: ${e.message}")
        emptyList()
    }
}

fun parseGeminiJson(json: String): List<GeminiPlantResult> {
    val resultaten = mutableListOf<GeminiPlantResult>()
    try {
        // Robuustere parsing voor JSON met mogelijke line-breaks en spaties
        val objectRegex = Regex("""\{"naam"\s*:\s*"(.*?)",\s*"omschrijving"\s*:\s*"(.*?)",\s*"snoeiAdvies"\s*:\s*"(.*?)",\s*"snoeiMaand"\s*:\s*"(.*?)",\s*"zoekTerm"\s*:\s*"(.*?)"\}""", RegexOption.DOT_MATCHES_ALL)
        val matches = objectRegex.findAll(json)
        matches.forEach { match ->
            val (naam, omschrijving, snoeiAdvies, snoeiMaand, zoekTerm) = match.destructured
            resultaten.add(GeminiPlantResult(naam, omschrijving, snoeiAdvies, snoeiMaand, zoekTerm))
        }
    } catch (e: Exception) {
        Log.e("TuinMaat", "Parsing error: ${e.message}")
    }
    
    // Tweede poging: Zoek individuele velden als objecten niet in één keer matchen
    if (resultaten.isEmpty()) {
        try {
            val namen = Regex(""""naam"\s*:\s*"(.*?)"""").findAll(json).map { it.groupValues[1] }.toList()
            val omschrijvingen = Regex(""""omschrijving"\s*:\s*"(.*?)"""").findAll(json).map { it.groupValues[1] }.toList()
            val adviezen = Regex(""""snoeiAdvies"\s*:\s*"(.*?)"""").findAll(json).map { it.groupValues[1] }.toList()
            val maanden = Regex(""""snoeiMaand"\s*:\s*"(.*?)"""").findAll(json).map { it.groupValues[1] }.toList()
            val zoekTermen = Regex(""""zoekTerm"\s*:\s*"(.*?)"""").findAll(json).map { it.groupValues[1] }.toList()
            
            for (i in 0 until minOf(namen.size, 3)) {
                resultaten.add(GeminiPlantResult(
                    namen.getOrElse(i) { "" },
                    omschrijvingen.getOrElse(i) { "" },
                    adviezen.getOrElse(i) { "" },
                    maanden.getOrElse(i) { "" },
                    zoekTermen.getOrElse(i) { "" }
                ))
            }
        } catch (e: Exception) {
            Log.e("TuinMaat", "Fallback parsing error: ${e.message}")
        }
    }
    
    return resultaten.take(3)
}

fun Modifier.neumorphicShadow(
    shape: Shape = RoundedCornerShape(20.dp),
    baseColor: Color = ZachtBeige
): Modifier = this.drawBehind {
    val shadowColor = Color.Black.copy(alpha = 0.1f) // Zachte donkere schaduw
    val highlightColor = Color.White.copy(alpha = 1f) // Lichte glans

    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()

        // Donkere schaduw (rechtsonder)
        frameworkPaint.color = shadowColor.toArgb()
        frameworkPaint.setShadowLayer(25f, 12f, 12f, shadowColor.toArgb())
        canvas.drawOutline(shape.createOutline(size, layoutDirection, this), paint)

        // Lichte schaduw/glans (linksboven)
        frameworkPaint.color = highlightColor.toArgb()
        frameworkPaint.setShadowLayer(25f, -12f, -12f, highlightColor.toArgb())
        canvas.drawOutline(shape.createOutline(size, layoutDirection, this), paint)
    }
}