package com.rvodevelopment.tuinmaat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rvodevelopment.tuinmaat.model.Plant
import com.rvodevelopment.tuinmaat.repository.TuinRepository
import com.rvodevelopment.tuinmaat.repository.UserRepository
import com.rvodevelopment.tuinmaat.service.AuthService
import com.rvodevelopment.tuinmaat.service.SelectionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SnoeiKalenderState(
    val planten: List<Plant> = emptyList(),
    val gefilterdePlanten: List<Plant> = emptyList(),
    val locaties: List<String> = emptyList(),
    val geselecteerdeLocatie: String = "Alle",
    val tuinnaam: String = "Laden...",
    val eigenaarNaam: String? = null,
    val isPremium: Boolean = false
)

class SnoeiKalenderViewModel(
    private val tuinRepository: TuinRepository,
    private val userRepository: UserRepository,
    private val authService: AuthService,
    private val selectionService: SelectionService,
    private val premiumService: com.rvodevelopment.tuinmaat.service.PremiumService
) : ViewModel() {

    private val _state = MutableStateFlow(SnoeiKalenderState())
    val state: StateFlow<SnoeiKalenderState> = _state.asStateFlow()

    private val _planten = MutableStateFlow<List<Plant>>(emptyList())
    val planten: StateFlow<List<Plant>> = _planten.asStateFlow()

    init {
        loadData()
        observeSelection()
        observePremium()
    }

    private fun observePremium() {
        viewModelScope.launch {
            premiumService.isPremium.collect { isPremium ->
                _state.update { it.copy(isPremium = isPremium) }
            }
        }
    }

    private fun observeSelection() {
        viewModelScope.launch {
            selectionService.geselecteerdeLocatie.collect { locatie ->
                _state.update { it.copy(geselecteerdeLocatie = locatie) }
                updateFilteredList()
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun loadData() {
        viewModelScope.launch {
            authService.currentUser
                .filterNotNull()
                .flatMapLatest { user ->
                    userRepository.getUserData(user.uid)
                }
                .filterNotNull()
                .flatMapLatest { userData ->
                    val gardenId = userData.activeGardenId ?: userData.sharedGardenId ?: userData.id
                    val isEigenTuin = gardenId == userData.id

                    if (isEigenTuin) {
                        combine(
                            tuinRepository.getTuinnaam(gardenId),
                            tuinRepository.getPlanten(gardenId)
                        ) { tuinnaam, planten ->
                            Triple(tuinnaam, null, planten)
                        }
                    } else {
                        combine(
                            tuinRepository.getTuinnaam(gardenId),
                            userRepository.getUserData(gardenId),
                            tuinRepository.getPlanten(gardenId)
                        ) { tuinnaam, ownerData, planten ->
                            Triple(tuinnaam, ownerData?.voornaam, planten)
                        }
                    }
                }
                .collect { (tuinnaam, eigenaar, plantenlijst) ->
                    _planten.value = plantenlijst
                    _state.update { it.copy(
                        planten = plantenlijst,
                        locaties = plantenlijst.asSequence().map { p -> p.locatie }.distinct().filter { l -> l.isNotBlank() }.toList(),
                        tuinnaam = tuinnaam,
                        eigenaarNaam = eigenaar
                    ) }
                    updateFilteredList()
                }
        }
    }

    fun onLocatieSelectie(locatie: String) {
        selectionService.updateLocatie(locatie)
    }

    private fun updateFilteredList() {
        val s = _state.value
        val gefilterd = s.planten.filter { plant ->
            (s.geselecteerdeLocatie == "Alle") || (plant.locatie == s.geselecteerdeLocatie)
        }
        _state.update { it.copy(gefilterdePlanten = gefilterd) }
    }
}
