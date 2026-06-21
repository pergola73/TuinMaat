package com.rvodevelopment.tuinmaat.premium.health

import com.rvodevelopment.tuinmaat.premium.BasePremiumViewModel
import com.rvodevelopment.tuinmaat.premium.PremiumManager
import com.rvodevelopment.tuinmaat.model.Diagnosis
import com.rvodevelopment.tuinmaat.repository.TuinRepository
import com.rvodevelopment.tuinmaat.repository.UserRepository
import com.rvodevelopment.tuinmaat.service.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class DrTuinmaatState(
    val isLaden: Boolean = false,
    val isScanning: Boolean = false,
    val resultaat: AiDiseaseResult? = null,
    val error: String? = null,
    val geselecteerdeFoto: ByteArray? = null,
    val historie: List<Diagnosis> = emptyList(),
    val toonPremiumDialog: Boolean = false
)

class DrTuinmaatViewModel(
    premiumManager: PremiumManager,
    private val drTuinmaatService: DrTuinmaatService,
    private val mediaService: MediaService,
    private val selectionService: com.rvodevelopment.tuinmaat.service.SelectionService,
    private val tuinRepository: TuinRepository,
    private val userRepository: UserRepository,
    private val authService: AuthService
) : BasePremiumViewModel(premiumManager) {

    private val _state = MutableStateFlow(DrTuinmaatState())
    val state: StateFlow<DrTuinmaatState> = _state.asStateFlow()

    init {
        loadHistory()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadHistory() {
        viewModelScope.launch {
            authService.currentUser.flatMapLatest { user ->
                if (user != null) {
                    userRepository.getUserData(user.uid).flatMapLatest { userData ->
                        val gardenId = userData?.activeGardenId ?: user.uid
                        tuinRepository.getDiagnoses(gardenId)
                    }
                } else {
                    flowOf(emptyList())
                }
            }.collect { diagnoses ->
                _state.update { it.copy(historie = diagnoses) }
            }
        }
    }

    fun diagnose(imageBytes: ByteArray, plantName: String? = null, plantId: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(geselecteerdeFoto = imageBytes, isScanning = true, error = null, resultaat = null) }
            
            // Toon de scanner animatie minimaal 2 seconden voor het "echte" laden begint
            kotlinx.coroutines.delay(2000)
            
            _state.update { it.copy(isScanning = false, isLaden = true) }

            drTuinmaatService.diagnosePlant(imageBytes, plantName)
                .onSuccess { res ->
                    _state.update { it.copy(isLaden = false, resultaat = res) }
                    
                    // Alleen opslaan in historie als de gebruiker premium is
                    if (isPremium.value) {
                        saveToHistory(res, imageBytes, plantId)
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLaden = false, error = e.message) }
                }
        }
    }

    private fun saveToHistory(res: AiDiseaseResult, imageBytes: ByteArray, plantId: String?) {
        viewModelScope.launch {
            val user = authService.currentUser.first() ?: return@launch
            val userData = userRepository.getUserData(user.uid).first()
            val gardenId = userData?.activeGardenId ?: user.uid
            
            val diagnosis = Diagnosis(
                timestamp = Clock.System.now().toEpochMilliseconds(),
                ziekteNaam = res.ziekteNaam,
                omschrijving = res.omschrijving,
                advies = res.advies,
                referentieFoto = res.referentieFoto,
                plantId = plantId
            )
            
            tuinRepository.saveDiagnosis(gardenId, diagnosis)
        }
    }

    fun toonPaywall() {
        _state.update { it.copy(toonPremiumDialog = true) }
    }

    fun sluitPaywall() {
        _state.update { it.copy(toonPremiumDialog = false) }
    }

    fun maakFoto(plantName: String? = null) {
        viewModelScope.launch {
            if (mediaService.requestCameraPermission()) {
                selectionService.markeerSysteemActie()
                mediaService.takePhoto()?.let { bytes ->
                    diagnose(bytes, plantName)
                }
            } else {
                _state.update { it.copy(error = "Geen toegang tot camera") }
            }
        }
    }

    fun kiesFoto(plantName: String? = null) {
        viewModelScope.launch {
            selectionService.markeerSysteemActie()
            mediaService.pickImage()?.let { bytes ->
                diagnose(bytes, plantName)
            }
        }
    }
    
    fun reset() {
        _state.update { DrTuinmaatState() }
    }
}
