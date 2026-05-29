package com.rvodevelopment.tuinmaat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rvodevelopment.tuinmaat.model.Plant
import com.rvodevelopment.tuinmaat.repository.TuinRepository
import com.rvodevelopment.tuinmaat.service.AuthService
import com.rvodevelopment.tuinmaat.service.StorageService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PlantDetailState(
    val planten: List<Plant> = emptyList(),
    val isLoading: Boolean = true,
    val initialIndex: Int = 0,
    val toonBeheerLocatiesTip: Boolean = false,
    val toonSnoeiKalenderTip: Boolean = false
)

class PlantDetailViewModel(
    private val authService: AuthService,
    private val tuinRepository: TuinRepository,
    private val storageService: StorageService,
    private val initialPlantId: String?
) : ViewModel() {

    private val _state = MutableStateFlow(PlantDetailState())
    val state: StateFlow<PlantDetailState> = _state.asStateFlow()

    init {
        loadPlanten()
        checkTips()
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

    private fun loadPlanten() {
        viewModelScope.launch {
            authService.currentUser.collectLatest { user ->
                if (user == null) return@collectLatest
                tuinRepository.getPlanten(user.uid).collect { planten ->
                    val index = planten.indexOfFirst { it.firestoreId == initialPlantId }.coerceAtLeast(0)
                    _state.update { it.copy(
                        planten = planten,
                        isLoading = false,
                        initialIndex = index
                    ) }
                }
            }
        }
    }

    fun deletePlant(plant: Plant) {
        viewModelScope.launch {
            val user = authService.currentUser.first() ?: return@launch
            tuinRepository.deletePlant(user.uid, plant.firestoreId)
        }
    }
}
