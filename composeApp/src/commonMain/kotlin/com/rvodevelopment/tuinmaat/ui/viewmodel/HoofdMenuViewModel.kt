package com.rvodevelopment.tuinmaat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rvodevelopment.tuinmaat.repository.TuinRepository
import com.rvodevelopment.tuinmaat.repository.UserRepository
import com.rvodevelopment.tuinmaat.service.AuthService
import com.rvodevelopment.tuinmaat.service.TuintipService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HoofdMenuState(
    val voornaam: String = "Tuinder",
    val tuinnaam: String = "Mijn Tuin",
    val aantalPlanten: Int = 0,
    val eigenaarNaam: String? = null,
    val tuintip: String = "",
    val isTuintipLaden: Boolean = false,
    val gekoppeldeGid: String? = null,
    val eigenGid: String? = null,
    val actieveGid: String? = null
)

class HoofdMenuViewModel(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val tuinRepository: TuinRepository,
    private val tuintipService: TuintipService
) : ViewModel() {

    private val _state = MutableStateFlow(HoofdMenuState())
    val state: StateFlow<HoofdMenuState> = _state.asStateFlow()

    init {
        observeUserData()
        fetchTuintip()
    }

    private fun observeUserData() {
        viewModelScope.launch {
            authService.currentUser.collectLatest { user ->
                if (user != null) {
                    userRepository.getUserData(user.uid).collectLatest { userData ->
                        if (userData != null) {
                            val activeGid = userData.sharedGardenId ?: user.uid
                            _state.update { it.copy(
                                voornaam = userData.voornaam,
                                tuinnaam = userData.tuinnaam,
                                gekoppeldeGid = userData.sharedGardenId,
                                eigenGid = user.uid,
                                actieveGid = activeGid
                            ) }
                            observeGardenData(activeGid)
                        }
                    }
                }
            }
        }
    }

    private var gardenJob: kotlinx.coroutines.Job? = null
    private fun observeGardenData(gardenId: String) {
        gardenJob?.cancel()
        gardenJob = viewModelScope.launch {
            tuinRepository.getPlanten(gardenId).collect { planten ->
                _state.update { it.copy(aantalPlanten = planten.size) }
            }
        }
    }

    private fun fetchTuintip() {
        viewModelScope.launch {
            _state.update { it.copy(isTuintipLaden = true) }
            tuintipService.getTuintip().onSuccess { tip ->
                _state.update { it.copy(tuintip = tip, isTuintipLaden = false) }
            }.onFailure {
                _state.update { it.copy(isTuintipLaden = false) }
            }
        }
    }
    
    fun switchGarden(gardenId: String) {
        viewModelScope.launch {
            val uid = _state.value.eigenGid ?: return@launch
            userRepository.updateSharedGardenId(uid, gardenId)
        }
    }
}
