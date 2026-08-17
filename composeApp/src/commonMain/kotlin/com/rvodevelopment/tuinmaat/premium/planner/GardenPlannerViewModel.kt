package com.rvodevelopment.tuinmaat.premium.planner

import com.rvodevelopment.tuinmaat.model.Plant
import com.rvodevelopment.tuinmaat.repository.TuinRepository
import com.rvodevelopment.tuinmaat.service.AuthService
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PlacedPlant(
    val id: String,
    val plant: Plant,
    val x: Float,
    val y: Float
)

data class GardenPlannerState(
    val isLoading: Boolean = false,
    val availablePlanten: List<Plant> = emptyList(),
    val placedPlanten: List<PlacedPlant> = emptyList(),
    val selectedPlant: Plant? = null,
    val gardenWidth: Float = 10f, // meters
    val gardenHeight: Float = 10f, // meters
    val scale: Float = 60f // pixels per meter
)

class GardenPlannerViewModel(
    premiumManager: com.rvodevelopment.tuinmaat.premium.PremiumManager,
    private val tuinRepository: TuinRepository,
    private val authService: AuthService
) : com.rvodevelopment.tuinmaat.premium.BasePremiumViewModel(premiumManager) {
    
    private val _state = MutableStateFlow(GardenPlannerState())
    val state: StateFlow<GardenPlannerState> = _state.asStateFlow()

    init {
        loadPlanten()
    }

    private fun loadPlanten() {
        viewModelScope.launch {
            authService.currentUser.filterNotNull().collectLatest { user ->
                tuinRepository.getPlanten(user.uid).collect { planten ->
                    _state.update { it.copy(availablePlanten = planten) }
                }
            }
        }
    }

    fun selectPlant(plant: Plant) {
        _state.update { it.copy(selectedPlant = plant) }
    }

    fun placePlant(x: Float, y: Float) {
        val selected = _state.value.selectedPlant ?: return
        val newPlaced = PlacedPlant(
            id = kotlinx.datetime.Clock.System.now().toEpochMilliseconds().toString(),
            plant = selected,
            x = x,
            y = y
        )
        _state.update { it.copy(
            placedPlanten = it.placedPlanten + newPlaced,
            selectedPlant = null
        ) }
    }

    fun removePlacedPlant(id: String) {
        _state.update { it.copy(
            placedPlanten = it.placedPlanten.filter { p -> p.id != id }
        ) }
    }

    fun updateGardenSize(width: Float, height: Float) {
        _state.update { it.copy(gardenWidth = width, gardenHeight = height) }
    }

    fun reset() {
        _state.update { it.copy(placedPlanten = emptyList(), selectedPlant = null) }
    }
}
