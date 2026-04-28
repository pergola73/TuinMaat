package com.rvodevelopment.tuinmaat.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rvodevelopment.tuinmaat.model.Plant
import com.rvodevelopment.tuinmaat.repository.TuinRepository
import com.rvodevelopment.tuinmaat.service.AiService
import com.rvodevelopment.tuinmaat.service.AuthService
import com.rvodevelopment.tuinmaat.service.StorageService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock as KtClock

data class PlantToevoegenState(
    val plant: Plant = Plant(),
    val isLaden: Boolean = false,
    val isAIBezig: Boolean = false,
    val beschikbareLocaties: List<String> = listOf("Tuin"),
    val geselecteerdeMaanden: List<String> = emptyList(),
    val error: String? = null
)

class PlantToevoegenViewModel(
    private val authService: AuthService,
    private val tuinRepository: TuinRepository,
    private val aiService: AiService,
    private val storageService: StorageService,
    private val plantId: String?
) : ViewModel() {

    private val _state = MutableStateFlow(PlantToevoegenState())
    val state: StateFlow<PlantToevoegenState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val user = authService.currentUser.first() ?: return@launch
            _state.update { it.copy(isLaden = true) }
            
            // In a real app we would also fetch garden-specific locations
            
            if (plantId != null) {
                tuinRepository.getPlant(user.uid, plantId).collect { plant ->
                    if (plant != null) {
                        val maanden = plant.snoeiMaand.split(", ").filter { it.isNotBlank() }
                        _state.update { it.copy(
                            plant = plant,
                            geselecteerdeMaanden = maanden,
                            isLaden = false
                        ) }
                    }
                }
            } else {
                _state.update { it.copy(isLaden = false) }
            }
        }
    }

    fun updatePlant(updater: (Plant) -> Plant) {
        _state.update { it.copy(plant = updater(it.plant)) }
    }

    fun toggleMaand(maand: String) {
        val huidige = _state.value.geselecteerdeMaanden.toMutableList()
        if (huidige.contains(maand)) huidige.remove(maand)
        else huidige.add(maand)
        _state.update { it.copy(
            geselecteerdeMaanden = huidige,
            plant = it.plant.copy(snoeiMaand = huidige.joinToString(", "))
        ) }
    }

    fun identifyPlant(imageBytes: ByteArray) {
        viewModelScope.launch {
            _state.update { it.copy(isAIBezig = true) }
            aiService.identifyPlant(imageBytes).onSuccess { result ->
                _state.update { it.copy(
                    plant = it.plant.copy(
                        naam = result.naam,
                        wetenschappelijkeNaam = result.wetenschappelijkeNaam,
                        omschrijving = result.omschrijving,
                        snoeiAdvies = result.snoeiAdvies,
                        snoeiMaand = result.snoeiMaand,
                        waterBehoefte = result.waterBehoefte,
                        lichtBehoefte = result.lichtBehoefte,
                        voedingAdvies = result.voedingAdvies,
                        bemesting = result.bemesting,
                        ehboSignaal = result.ehboSignaal
                    ),
                    geselecteerdeMaanden = result.snoeiMaand.split(", ").filter { it.isNotBlank() },
                    isAIBezig = false
                ) }
            }.onFailure { e ->
                _state.update { it.copy(isAIBezig = false, error = e.message) }
            }
        }
    }

    fun savePlant(imageBytes: ByteArray?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLaden = true) }
            val user = authService.currentUser.first() ?: return@launch
            
            var plantToSave = _state.value.plant
            
            if (imageBytes != null) {
                val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                val path = "planten/${user.uid}_$timestamp.jpg"
                storageService.uploadFile(path, imageBytes).onSuccess { url ->
                    plantToSave = plantToSave.copy(fotoUri = url)
                }
            }
            
            tuinRepository.savePlant(user.uid, plantToSave).onSuccess {
                onSuccess()
            }.onFailure { e ->
                _state.update { it.copy(isLaden = false, error = e.message) }
            }
        }
    }
}
