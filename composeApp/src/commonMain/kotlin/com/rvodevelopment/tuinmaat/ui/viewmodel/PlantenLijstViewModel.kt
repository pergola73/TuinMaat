package com.rvodevelopment.tuinmaat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rvodevelopment.tuinmaat.model.Plant
import com.rvodevelopment.tuinmaat.repository.TuinRepository
import com.rvodevelopment.tuinmaat.repository.UserRepository
import com.rvodevelopment.tuinmaat.service.AuthService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PlantenLijstState(
    val tuinnaam: String = "Laden...",
    val eigenaarNaam: String? = null,
    val planten: List<Plant> = emptyList(),
    val gefilterdePlanten: List<Plant> = emptyList(),
    val locaties: List<String> = emptyList(),
    val geselecteerdeLocatie: String = "Alle",
    val zoekTerm: String = "",
    val isZoekenZichtbaar: Boolean = false,
)

class PlantenLijstViewModel(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val tuinRepository: TuinRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlantenLijstState())
    val state: StateFlow<PlantenLijstState> = _state.asStateFlow()

    init {
        observeData()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeData() {
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
                .collect { (tuinnaam, eigenaar, planten) ->
                    _state.update { it.copy(
                        tuinnaam = tuinnaam,
                        eigenaarNaam = eigenaar,
                        planten = planten.sortedBy { p -> p.naam },
                        locaties = planten.asSequence().map { p -> p.locatie }.distinct().filter { l -> l.isNotBlank() }.toList()
                    ) }
                    updateFilteredList()
                }
        }
    }

    fun onZoekTermChange(term: String) {
        _state.update { it.copy(zoekTerm = term) }
        updateFilteredList()
    }

    fun onLocatieSelectie(locatie: String) {
        _state.update { it.copy(geselecteerdeLocatie = locatie) }
        updateFilteredList()
    }

    fun toggleZoekveld() {
        _state.update { 
            val nieuwZichtbaar = !it.isZoekenZichtbaar
            it.copy(isZoekenZichtbaar = nieuwZichtbaar) 
        }
        if (!_state.value.isZoekenZichtbaar) {
            onZoekTermChange("")
        }
    }

    private fun updateFilteredList() {
        val s = _state.value
        val gefilterd = s.planten.filter { plant ->
            val matchesSearch = plant.naam.contains(s.zoekTerm, ignoreCase = true) ||
                    plant.locatie.contains(s.zoekTerm, ignoreCase = true)
            val matchesLocation = (s.geselecteerdeLocatie == "Alle") || (plant.locatie == s.geselecteerdeLocatie)
            matchesSearch && matchesLocation
        }
        _state.update { it.copy(gefilterdePlanten = gefilterd) }
    }
}
