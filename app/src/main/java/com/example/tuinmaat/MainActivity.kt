package com.example.tuinmaat

import android.Manifest
import android.app.Activity
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
import coil.compose.AsyncImage
import com.example.tuinmaat.ui.theme.DonkerGroen
import com.example.tuinmaat.ui.theme.GrasGroen
import com.example.tuinmaat.ui.theme.TuinMaatTheme
import com.example.tuinmaat.ui.theme.ZachtBeige
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
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
                            composable("toevoegen?plantId={plantId}") { backStackEntry ->
                                val id = backStackEntry.arguments?.getString("plantId")
                                PlantToevoegenScherm(navController, bewerkPlantFirestoreId = id)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZachtBeige)
            .statusBarsPadding()
            .navigationBarsPadding()
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
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; foutMelding = null },
            label = { Text("E-mailadres") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = DonkerGroen) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = wachtwoord,
            onValueChange = { wachtwoord = it; foutMelding = null },
            label = { Text("Wachtwoord") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (wachtwoordZichtbaar) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = DonkerGroen) },
            trailingIcon = {
                IconButton(onClick = { wachtwoordZichtbaar = !wachtwoordZichtbaar }) {
                    Icon(
                        if (wachtwoordZichtbaar) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = DonkerGroen
                    )
                }
            }
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
            onClick = {
                if (isRegistreren) {
                    if (email.isNotBlank() && wachtwoord.isNotBlank() && voornaam.isNotBlank()) {
                        isLaden = true
                        auth.createUserWithEmailAndPassword(email, wachtwoord)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    val profile = hashMapOf(
                                        "voornaam" to voornaam,
                                        "email" to email,
                                        "createdAt" to System.currentTimeMillis()
                                    )
                                    db.collection("users").document(user!!.uid).set(profile)
                                        .addOnSuccessListener {
                                            // Maak ook een standaard tuin aan voor de nieuwe gebruiker
                                            val tuin = hashMapOf("naam" to "Mijn Tuin")
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
            },
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { navController.navigate("detail/${plant.firestoreId}") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = plant.fotoUri,
                contentDescription = null,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(plant.naam, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DonkerGroen)
                Text(plant.locatie, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = DonkerGroen)
        }
    }
}

@Composable
fun HoofdMenu(navController: NavController) {
    val auth = Firebase.auth
    val db = Firebase.firestore
    val voornaam = remember { mutableStateOf("Tuinder") }
    val tuinnaam = remember { mutableStateOf("Mijn Tuin") }
    val aantalPlanten = remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).addSnapshotListener { userDoc, error ->
                if (error != null) {
                    Log.e("TuinMaat", "Firestore error in HoofdMenu (users): ${error.message}")
                    return@addSnapshotListener
                }
                
                if (userDoc != null && userDoc.exists()) {
                    voornaam.value = userDoc.getString("voornaam") ?: "Tuinder"
                    val gid = userDoc.getString("sharedGardenId") ?: user.uid
                    
                    db.collection("tuinen").document(gid).addSnapshotListener { gardenDoc, gError ->
                        if (gError != null) {
                            Log.e("TuinMaat", "Firestore error in HoofdMenu (tuinen): ${gError.message}")
                            return@addSnapshotListener
                        }
                        if (gardenDoc != null && gardenDoc.exists()) {
                            tuinnaam.value = gardenDoc.getString("naam") ?: "Mijn Tuin"
                        }
                    }

                    db.collection("tuinen").document(gid).collection("planten").addSnapshotListener { snapshot, pError ->
                        if (pError != null) {
                            Log.e("TuinMaat", "Firestore error in HoofdMenu (planten): ${pError.message}")
                            return@addSnapshotListener
                        }
                        aantalPlanten.intValue = snapshot?.size() ?: 0
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZachtBeige)
            .statusBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Tree Logo
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Icon(
                Icons.Default.Park,
                contentDescription = "TuinMaat Logo",
                tint = DonkerGroen,
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Hallo,", style = MaterialTheme.typography.bodyLarge, color = DonkerGroen)
                Text(voornaam.value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = DonkerGroen)
                
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    Surface(
                        color = GrasGroen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = DonkerGroen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tuinnaam.value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = DonkerGroen)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = DonkerGroen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "${aantalPlanten.intValue} planten",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = DonkerGroen
                        )
                    }
                }
            }
            Surface(
                modifier = Modifier.size(50.dp).clickable { navController.navigate("instellingen") },
                shape = CircleShape,
                color = Color.Transparent
            ) {
                // Icon verwijderd op verzoek van gebruiker, maar klikgebied behouden voor balans of weghalen
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text("Wat wil je doen?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DonkerGroen)

        Spacer(modifier = Modifier.height(24.dp))

        MenuKnop("Mijn Planten", Icons.AutoMirrored.Filled.List) { navController.navigate("lijst") }
        MenuKnop("Plant Toevoegen", Icons.Default.Add) { navController.navigate("toevoegen") }
        MenuKnop("Snoei Kalender", Icons.Default.CalendarToday) { navController.navigate("kalender") }
        MenuKnop("Instellingen", Icons.Default.Settings) { navController.navigate("instellingen") }
    }
}

@Composable
fun MenuKnop(tekst: String, icoon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = GrasGroen.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                Icon(icoon, contentDescription = null, tint = DonkerGroen, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(tekst, color = DonkerGroen, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = DonkerGroen.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantToevoegenScherm(
    navController: NavController,
    bewerkPlantFirestoreId: String? = null
) {
    val db = Firebase.firestore
    val auth = Firebase.auth
    val userId = auth.currentUser?.uid ?: ""
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // State velden overeenkomstig met Plant detail model
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var bestaandeFotoUri by remember { mutableStateOf<String?>(null) }
    var naam by remember { mutableStateOf("") }
    var geselecteerdeLocatie by remember { mutableStateOf("") }
    var omschrijving by remember { mutableStateOf("") }
    var snoeiMaand by remember { mutableStateOf("") }
    var snoeiAdvies by remember { mutableStateOf("") }
    var beschikbareLocaties by remember { mutableStateOf<List<String>>(emptyList()) }

    // Gemini AI State
    var aiSuggesties by remember { mutableStateOf<List<GeminiPlantResult>>(emptyList()) }
    var laatSuggestiesZien by remember { mutableStateOf(false) }
    var isAIBezig by remember { mutableStateOf(false) }

    // UI State
    var isLaden by remember { mutableStateOf(false) }
    var laatLocatieMenuZien by remember { mutableStateOf(false) }

    // Load locations and existing plant
    LaunchedEffect(userId, bewerkPlantFirestoreId) {
        if (userId.isNotEmpty()) {
            try {
                val userDoc = db.collection("users").document(userId).get().await()
                val gardenId = userDoc.getString("sharedGardenId") ?: userId
                
                // Load locations
                val gardenDoc = db.collection("tuinen").document(gardenId).get().await()
                @Suppress("UNCHECKED_CAST")
                val locs = gardenDoc.get("locaties") as? List<String> ?: listOf("Woonkamer", "Keuken", "Balkon")
                beschikbareLocaties = locs
                
                if (bewerkPlantFirestoreId != null) {
                    isLaden = true
                    val doc = db.collection("tuinen").document(gardenId).collection("planten").document(bewerkPlantFirestoreId).get().await()
                    val plant = doc.toObject(Plant::class.java)
                    if (plant != null) {
                        naam = plant.naam
                        geselecteerdeLocatie = plant.locatie
                        omschrijving = plant.omschrijving
                        snoeiMaand = plant.snoeiMaand
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

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { result -> if (result != null) bitmap = result }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val context = (navController.context as? Activity)
            if (context != null) {
                val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                bitmap = android.graphics.ImageDecoder.decodeBitmap(source)
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
                                    "snoeiMaand" to snoeiMaand,
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
            if (bitmap != null) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isAIBezig = true
                            try {
                                val resultaten = zoekPlantInfoMetAI(bitmap!!, context)
                                if (resultaten.isNotEmpty()) {
                                    aiSuggesties = resultaten
                                    laatSuggestiesZien = true
                                } else {
                                    Toast.makeText(context, "AI kon plant niet identificeren", Toast.LENGTH_SHORT).show()
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
                                snoeiMaand = suggestie.snoeiMaand
                                laatSuggestiesZien = false
                            },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, GrasGroen.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(suggestie.naam, fontWeight = FontWeight.Bold, color = DonkerGroen)
                                Text(suggestie.omschrijving, maxLines = 2, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    TextButton(onClick = { laatSuggestiesZien = false }) { Text("Annuleren", color = Color.Gray) }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                OutlinedTextField(value = naam, onValueChange = { naam = it }, label = { Text("Naam") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(expanded = laatLocatieMenuZien, onExpandedChange = { laatLocatieMenuZien = !laatLocatieMenuZien }) {
                    OutlinedTextField(
                        value = geselecteerdeLocatie, onValueChange = {}, readOnly = true, label = { Text("Locatie") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = DonkerGroen) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = laatLocatieMenuZien) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = laatLocatieMenuZien, onDismissRequest = { laatLocatieMenuZien = false }) {
                        beschikbareLocaties.forEach { loc ->
                            DropdownMenuItem(text = { Text(loc) }, onClick = { geselecteerdeLocatie = loc; laatLocatieMenuZien = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                InvoerVeldMetIcoon("Omschrijving", omschrijving, { omschrijving = it }, Icons.Default.Info)
                InvoerVeldMetIcoon("Beste snoeimaand", snoeiMaand, { snoeiMaand = it }, Icons.Default.CalendarMonth)
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
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DonkerGroen)
        )
    }
}

@Composable
fun PlantDetailScherm(plantId: String?, navController: NavController) {
    val db = Firebase.firestore
    val auth = Firebase.auth
    val userId = auth.currentUser?.uid ?: ""
    var plant by remember { mutableStateOf<Plant?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(plantId) {
        if (plantId != null) {
            try {
                val userDoc = db.collection("users").document(userId).get().await()
                val gardenId = userDoc.getString("sharedGardenId") ?: userId
                val doc = db.collection("tuinen").document(gardenId).collection("planten").document(plantId).get().await()
                plant = doc.toObject(Plant::class.java)
            } catch (e: Exception) {
                Log.e("TuinMaat", "Fout bij laden plant details: ${e.message}")
            }
        }
    }

    if (plant != null) {
        Column(modifier = Modifier.fillMaxSize().background(ZachtBeige).navigationBarsPadding().verticalScroll(rememberScrollState())) {
            Box {
                AsyncImage(
                    model = plant!!.fotoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(350.dp),
                    contentScale = ContentScale.Crop
                )
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.padding(16.dp).statusBarsPadding().background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(50))) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(plant!!.naam, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = DonkerGroen, modifier = Modifier.weight(1f))
                    IconButton(onClick = { navController.navigate("toevoegen?plantId=${plant!!.firestoreId}") }) {
                        Icon(Icons.Default.Edit, contentDescription = "Bewerken", tint = DonkerGroen)
                    }
                    IconButton(onClick = {
                        scope.launch {
                            val userDoc = db.collection("users").document(userId).get().await()
                            val gardenId = userDoc.getString("sharedGardenId") ?: userId
                            db.collection("tuinen").document(gardenId).collection("planten").document(plantId!!).delete().await()
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Verwijderen", tint = Color.Red)
                    }
                }

                Surface(color = GrasGroen.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(plant!!.locatie, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = DonkerGroen, style = MaterialTheme.typography.labelLarge)
                }

                Spacer(modifier = Modifier.height(24.dp))

                InfoSectie("Omschrijving", plant!!.omschrijving, Icons.Default.Info)
                InfoSectie("Beste snoeimaand", plant!!.snoeiMaand, Icons.Default.CalendarMonth)
                InfoSectie("Snoeiadvies", plant!!.snoeiAdvies, Icons.Default.ContentCut)
            }
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

@Composable
fun SnoeiKalenderScherm(navController: NavController) {
    val db = Firebase.firestore
    val auth = Firebase.auth
    val userId = auth.currentUser?.uid ?: ""
    var planten by remember { mutableStateOf<List<Plant>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            val gardenId = userDoc.getString("sharedGardenId") ?: userId
            db.collection("tuinen").document(gardenId).collection("planten").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TuinMaat", "Firestore error in SnoeiKalender: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    planten = snapshot.toObjects(Plant::class.java)
                }
            }
        } catch (e: Exception) {
            Log.e("TuinMaat", "Fout bij laden snoeikalender: ${e.message}")
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(ZachtBeige).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("Snoei Kalender", style = MaterialTheme.typography.headlineMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
        }

        val maanden = listOf("Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September", "Oktober", "November", "December")
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(maanden) { maand ->
                val plantenVoorMaand = planten.filter { it.snoeiMaand.contains(maand, ignoreCase = true) }
                if (plantenVoorMaand.isNotEmpty()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(maand, style = MaterialTheme.typography.titleLarge, color = DonkerGroen, fontWeight = FontWeight.ExtraBold)
                        plantenVoorMaand.forEach { plant ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { navController.navigate("detail/${plant.firestoreId}") },
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = plant.fotoUri,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(plant.naam, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = DonkerGroen)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.ContentCut, contentDescription = null, tint = GrasGroen, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
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
                locaties = doc.get("locaties") as? List<String> ?: listOf("Woonkamer", "Keuken", "Balkon")
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
                OutlinedTextField(value = nieuweLocatie, onValueChange = { nieuweLocatie = it }, modifier = Modifier.weight(1f), placeholder = { Text("Nieuwe plek...") })
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
    var gardenIdToJoin by remember { mutableStateOf("") }
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
                Log.e("TuinMaat", "Fout bij laden profielgegevens: ${e.message}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZachtBeige)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("Profiel Bewerken", style = MaterialTheme.typography.headlineMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(24.dp)) {
            OutlinedTextField(
                value = voornaam,
                onValueChange = { voornaam = it },
                label = { Text("Voornaam") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = achternaam,
                onValueChange = { achternaam = it },
                label = { Text("Achternaam") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = tuinnaam,
                onValueChange = { tuinnaam = it },
                label = { Text("Tuinnaam") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
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
                                Log.e("TuinMaat", "Fout bij opslaan profiel: ${e.message}")
                            } finally {
                                isLaden = false
                            }
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

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            Text("Tuin Delen", style = MaterialTheme.typography.titleLarge, color = DonkerGroen, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Deel deze ID met anderen om samen een tuin te beheren:", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    SelectionContainer {
                        Text(userId, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    }
                    val clipboard = LocalClipboardManager.current
                    IconButton(onClick = {
                        clipboard.setText(AnnotatedString(userId))
                        Toast.makeText(context, "ID gekopieerd!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = DonkerGroen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Koppel aan een andere tuin:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = gardenIdToJoin,
                onValueChange = { gardenIdToJoin = it },
                label = { Text("Voer Tuin ID in") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    scope.launch {
                        if (gardenIdToJoin.isNotBlank()) {
                            isLaden = true
                            try {
                                val targetGarden = db.collection("tuinen").document(gardenIdToJoin).get().await()
                                if (targetGarden.exists()) {
                                    db.collection("users").document(userId).update("sharedGardenId", gardenIdToJoin).await()
                                    Toast.makeText(context, "Gekoppeld aan de tuin van ${targetGarden.getString("naam") ?: "iemand anders"}!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(context, "Ongeldige Tuin ID. Deze tuin bestaat niet.", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Fout bij koppelen: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLaden = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GrasGroen),
                enabled = !isLaden
            ) {
                Text("Koppel Tuin")
            }

            TextButton(
                onClick = {
                    scope.launch {
                        db.collection("users").document(userId).update("sharedGardenId", null).await()
                        Toast.makeText(context, "Je gebruikt nu weer je eigen tuin.", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Stop met delen (eigen tuin gebruiken)", color = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun PlantenLijstScherm(navController: NavController) {
    val db = Firebase.firestore
    val auth = Firebase.auth
    val userId = auth.currentUser?.uid ?: ""
    var allePlanten by remember { mutableStateOf<List<Plant>>(emptyList()) }
    var zoekTerm by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            val gardenId = userDoc.getString("sharedGardenId") ?: userId
            db.collection("tuinen").document(gardenId).collection("planten")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("TuinMaat", "Firestore error in PlantenLijst: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        allePlanten = snapshot.toObjects(Plant::class.java)
                    }
                }
        } catch (e: Exception) {
            Log.e("TuinMaat", "Fout bij ophalen plantenlijst: ${e.message}")
        }
    }

    val gefilterdePlanten = allePlanten.filter { plant ->
        plant.naam.contains(zoekTerm, ignoreCase = true) ||
                plant.locatie.contains(zoekTerm, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().background(ZachtBeige).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("Mijn Tuin", style = MaterialTheme.typography.headlineMedium, color = DonkerGroen, fontWeight = FontWeight.Bold)
        }

        OutlinedTextField(
            value = zoekTerm,
            onValueChange = { zoekTerm = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Zoek op naam of plek...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DonkerGroen) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
        )

        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
            items(gefilterdePlanten) { plant ->
                PlantKaart(plant, navController)
            }
        }
    }
}

// Data class voor Gemini AI resultaten
data class GeminiPlantResult(
    val naam: String,
    val omschrijving: String,
    val snoeiAdvies: String,
    val snoeiMaand: String
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
                    "snoeiMaand": "De beste maand(en) om te snoeien, bijv. 'Maart - April'"
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
        val objectRegex = Regex("""\{"naam"\s*:\s*"(.*?)",\s*"omschrijving"\s*:\s*"(.*?)",\s*"snoeiAdvies"\s*:\s*"(.*?)",\s*"snoeiMaand"\s*:\s*"(.*?)"\}""", RegexOption.DOT_MATCHES_ALL)
        val matches = objectRegex.findAll(json)
        matches.forEach { match ->
            val (naam, omschrijving, snoeiAdvies, snoeiMaand) = match.destructured
            resultaten.add(GeminiPlantResult(naam, omschrijving, snoeiAdvies, snoeiMaand))
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
            
            for (i in 0 until minOf(namen.size, 3)) {
                resultaten.add(GeminiPlantResult(
                    namen.getOrElse(i) { "" },
                    omschrijvingen.getOrElse(i) { "" },
                    adviezen.getOrElse(i) { "" },
                    maanden.getOrElse(i) { "" }
                ))
            }
        } catch (e: Exception) {
            Log.e("TuinMaat", "Fallback parsing error: ${e.message}")
        }
    }
    
    return resultaten.take(3)
}
