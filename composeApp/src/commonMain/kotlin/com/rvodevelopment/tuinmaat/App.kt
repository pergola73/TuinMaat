package com.rvodevelopment.tuinmaat

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rvodevelopment.tuinmaat.service.MessageService
import com.rvodevelopment.tuinmaat.ui.components.SecurityWrapper
import com.rvodevelopment.tuinmaat.ui.screens.*
import com.rvodevelopment.tuinmaat.ui.theme.TuinMaatTheme
import com.rvodevelopment.tuinmaat.ui.viewmodel.*
import com.rvodevelopment.tuinmaat.premium.health.DrTuinmaatScherm
import com.rvodevelopment.tuinmaat.premium.health.DrTuinmaatViewModel
import com.rvodevelopment.tuinmaat.premium.notifications.TuinAgendaScherm
import com.rvodevelopment.tuinmaat.premium.notifications.TuinAgendaViewModel
import com.rvodevelopment.tuinmaat.premium.planner.GardenPlannerScherm
import com.rvodevelopment.tuinmaat.premium.planner.GardenPlannerViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf

@OptIn(KoinExperimentalAPI::class)
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
        SecurityWrapper {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            // Bepaal de startbestemming op basis van inlogstatus
            val startDestination = remember { 
                if (authService.isUserLoggedIn()) "hoofdmenu" else "login"
            }

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    val toonBottomBar = currentRoute != "login" && authService.isUserLoggedIn()
                    if (toonBottomBar) {
                        com.rvodevelopment.tuinmaat.ui.components.TuinMaatBottomBar(
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                // Navigatie logica voor bottom bar
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        // Vermijd opstapelen van bestemmingen
                                        popUpTo("hoofdmenu") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                ) {
                    composable("login") {
                        val viewModel: LoginViewModel = koinViewModel()
                        LoginScherm(
                            viewModel = viewModel,
                        ) {
                            navController.navigate("hoofdmenu") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                    composable("hoofdmenu") {
                        val viewModel: HoofdMenuViewModel = koinViewModel()
                        HoofdMenuScherm(
                            viewModel = viewModel,
                        ) { route ->
                            navController.navigate(route)
                        }
                    }
                    composable("lijst") {
                        val viewModel: PlantenLijstViewModel = koinViewModel()
                        PlantenLijstScherm(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToDetail = { id -> navController.navigate("detail/$id") },
                        ) { navController.navigate("toevoegen") }
                    }
                    composable("detail/{plantId}") { backStackEntry ->
                        val plantId = backStackEntry.arguments?.getString("plantId")
                        val viewModel: PlantDetailViewModel = koinViewModel { parametersOf(plantId) }
                        PlantDetailScherm(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToEdit = { id -> navController.navigate("toevoegen?plantId=$id") },
                            onNavigateToDrTuinmaat = { id -> navController.navigate("drtuinmaat/$id") },
                            onNavigateToLocaties = { navController.navigate("locatiebeheer") },
                            onNavigateToAgenda = { navController.navigate("actiecentrum") }
                        )
                    }
                    composable("drtuinmaat") {
                        val viewModel: DrTuinmaatViewModel = koinViewModel()
                        DrTuinmaatScherm(
                            viewModel = viewModel,
                            plantName = null,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("drtuinmaat/{plantName}") { backStackEntry ->
                        val plantName = backStackEntry.arguments?.getString("plantName")
                        val viewModel: DrTuinmaatViewModel = koinViewModel()
                        DrTuinmaatScherm(
                            viewModel = viewModel,
                            plantName = plantName,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("toevoegen?plantId={plantId}") { backStackEntry ->
                        val plantId = backStackEntry.arguments?.getString("plantId")
                        val viewModel: PlantToevoegenViewModel = koinViewModel { parametersOf(plantId) }
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
                    composable("actiecentrum") {
                        val viewModel: TuinAgendaViewModel = koinViewModel()
                        TuinAgendaScherm(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToPlant = { plantId -> navController.navigate("detail/$plantId") }
                        )
                    }
                    composable("tuintekenaar") {
                        val viewModel: GardenPlannerViewModel = koinViewModel()
                        GardenPlannerScherm(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
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
