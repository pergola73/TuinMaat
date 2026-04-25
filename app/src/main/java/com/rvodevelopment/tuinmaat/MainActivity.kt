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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.delay
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
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import com.google.firebase.firestore.FieldValue
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                ZachtBeige.toArgb(), ZachtBeige.toArgb()
            )
        )
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

        setContent {
            TuinMaatTheme {
                val view = LocalView.current

                // Globale instelling voor de statusbalk iconen
                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as Activity).window
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
    var achternaam by rememberSaveable { mutableStateOf("") }
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
            if (email.isNotBlank() && wachtwoord.isNotBlank() && voornaam.isNotBlank() && achternaam.isNotBlank()) {
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
                                "achternaam" to achternaam,
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

            OutlinedTextField(
                value = achternaam,
                onValueChange = { achternaam = it },
                label = { Text("Achternaam") },
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
            .padding(vertical = 4.dp) // Minder ruimte tussen kaarten
            .neumorphicShadow(shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = ZachtBeige,
        onClick = { navController.navigate("detail/${plant.firestoreId}") }
    ) {
        Row(
            modifier = Modifier.padding(12.dp), // Minder padding binnen de kaart
            verticalAlignment = Alignment.CenterVertically
        ) {
            // De container voor de afbeelding
            Surface(
                modifier = Modifier.size(50.dp), // Iets compacter
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
                        modifier = Modifier.padding(12.dp)
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

    val tuintipViewModel: TuintipViewModel = viewModel()
    val tuintip by tuintipViewModel.tuintip
    val isTuintipLaden by tuintipViewModel.isLoading
    var toonTuintip by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        tuintipViewModel.getTuintip()
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

                // De Glass-effect Badges (Planten teller) + Tuintip lampje
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(50.dp), // Pil-vorm
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                        modifier = Modifier.neumorphicShadow(shape = RoundedCornerShape(50.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.LocalFlorist,
                                contentDescription = null,
                                tint = DonkerGroen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "${aantalPlanten.intValue} Planten",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = DonkerGroen
                            )
                        }
                    }

                    if (!toonTuintip) {
                        IconButton(
                            onClick = { toonTuintip = true },
                            modifier = Modifier
                                .size(40.dp)
                                .neumorphicShadow(shape = CircleShape)
                                .background(Color.White.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = "Toon Tuintip",
                                tint = DonkerGroen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (toonTuintip) {
                TuintipCard(
                    tip = tuintip,
                    isLoading = isTuintipLaden,
                    onDismiss = { toonTuintip = false }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. De Knoppen (Menu items)
            // Tip: Zorg dat MenuKnop intern ook Color.White.copy(alpha = 0.7f) gebruikt!
            MenuKnop("Mijn Planten", Icons.AutoMirrored.Filled.List) { navController.navigate("lijst") }
            MenuKnop("Plant Toevoegen", Icons.Default.Add) { navController.navigate("toevoegen") }
            MenuKnop("Snoei Kalender", Icons.Default.CalendarToday) { navController.navigate("kalender") }
            MenuKnop("Instellingen", Icons.Default.Settings) { navController.navigate("instellingen") }

            Spacer(modifier = Modifier.height(64.dp)) // Extra ruimte onderaan voor navigatiebalk
        }
    }
}

@Composable
fun TuintipCard(tip: String, isLoading: Boolean, onDismiss: () -> Unit) {
    val tuintipViewModel: TuintipViewModel = viewModel()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = DonkerGroen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Tuintip",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DonkerGroen
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { tuintipViewModel.getVolgendeTip() },
                        modifier = Modifier.size(32.dp),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Volgende tip", tint = DonkerGroen, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Sluiten", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = DonkerGroen,
                        strokeWidth = 2.dp
                    )
                }
            } else {
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }
        }
    }
}

class TuintipViewModel : ViewModel() {
    private val _tuintip = mutableStateOf("Laden...")
    val tuintip: State<String> = _tuintip

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private var hasFetched = false

    fun getTuintip() {
        if (hasFetched) return
        hasFetched = true
        fetchTip()
    }

    fun getVolgendeTip() {
        fetchTip()
    }

    private fun fetchTip() {
        val vertexAI = Firebase.vertexAI
        val generativeModel = vertexAI.generativeModel(modelName = "gemini-2.5-flash-lite")
        
        _isLoading.value = true

        val maanden = listOf("Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September", "Oktober", "November", "December")
        val huidigeMaand = maanden[Calendar.getInstance().get(Calendar.MONTH)]
        
        val prompt = """
            Je bent een ervaren hovenier. Geef één korte, praktische tuintip voor een doe-het-zelf tuinier in Nederland. 
            De tip MOET specifiek gaan over de huidige maand ($huidigeMaand) of het huidige seizoen. 
            Geef bij voorkeur eerst tips die NU in $huidigeMaand relevant zijn.
            Houd de tip onder de 25 woorden en begin met een vrolijke emoji.
        """.trimIndent()
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(prompt)
                withContext(Dispatchers.Main) {
                    _tuintip.value = response.text ?: "Geen tip gevonden."
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _tuintip.value = "Kon geen tip ophalen. Probeer het later opnieuw."
                    _isLoading.value = false
                }
            }
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
    var wetenschappelijkeNaam by remember { mutableStateOf("") }
    var geselecteerdeLocatie by remember { mutableStateOf("") }
    var standaardLocatie by remember { mutableStateOf("") }
    var omschrijving by remember { mutableStateOf("") }
    var waterBehoefte by remember { mutableStateOf("") }
    var lichtBehoefte by remember { mutableStateOf("") }
    var voedingAdvies by remember { mutableStateOf("") }
    var ehboSignaal by remember { mutableStateOf("") }
    val geselecteerdeMaanden = remember { mutableStateListOf<String>() }
    var snoeiAdvies by remember { mutableStateOf("") }
    var beschikbareLocaties by remember { mutableStateOf<List<String>>(emptyList()) }

    // UI State
    var isLaden by remember { mutableStateOf(false) }
    var laatLocatieMenuZien by remember { mutableStateOf(false) }
    var toonTip by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(5000)
        toonTip = false
    }

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
                standaardLocatie = gardenDoc.getString("standaardLocatie") ?: ""
                
                if (bewerkPlantFirestoreId != null) {
                    isLaden = true
                    val doc = db.collection("tuinen").document(gardenId).collection("planten").document(bewerkPlantFirestoreId).get().await()
                    val plant = doc.toObject(Plant::class.java)
                    if (plant != null) {
                        naam = plant.naam
                        wetenschappelijkeNaam = plant.wetenschappelijkeNaam
                        geselecteerdeLocatie = plant.locatie
                        omschrijving = plant.omschrijving
                        waterBehoefte = plant.waterBehoefte
                        lichtBehoefte = plant.lichtBehoefte
                        voedingAdvies = plant.voedingAdvies
                        ehboSignaal = plant.ehboSignaal
                        // Parse string back to list
                        geselecteerdeMaanden.clear()
                        if (plant.snoeiMaand.isNotBlank()) {
                            val opgeslagenMaanden = plant.snoeiMaand.split(", ")
                            geselecteerdeMaanden.addAll(opgeslagenMaanden.filter { it in maandenLijst })
                        }
                        snoeiAdvies = plant.snoeiAdvies
                        bestaandeFotoUri = plant.fotoUri
                    }
                } else {
                    // Bij nieuwe plant, gebruik standaardlocatie of eerste beschikbare
                    if (standaardLocatie.isNotEmpty()) {
                        geselecteerdeLocatie = standaardLocatie
                    } else if (locs.isNotEmpty()) {
                        geselecteerdeLocatie = locs.first()
                    }
                }
            } catch (e: Exception) {
                Log.e("TuinMaat", "Fout bij laden data: ${e.message}")
            } finally {
                isLaden = false
            }
        }
    }

    var aiResultaat by remember { mutableStateOf<GeminiPlantResult?>(null) }
    var isAIBezig by remember { mutableStateOf(false) }
    val context = LocalContext.current as FragmentActivity

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { result -> 
        if (result != null) {
            bitmap = result 
            // Voer direct identificatie uit
            coroutineScope.launch {
                isAIBezig = true
                try {
                    val resultaat = identificeerPlantEnHaalInfoOp(result, context)
                    if (resultaat != null) {
                        aiResultaat = resultaat
                        naam = resultaat.naam
                        wetenschappelijkeNaam = resultaat.wetenschappelijkeNaam
                        omschrijving = resultaat.omschrijving
                        waterBehoefte = resultaat.waterBehoefte
                        lichtBehoefte = resultaat.lichtBehoefte
                        voedingAdvies = resultaat.voedingAdvies
                        ehboSignaal = resultaat.ehboSignaal
                        snoeiAdvies = resultaat.snoeiAdvies
                        geselecteerdeMaanden.clear()
                        if (resultaat.snoeiMaand.isNotBlank()) {
                            val eersteMaand = resultaat.snoeiMaand.split(",").firstOrNull()?.trim()
                            maandenLijst.firstOrNull { it.equals(eersteMaand, ignoreCase = true) }?.let {
                                geselecteerdeMaanden.add(it)
                            }
                        }
                        Toast.makeText(context, "Plant herkend via Pl@ntNet", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("TuinMaat", "Auto AI Error: ${e.message}")
                } finally {
                    isAIBezig = false
                }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val selectedBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                bitmap = selectedBitmap
                inputStream?.close()
                
                if (selectedBitmap != null) {
                    // Voer direct identificatie uit
                    coroutineScope.launch {
                        isAIBezig = true
                        try {
                            val resultaat = identificeerPlantEnHaalInfoOp(selectedBitmap, context)
                            if (resultaat != null) {
                                aiResultaat = resultaat
                                naam = resultaat.naam
                                wetenschappelijkeNaam = resultaat.wetenschappelijkeNaam
                                omschrijving = resultaat.omschrijving
                                waterBehoefte = resultaat.waterBehoefte
                                lichtBehoefte = resultaat.lichtBehoefte
                                voedingAdvies = resultaat.voedingAdvies
                                ehboSignaal = resultaat.ehboSignaal
                                snoeiAdvies = resultaat.snoeiAdvies
                                geselecteerdeMaanden.clear()
                                if (resultaat.snoeiMaand.isNotBlank()) {
                                    val eersteMaand = resultaat.snoeiMaand.split(",").firstOrNull()?.trim()
                                    maandenLijst.firstOrNull { it.equals(eersteMaand, ignoreCase = true) }?.let {
                                        geselecteerdeMaanden.add(it)
                                    }
                                }
                                Toast.makeText(context, "Plant herkend via Pl@ntNet", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("TuinMaat", "Auto AI Error: ${e.message}")
                        } finally {
                            isAIBezig = false
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TuinMaat", "Error loading image: ${e.message}")
            }
        }
    }

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
                                    "wetenschappelijkeNaam" to wetenschappelijkeNaam,
                                    "locatie" to geselecteerdeLocatie,
                                    "omschrijving" to omschrijving,
                                    "waterBehoefte" to waterBehoefte,
                                    "lichtBehoefte" to lichtBehoefte,
                                    "voedingAdvies" to voedingAdvies,
                                    "ehboSignaal" to ehboSignaal,
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

                // Tip bovenaan de foto
                androidx.compose.animation.AnimatedVisibility(
                    visible = toonTip,
                    enter = androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
                ) {
                    Surface(
                        color = DonkerGroen.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Voeg een foto toe en gebruik de AI knop",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                                    val loader = ImageLoader(context)
                                    val request = ImageRequest.Builder(context)
                                        .data(bestaandeFotoUri)
                                        .allowHardware(false)
                                        .build()
                                    val result = loader.execute(request)
                                    bitmapToProcess = (result.drawable as? BitmapDrawable)?.bitmap
                                }

                                if (bitmapToProcess != null) {
                                    val resultaat = identificeerPlantEnHaalInfoOp(bitmapToProcess, context)
                                    if (resultaat != null) {
                                        aiResultaat = resultaat
                                        naam = resultaat.naam
                                        wetenschappelijkeNaam = resultaat.wetenschappelijkeNaam
                                        omschrijving = resultaat.omschrijving
                                        waterBehoefte = resultaat.waterBehoefte
                                        lichtBehoefte = resultaat.lichtBehoefte
                                        voedingAdvies = resultaat.voedingAdvies
                                        ehboSignaal = resultaat.ehboSignaal
                                        snoeiAdvies = resultaat.snoeiAdvies
                                        
                                        // Update snoeimaanden (Alleen de 1e maand)
                                        geselecteerdeMaanden.clear()
                                        if (resultaat.snoeiMaand.isNotBlank()) {
                                            val eersteMaand = resultaat.snoeiMaand.split(",").firstOrNull()?.trim()
                                            maandenLijst.firstOrNull { it.equals(eersteMaand, ignoreCase = true) }?.let {
                                                geselecteerdeMaanden.add(it)
                                            }
                                        }
                                        Toast.makeText(context, "Plant herkend via Pl@ntNet", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Plant kon niet worden geïdentificeerd", Toast.LENGTH_SHORT).show()
                                    }
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
                        Text("Identificeer Plant", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Bronvermelding
            aiResultaat?.let { result ->
                Text(
                    text = "Bron: ${result.bron}",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
            }

            Column(modifier = Modifier.padding(24.dp)) {
                // Tip is verplaatst naar bovenop de foto
                
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
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
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
                InvoerVeldMetIcoon("Lichtbehoefte", lichtBehoefte, { lichtBehoefte = it }, Icons.Default.WbSunny)
                InvoerVeldMetIcoon("Waterbehoefte", waterBehoefte, { waterBehoefte = it }, Icons.Default.WaterDrop)
                InvoerVeldMetIcoon("Voedingsadvies", voedingAdvies, { voedingAdvies = it }, Icons.Default.Agriculture)
                InvoerVeldMetIcoon("EHBO Signaal", ehboSignaal, { ehboSignaal = it }, Icons.Default.ReportProblem)

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

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun PlantDetailScherm(initialPlantId: String?, navController: NavController) {
    val auth = Firebase.auth
    val db = Firebase.firestore
    val scope = rememberCoroutineScope()
    var allePlanten by remember { mutableStateOf<List<Plant>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf<Plant?>(null) }

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

                        IconButton(
                            onClick = { showDeleteDialog = p },
                            modifier = Modifier.statusBarsPadding().align(Alignment.TopEnd).padding(16.dp).background(Color.White.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.8f))
                        }

                        // Locatie overlay op foto
                        if (p.locatie.isNotBlank()) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 16.dp, bottom = 46.dp), // Verhoogd om boven info kaart te blijven
                                color = DonkerGroen.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Place, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(p.locatie, color = Color.White, style = MaterialTheme.typography.labelMedium)
                                }
                            }
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
                            
                            if (p.wetenschappelijkeNaam.isNotBlank()) {
                                Text(
                                    text = p.wetenschappelijkeNaam,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = DonkerGroen.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            if (p.omschrijving.isNotBlank()) {
                                Text(
                                    text = p.omschrijving,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DonkerGroen.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // 6-punts Overzicht
                            SectionHeader("Verzorging")
                            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                VerzorgingItem(Icons.Default.CalendarMonth, "Snoeimaand", p.snoeiMaand)
                                VerzorgingItem(Icons.Default.ContentCut, "Snoeiadvies", p.snoeiAdvies)
                                VerzorgingItem(Icons.Default.WbSunny, "Licht", p.lichtBehoefte)
                                VerzorgingItem(Icons.Default.WaterDrop, "Water", p.waterBehoefte)
                                VerzorgingItem(Icons.Default.Agriculture, "Voeding", p.voedingAdvies)
                                VerzorgingItem(Icons.Default.ReportProblem, "EHBO", p.ehboSignaal)
                            }

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

        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Plant Verwijderen") },
                text = { Text("Weet je zeker dat je '${showDeleteDialog?.naam}' wilt verwijderen uit je tuin? Dit kan niet ongedaan worden gemaakt.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val plantToDelete = showDeleteDialog
                            showDeleteDialog = null
                            val userId = auth.currentUser?.uid ?: return@TextButton
                            scope.launch {
                                try {
                                    val userDoc = db.collection("users").document(userId).get().await()
                                    val gardenId = userDoc.getString("sharedGardenId") ?: userId
                                    plantToDelete?.firestoreId?.let { id ->
                                        db.collection("tuinen").document(gardenId).collection("planten").document(id).delete().await()
                                    }
                                } catch (e: Exception) {
                                    Log.e("TuinMaat", "Fout bij verwijderen: ${e.message}")
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Verwijderen", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Annuleren", color = DonkerGroen)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
fun VerzorgingItem(icoon: ImageVector, label: String, waarde: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            color = DonkerGroen.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icoon, contentDescription = null, tint = DonkerGroen, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = DonkerGroen,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (waarde.isNotBlank()) waarde else "Geen info beschikbaar",
                style = MaterialTheme.typography.bodyMedium,
                color = DonkerGroen.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )
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
        modifier = Modifier.padding(bottom = 12.dp)
    )
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
    var standaardLocatie by remember { mutableStateOf("") }
    var nieuweLocatie by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            val gardenId = userDoc.getString("sharedGardenId") ?: userId
            db.collection("tuinen").document(gardenId).addSnapshotListener { doc, _ ->
                if (doc != null) {
                    @Suppress("UNCHECKED_CAST")
                    locaties = doc.get("locaties") as? List<String> ?: listOf("Tuin")
                    standaardLocatie = doc.getString("standaardLocatie") ?: ""
                }
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
                            nieuweLocatie = ""
                        }
                    }
                }) { Icon(Icons.Default.Add, contentDescription = null) }
            }

            Spacer(modifier = Modifier.height(16.dp))

            locaties.forEach { loc ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    onClick = {
                        scope.launch {
                            val userDoc = db.collection("users").document(userId).get().await()
                            val gardenId = userDoc.getString("sharedGardenId") ?: userId
                            db.collection("tuinen").document(gardenId).update("standaardLocatie", loc)
                        }
                    }
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (loc == standaardLocatie) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (loc == standaardLocatie) Color(0xFFFFD700) else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(loc, modifier = Modifier.weight(1f), fontWeight = if (loc == standaardLocatie) FontWeight.Bold else FontWeight.Normal)
                        IconButton(onClick = {
                            scope.launch {
                                val userDoc = db.collection("users").document(userId).get().await()
                                val gardenId = userDoc.getString("sharedGardenId") ?: userId
                                val updatedList = locaties - loc
                                val updates = mutableMapOf<String, Any>("locaties" to updatedList)
                                if (standaardLocatie == loc) {
                                    updates["standaardLocatie"] = ""
                                }
                                db.collection("tuinen").document(gardenId).update(updates)
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
    var locaties by remember { mutableStateOf<List<String>>(emptyList()) }
    var geselecteerdeFilterLocatie by remember { mutableStateOf("Alle") }

    LaunchedEffect(Unit) {
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            val gardenId = userDoc.getString("sharedGardenId") ?: userId

            // 1. Luister naar de tuinnaam en locaties
            db.collection("tuinen").document(gardenId).addSnapshotListener { gardenDoc, _ ->
                if (gardenDoc != null && gardenDoc.exists()) {
                    tuinnaam = gardenDoc.getString("naam") ?: "Mijn Tuin"
                    @Suppress("UNCHECKED_CAST")
                    locaties = gardenDoc.get("locaties") as? List<String> ?: listOf("Tuin")
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
        val matchesSearch = plant.naam.contains(zoekTerm, ignoreCase = true) ||
                plant.locatie.contains(zoekTerm, ignoreCase = true)
        val matchesLocation = geselecteerdeFilterLocatie == "Alle" || plant.locatie == geselecteerdeFilterLocatie
        matchesSearch && matchesLocation
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

            // Locatie filters
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Surface(
                        modifier = Modifier.neumorphicShadow(shape = RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent
                    ) {
                        FilterChip(
                            selected = geselecteerdeFilterLocatie == "Alle",
                            onClick = { geselecteerdeFilterLocatie = "Alle" },
                            label = { Text("Alle") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GrasGroen,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.6f),
                                labelColor = DonkerGroen
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = geselecteerdeFilterLocatie == "Alle",
                                borderColor = Color.Transparent,
                                selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
                items(locaties.size) { index ->
                    val loc = locaties[index]
                    Surface(
                        modifier = Modifier.neumorphicShadow(shape = RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent
                    ) {
                        FilterChip(
                            selected = geselecteerdeFilterLocatie == loc,
                            onClick = { geselecteerdeFilterLocatie = loc },
                            label = { Text(loc) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GrasGroen,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.6f),
                                labelColor = DonkerGroen
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = geselecteerdeFilterLocatie == loc,
                                borderColor = Color.Transparent,
                                selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
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
                    placeholder = { Text("Zoek op naam...", color = DonkerGroen.copy(alpha = 0.5f)) },
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
    val naam: String = "",
    val wetenschappelijkeNaam: String = "",
    val omschrijving: String = "",
    val snoeiAdvies: String = "",
    val snoeiMaand: String = "",
    val waterBehoefte: String = "",
    val lichtBehoefte: String = "",
    val voedingAdvies: String = "",
    val ehboSignaal: String = "",
    val zoekTerm: String = "",
    val bron: String = "Gemini AI"
)

// Helperfunctie voor gelaagde plantinformatie (Pl@ntNet -> Wikipedia -> Gemini)
suspend fun identificeerPlantEnHaalInfoOp(bitmap: Bitmap, context: android.content.Context): GeminiPlantResult? {
    return withContext(Dispatchers.IO) {
        try {
            // Stap 1: Pl@ntNet Identificatie
            val plantNetResult = identificeerMetPlantNet(bitmap) ?: return@withContext null
            val plantNaam = plantNetResult.first // Best scorende commonName of scientificName
            
            // Stap 2: Wikipedia Extractie
            val wikipediaInfo = haalWikipediaInfoOp(plantNaam)
            
            // Stap 3: Gemini Verrijking
            val finaleInfo = verrijkMetGemini(plantNaam, wikipediaInfo)
            
            // Bron bepalen
            val bron = if (wikipediaInfo != null) "Pl@ntNet • Wikipedia/Gemini AI" else "Pl@ntNet • Gemini AI"
            
            finaleInfo.copy(naam = plantNaam, wetenschappelijkeNaam = plantNetResult.second, bron = bron)
        } catch (e: Exception) {
            Log.e("TuinMaat", "Fout in flow: ${e.message}")
            null
        }
    }
}

private suspend fun identificeerMetPlantNet(bitmap: Bitmap): Pair<String, String>? {
    val client = OkHttpClient()
    
    // Converteer bitmap naar byte array
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
    val byteArray = stream.toByteArray()
    
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("organs", "flower") // Default organ, Pl@ntNet verwacht dit
        .addFormDataPart("images", "image.jpg", byteArray.toRequestBody("image/jpeg".toMediaType()))
        .build()

    val request = Request.Builder()
        .url("https://my-api.plantnet.org/v2/identify/all?api-key=${BuildConfig.PLANTNET_API_KEY}&lang=nl")
        .post(requestBody)
        .build()

    return try {
        val response = client.newCall(request).execute()
        val jsonResponse = response.body?.string() ?: ""
        val jsonObject = JSONObject(jsonResponse)
        val results = jsonObject.getJSONArray("results")
        
        if (results.length() > 0) {
            val bestMatch = results.getJSONObject(0)
            val species = bestMatch.getJSONObject("species")
            val scientificName = species.getString("scientificNameWithoutAuthor")
            val commonNames = species.optJSONArray("commonNames")
            val commonName = if (commonNames != null && commonNames.length() > 0) commonNames.getString(0) else scientificName
            
            Pair(commonName, scientificName)
        } else null
    } catch (e: Exception) {
        Log.e("TuinMaat", "Pl@ntNet Error: ${e.message}")
        null
    }
}

private suspend fun haalWikipediaInfoOp(naam: String): String? {
    val client = OkHttpClient()
    val url = "https://nl.wikipedia.org/api/rest_v1/page/summary/${naam.replace(" ", "_")}"
    
    val request = Request.Builder().url(url).build()
    
    return try {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val json = JSONObject(response.body?.string() ?: "")
            json.optString("extract")
        } else null
    } catch (e: Exception) {
        Log.e("TuinMaat", "Wikipedia Error: ${e.message}")
        null
    }
}

private suspend fun verrijkMetGemini(plantNaam: String, wikipediaInfo: String?): GeminiPlantResult {
    val generativeModel = Firebase.vertexAI.generativeModel(modelName = "gemini-2.5-flash-lite")
    
    val wikiText = wikipediaInfo ?: "Geen Wikipedia informatie beschikbaar."
    val prompt = """
        Je bent een hovenier. Gebruik de Wikipedia-info: $wikiText. 
        Vul aan voor $plantNaam: 
        1. Water (vingertest), 
        2. Licht (plek), 
        3. Voeding (wanneer), 
        4. EHBO (signaal), 
        5. Snoeimaanden (lijst van maanden gescheiden door komma's), 
        6. Snoei-instructie (korte hoveniers-tip max 15 woorden). 
        Als Wikipedia onvoldoende info heeft, gebruik dan je eigen kennis. 
        
        Antwoord strikt in dit JSON formaat:
        {
          "waterBehoefte": "...",
          "lichtBehoefte": "...",
          "voedingAdvies": "...",
          "ehboSignaal": "...",
          "snoeiMaand": "Maand1, Maand2",
          "snoeiAdvies": "...",
          "omschrijving": "..."
        }
        Gebruik alleen de JSON structuur, geen extra tekst. Nederlands.
    """.trimIndent()

    return try {
        val response = generativeModel.generateContent(prompt)
        val jsonText = response.text?.replace("```json", "")?.replace("```", "")?.trim() ?: "{}"
        val json = JSONObject(jsonText)
        
        GeminiPlantResult(
            naam = plantNaam,
            omschrijving = json.optString("omschrijving"),
            waterBehoefte = json.optString("waterBehoefte"),
            lichtBehoefte = json.optString("lichtBehoefte"),
            voedingAdvies = json.optString("voedingAdvies"),
            ehboSignaal = json.optString("ehboSignaal"),
            snoeiMaand = json.optString("snoeiMaand"),
            snoeiAdvies = json.optString("snoeiAdvies")
        )
    } catch (e: Exception) {
        Log.e("TuinMaat", "Gemini Verrijking Error: ${e.message}")
        GeminiPlantResult(naam = plantNaam, omschrijving = wikiText)
    }
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