package com.rvodevelopment.tuinmaat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rvodevelopment.tuinmaat.repository.TuinRepository
import com.rvodevelopment.tuinmaat.repository.UserRepository
import com.rvodevelopment.tuinmaat.service.AuthService
import com.rvodevelopment.tuinmaat.service.TuintipService
import com.rvodevelopment.tuinmaat.service.WeerBericht
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HoofdMenuState(
    val voornaam: String = "Tuinder",
    val tuinnaam: String = "Mijn Tuin",
    val aantalPlanten: Int = 0,
    val eigenaarNaam: String? = null,
    val tuintips: List<String> = emptyList(),
    val huidigeTipIndex: Int = 0,
    val weerBericht: WeerBericht? = null,
    val isTuintipLaden: Boolean = false,
    val gekoppeldeGid: String? = null,
    val eigenGid: String? = null,
    val actieveGid: String? = null,
    val huidigeMaand: Int = 1,
    val planten: List<String> = emptyList()
) {
    val huidigeTip: String get() = if (tuintips.isNotEmpty()) tuintips[huidigeTipIndex] else ""
}

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
                val namen = planten.map { it.naam }
                val wasLeeg = _state.value.planten.isEmpty() && _state.value.tuintips.isEmpty()
                
                _state.update { it.copy(
                    aantalPlanten = planten.size,
                    planten = namen
                ) }

                // Als we nog geen tips hebben en de planten zijn geladen, haal dan een relevante tip op
                if (wasLeeg && namen.isNotEmpty()) {
                    fetchTuintip()
                }
            }
        }
    }

    private fun fetchTuintip() {
        val nu = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        _state.update { it.copy(huidigeMaand = nu.monthNumber) }

        viewModelScope.launch {
            _state.update { it.copy(isTuintipLaden = true) }
            
            // Haal actuele AI tip op, rekening houdend met de planten
            tuintipService.getActueelTuintip(_state.value.planten).onSuccess { weer ->
                _state.update { it.copy(
                    weerBericht = weer,
                    tuintips = listOf(weer.advies),
                    huidigeTipIndex = 0
                ) }
            }.onFailure {
                // Fallback naar statische tips
                tuintipService.getTuintips().onSuccess { tips ->
                    _state.update { it.copy(tuintips = tips, huidigeTipIndex = 0) }
                }
            }

            _state.update { it.copy(isTuintipLaden = false) }
        }
    }

    fun volgendeTip() {
        if (_state.value.huidigeTipIndex < _state.value.tuintips.size - 1) {
            _state.update { it.copy(huidigeTipIndex = it.huidigeTipIndex + 1) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isTuintipLaden = true) }
            tuintipService.getActueelTuintip(_state.value.planten).onSuccess { weer ->
                _state.update { 
                    val nieuweTips = it.tuintips + weer.advies
                    it.copy(
                        weerBericht = weer,
                        tuintips = nieuweTips,
                        huidigeTipIndex = nieuweTips.size - 1
                    )
                }
            }
            _state.update { it.copy(isTuintipLaden = false) }
        }
    }

    fun vorigeTip() {
        _state.update {
            if (it.huidigeTipIndex > 0) {
                it.copy(huidigeTipIndex = it.huidigeTipIndex - 1)
            } else {
                it
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
