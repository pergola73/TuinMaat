package com.rvodevelopment.tuinmaat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rvodevelopment.tuinmaat.model.Plant
import com.rvodevelopment.tuinmaat.repository.TuinRepository
import com.rvodevelopment.tuinmaat.repository.UserRepository
import com.rvodevelopment.tuinmaat.service.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PlantDetailState(
    val planten: List<Plant> = emptyList(),
    val isLoading: Boolean = true,
    val initialIndex: Int = 0,
    val toonBeheerLocatiesTip: Boolean = false,
    val toonSnoeiKalenderTip: Boolean = false,
    val eigenaarNaam: String? = null
)

class PlantDetailViewModel(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val tuinRepository: TuinRepository,
    private val storageService: StorageService,
    private val selectionService: SelectionService,
    private val analyticsService: AnalyticsService,
    private val initialPlantId: String?
) : ViewModel() {

    private val _state = MutableStateFlow(PlantDetailState())
    val state: StateFlow<PlantDetailState> = _state.asStateFlow()

    init {
        loadPlanten()
        checkTips()
        analyticsService.logScreenView("PlantDetail", "PlantDetailViewModel")
        initialPlantId?.let { analyticsService.logEvent("view_plant_detail", mapOf("plant_id" to it)) }
    }

    private fun checkTips() {
        // Gebruik dezelfde sleutel voor persistentie als de andere ViewModels
        val locatiesGezien = storageService.getBoolean("tip_beheer_locaties_gezien", false)
        val snoeiGezien = storageService.getBoolean("tip_snoeikalender_gezien", false)

        if (!locatiesGezien) {
            _state.update { it.copy(toonBeheerLocatiesTip = true) }
        } else if (!snoeiGezien) {
            _state.update { it.copy(toonSnoeiKalenderTip = true) }
        }
    }

    fun dismissBeheerLocatiesTip() {
        _state.update { it.copy(toonBeheerLocatiesTip = false) }
        storageService.setBoolean("tip_beheer_locaties_gezien", true)
    }

    fun dismissSnoeiKalenderTip() {
        _state.update { it.copy(toonSnoeiKalenderTip = false) }
        storageService.setBoolean("tip_snoeikalender_gezien", true)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun loadPlanten() {
        viewModelScope.launch {
            val userFlow = authService.currentUser.filterNotNull()
                .flatMapLatest { user -> userRepository.getUserData(user.uid) }
                .filterNotNull()

            combine(
                userFlow,
                selectionService.geselecteerdeLocatie,
                selectionService.zoekTerm
            ) { userData, locatie, zoekTerm ->
                Triple(userData, locatie, zoekTerm)
            }.flatMapLatest { (userData, locFilter, termFilter) ->
                val gardenId = userData.activeGardenId ?: userData.sharedGardenId ?: userData.id
                val isEigenTuin = gardenId == userData.id

                val plantsFlow = if (isEigenTuin) {
                    tuinRepository.getPlanten(gardenId).map { it to null }
                } else {
                    combine(
                        tuinRepository.getPlanten(gardenId),
                        userRepository.getUserData(gardenId)
                    ) { planten, ownerData ->
                        planten to ownerData?.voornaam
                    }
                }

                plantsFlow.map { (planten, eigenaar) ->
                    val gefilterd = planten.filter { plant ->
                        val matchesSearch = plant.naam.contains(termFilter, ignoreCase = true) ||
                                plant.locatie.contains(termFilter, ignoreCase = true)
                        val matchesLocation = (locFilter == "Alle") || (plant.locatie == locFilter)
                        matchesSearch && matchesLocation
                    }.sortedBy { it.naam }
                    gefilterd to eigenaar
                }
            }.collect { (gefilterdePlanten, eigenaar) ->
                val index = gefilterdePlanten.indexOfFirst { it.firestoreId == initialPlantId }.coerceAtLeast(0)
                _state.update { it.copy(
                    planten = gefilterdePlanten,
                    isLoading = false,
                    initialIndex = index,
                    eigenaarNaam = eigenaar
                ) }
            }
        }
    }

    fun deletePlant(plant: Plant) {
        viewModelScope.launch {
            val user = authService.currentUser.first() ?: return@launch
            val userData = userRepository.getUserData(user.uid).first()
            val gardenId = userData?.activeGardenId ?: userData?.sharedGardenId ?: user.uid
            tuinRepository.deletePlant(gardenId, plant.firestoreId)
        }
    }
}
