package com.rvodevelopment.tuinmaat

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rvodevelopment.tuinmaat.service.MessageService
import com.rvodevelopment.tuinmaat.ui.components.SecurityWrapper
import com.rvodevelopment.tuinmaat.ui.screens.*
import com.rvodevelopment.tuinmaat.ui.theme.TuinMaatTheme
import com.rvodevelopment.tuinmaat.ui.viewmodel.*
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun App() {
    val authService: com.rvodevelopment.tuinmaat.service.AuthService = koinInject()
    val messageService: MessageService = koinInject()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        messageService.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    TuinMaatTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            SecurityWrapper {
                val navController = rememberNavController()
                
                // Bepaal de startbestemming op basis van inlogstatus
                val startDestination = remember { 
                    if (authService.isUserLoggedIn()) "hoofdmenu" else "login"
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.padding(padding)
                ) {
                    composable("login") {
                        val viewModel: LoginViewModel = koinInject()
                        LoginScherm(
                            viewModel = viewModel,
                            onLoginSuccess = {
                                navController.navigate("hoofdmenu") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("hoofdmenu") {
                        val viewModel: HoofdMenuViewModel = koinInject()
                        HoofdMenuScherm(
                            viewModel = viewModel,
                            onNavigate = { route ->
                                navController.navigate(route)
                            }
                        )
                    }
                    composable("lijst") {
                        val viewModel: PlantenLijstViewModel = koinInject()
                        PlantenLijstScherm(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToDetail = { id -> navController.navigate("detail/$id") },
                            onNavigateToToevoegen = { navController.navigate("toevoegen") }
                        )
                    }
                    composable("detail/{plantId}") { backStackEntry ->
                        val plantId = backStackEntry.arguments?.getString("plantId")
                        val viewModel: PlantDetailViewModel = koinInject { parametersOf(plantId) }
                        PlantDetailScherm(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToEdit = { id -> navController.navigate("toevoegen?plantId=$id") },
                            onNavigateToLocaties = { navController.navigate("locatiebeheer") },
                            onNavigateToSnoeiKalender = { navController.navigate("snoeikalender") }
                        )
                    }
                    composable("toevoegen?plantId={plantId}") { backStackEntry ->
                        val plantId = backStackEntry.arguments?.getString("plantId")
                        val viewModel: PlantToevoegenViewModel = koinInject { parametersOf(plantId) }
                        PlantToevoegenScherm(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onSaveSuccess = {
                                navController.navigate("lijst") {
                                    popUpTo("hoofdmenu") { inclusive = false }
                                }
                            }
                        )
                    }
                    composable("snoeikalender") {
                        SnoeiKalenderScherm(navController = navController)
                    }
                    composable("instellingen") {
                        InstellingenScherm(navController = navController)
                    }
                    composable("profiel_bewerken") {
                        ProfielBewerkenScherm(navController = navController)
                    }
                    composable("tuin_delen") {
                        TuinDelenScherm(navController = navController)
                    }
                    composable("locatiebeheer") {
                        LocatieBeheerScherm(navController = navController)
                    }
                    composable("beveiliging") {
                        BeveiligingScherm(navController = navController)
                    }
                    composable("info") {
                        InfoScherm(navController = navController)
                    }
                }
            }
        }
    }
}
