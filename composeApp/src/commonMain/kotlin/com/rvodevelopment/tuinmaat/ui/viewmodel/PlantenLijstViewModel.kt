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
    val planten: List<Plant> = emptyList(),
    val gefilterdePlanten: List<Plant> = emptyList(),
    val locaties: List<String> = emptyList(),
    val geselecteerdeLocatie: String = "Alle",
    val zoekTerm: String = ""
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

    private fun observeData() {
        viewModelScope.launch {
            authService.currentUser.collectLatest { user ->
                if (user == null) return@collectLatest
                userRepository.getUserData(user.uid).collect { userData ->
                    val gardenId = userData?.sharedGardenId ?: user.uid
                    
                    // Fetch planten
                    tuinRepository.getPlanten(gardenId).collect { planten ->
                        _state.update { it.copy(
                            planten = planten.sortedBy { p -> p.naam },
                            locaties = planten.map { p -> p.locatie }.distinct().filter { l -> l.isNotBlank() }
                        ) }
                        updateFilteredList()
                    }
                }
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

    private fun updateFilteredList() {
        val s = _state.value
        val gefilterd = s.planten.filter { plant ->
            val matchesSearch = plant.naam.contains(s.zoekTerm, ignoreCase = true) ||
                    plant.locatie.contains(s.zoekTerm, ignoreCase = true)
            val matchesLocation = s.geselecteerdeLocatie == "Alle" || plant.locatie == s.geselecteerdeLocatie
            matchesSearch && matchesLocation
        }
        _state.update { it.copy(gefilterdePlanten = gefilterd) }
    }
}
