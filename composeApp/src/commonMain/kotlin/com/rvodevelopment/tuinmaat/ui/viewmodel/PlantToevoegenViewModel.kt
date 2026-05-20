package com.rvodevelopment.tuinmaat.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rvodevelopment.tuinmaat.model.Plant
import com.rvodevelopment.tuinmaat.repository.TuinRepository
import com.rvodevelopment.tuinmaat.repository.UserRepository
import com.rvodevelopment.tuinmaat.service.AiService
import com.rvodevelopment.tuinmaat.service.AuthService
import com.rvodevelopment.tuinmaat.service.MediaService
import com.rvodevelopment.tuinmaat.service.StorageService
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
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
    val error: String? = null,
    val infoBericht: String? = null,
    val selectedImageBytes: ByteArray? = null,
    val eigenaarNaam: String? = null
)

class PlantToevoegenViewModel(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val tuinRepository: TuinRepository,
    private val aiService: AiService,
    private val storageService: StorageService,
    private val mediaService: MediaService,
    private val client: HttpClient,
    private val plantId: String?
) : ViewModel() {

    private val _state = MutableStateFlow(PlantToevoegenState())
    val state: StateFlow<PlantToevoegenState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val profile = authService.currentUser.first() ?: return@launch
            _state.update { it.copy(isLaden = true) }
            
            // Haal gebruikersgegevens op voor locaties en gardenId
            val userData = userRepository.getUserData(profile.uid).first()
            if (userData != null) {
                val gardenId = userData.activeGardenId ?: userData.sharedGardenId ?: profile.uid
                val isEigenTuin = gardenId == profile.uid

                if (!isEigenTuin) {
                    val ownerData = userRepository.getUserData(gardenId).first()
                    _state.update { it.copy(eigenaarNaam = ownerData?.voornaam) }
                }

                _state.update { 
                    it.copy(
                        beschikbareLocaties = userData.locaties,
                        // Als het een nieuwe plant is, gebruik de standaardlocatie
                        plant = if (plantId == null && it.plant.locatie.isBlank()) 
                            it.plant.copy(locatie = userData.standaardLocatie) 
                        else it.plant
                    )
                }

                // Als we een plantId hebben, haal de plantgegevens op
                if (plantId != null) {
                    tuinRepository.getPlant(gardenId, plantId).collect { plant ->
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
            } else {
                _state.update { it.copy(isLaden = false) }
            }
        }
    }

    fun updatePlant(updater: (Plant) -> Plant) {
        _state.update { it.copy(plant = updater(it.plant)) }
    }

    fun toggleMaand(maand: String) {
        // We gebruiken nu slechts 1 snoeimaand
        val nieuweSelectie = listOf(maand)
        _state.update { it.copy(
            geselecteerdeMaanden = nieuweSelectie,
            plant = it.plant.copy(snoeiMaand = maand)
        ) }
    }

    fun identifyPlant(imageBytes: ByteArray) {
        viewModelScope.launch {
            _state.update { it.copy(isAIBezig = true, error = null, infoBericht = null) }
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
                        ehboSignaal = result.ehboSignaal,
                        bron = result.bron
                    ),
                    geselecteerdeMaanden = if (result.snoeiMaand.isNotBlank()) listOf(result.snoeiMaand) else emptyList(),
                    isAIBezig = false,
                    infoBericht = "Plant succesvol herkend!"
                ) }
            }.onFailure { e ->
                _state.update { it.copy(isAIBezig = false, error = "Herkenning mislukt: ${e.message}") }
            }
        }
    }

    fun reIdentify() {
        viewModelScope.launch {
            val bytes = _state.value.selectedImageBytes
            if (bytes != null) {
                identifyPlant(bytes)
            } else {
                val uri = _state.value.plant.fotoUri
                if (!uri.isNullOrBlank()) {
                    _state.update { it.copy(isAIBezig = true, error = null) }
                    try {
                        val downloadedBytes = client.get(uri).body<ByteArray>()
                        _state.update { it.copy(selectedImageBytes = downloadedBytes) }
                        identifyPlant(downloadedBytes)
                    } catch (e: Exception) {
                        _state.update { it.copy(isAIBezig = false, error = "Afbeelding ophalen mislukt: ${e.message}") }
                    }
                }
            }
        }
    }

    fun savePlant(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLaden = true) }
            val profile = authService.currentUser.first() ?: return@launch
            
            val userData = userRepository.getUserData(profile.uid).first()
            val gardenId = userData?.activeGardenId ?: userData?.sharedGardenId ?: profile.uid

            var plantToSave = _state.value.plant
            val imageBytes = _state.value.selectedImageBytes
            
            if (imageBytes != null) {
                val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                val path = "planten/${profile.uid}_$timestamp.jpg"
                storageService.uploadFile(path, imageBytes).onSuccess { url ->
                    plantToSave = plantToSave.copy(fotoUri = url)
                }
            }
            
            tuinRepository.savePlant(gardenId, plantToSave).onSuccess {
                onSuccess()
            }.onFailure { e ->
                _state.update { it.copy(isLaden = false, error = e.message) }
            }
        }
    }

    fun deletePlant(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLaden = true) }
            val profile = authService.currentUser.first() ?: return@launch
            val userData = userRepository.getUserData(profile.uid).first()
            val gardenId = userData?.activeGardenId ?: userData?.sharedGardenId ?: profile.uid
            val plantId = _state.value.plant.firestoreId
            
            if (plantId.isNotEmpty()) {
                tuinRepository.deletePlant(gardenId, plantId).onSuccess {
                    onSuccess()
                }.onFailure { e ->
                    _state.update { it.copy(isLaden = false, error = e.message) }
                }
            }
        }
    }

    fun pickImage() {
        viewModelScope.launch {
            mediaService.pickImage()?.let { bytes ->
                _state.update { it.copy(selectedImageBytes = bytes) }
                identifyPlant(bytes)
            }
        }
    }

    fun takePhoto() {
        viewModelScope.launch {
            mediaService.takePhoto()?.let { bytes ->
                _state.update { it.copy(selectedImageBytes = bytes) }
                identifyPlant(bytes)
            }
        }
    }
}
