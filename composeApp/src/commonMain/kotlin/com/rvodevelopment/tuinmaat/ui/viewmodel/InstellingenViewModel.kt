package com.rvodevelopment.tuinmaat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rvodevelopment.tuinmaat.repository.UserData
import com.rvodevelopment.tuinmaat.repository.UserRepository
import com.rvodevelopment.tuinmaat.repository.TuinRepository
import com.rvodevelopment.tuinmaat.service.*
import com.rvodevelopment.tuinmaat.getPlatform
import com.rvodevelopment.tuinmaat.PlatformType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InstellingenViewModel(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val tuinRepository: TuinRepository,
    private val sharingService: SharingService,
    private val biometricService: BiometricService,
    private val selectionService: SelectionService,
    private val deepLinkHandler: DeepLinkHandler,
    private val premiumService: PremiumService,
    private val billingService: BillingService,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    private val _viewersData = MutableStateFlow<List<UserData>>(emptyList())
    val viewersData: StateFlow<List<UserData>> = _viewersData

    private val _isBiometrieBeschikbaar = MutableStateFlow(false)
    val isBiometrieBeschikbaar: StateFlow<Boolean> = _isBiometrieBeschikbaar

    private val _isLaden = MutableStateFlow(false)
    val isLaden: StateFlow<Boolean> = _isLaden

    private val _foutMelding = MutableStateFlow<String?>(null)
    val foutMelding: StateFlow<String?> = _foutMelding

    val isPremium: StateFlow<Boolean> = premiumService.isPremium

    fun getPremiumPrice(): String {
        return billingService.getProductPrice(premiumService.premiumProductId) ?: "€2,99"
    }

    private var userDataJob: kotlinx.coroutines.Job? = null

    init {
        analyticsService.logScreenView("Instellingen", "InstellingenViewModel")
        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        val userFlow = authService.currentUser.flatMapLatest { profile ->
            if (profile != null) {
                userRepository.getUserData(profile.uid)
            } else {
                flowOf(null)
            }
        }
        
        userDataJob = viewModelScope.launch {
            _isBiometrieBeschikbaar.value = biometricService.isBiometricAvailable()
            userFlow
                .catch { 
                    println("User data flow caught error: ${it.message}")
                    emit(null) 
                }
                .collect {
                    _userData.value = it
                    if (it != null && it.sharedByUsers.isNotEmpty()) {
                        fetchViewers(it.sharedByUsers)
                    } else {
                        _viewersData.value = emptyList()
                    }
                }
        }
    }

    private fun fetchViewers(uids: List<String>) {
        viewModelScope.launch {
            val viewers = uids.mapNotNull { uid ->
                userRepository.getUserData(uid).first()
            }
            _viewersData.value = viewers
        }
    }

    fun removeViewer(viewerUid: String) {
        viewModelScope.launch {
            val profile = authService.currentUser.first()
            if (profile != null) {
                userRepository.removeViewerFromGarden(profile.uid, viewerUid)
            }
        }
    }

    fun updateProfile(voornaam: String, achternaam: String, tuinnaam: String, email: String) {
        viewModelScope.launch {
            _isLaden.value = true
            val profile = authService.currentUser.first()
            if (profile != null) {
                userRepository.updateProfile(profile.uid, voornaam, achternaam, tuinnaam, email)
                    .onFailure { _foutMelding.value = it.message }
            }
            _isLaden.value = false
        }
    }

    fun updateBiometrie(ingeschakeld: Boolean) {
        viewModelScope.launch {
            val profile = authService.currentUser.first()
            if (profile != null) {
                userRepository.updateBiometrie(profile.uid, ingeschakeld)
                    .onFailure { _foutMelding.value = it.message }
            }
        }
    }

    fun updateLocaties(locaties: List<String>, standaardLocatie: String) {
        viewModelScope.launch {
            val profile = authService.currentUser.first()
            if (profile != null) {
                userRepository.updateLocaties(profile.uid, locaties, standaardLocatie)
                    .onFailure { _foutMelding.value = it.message }
            }
        }
    }

    fun upgradeToPremium() {
        analyticsService.logEvent("click_upgrade_premium")
        viewModelScope.launch {
            _isLaden.value = true
            selectionService.markeerSysteemActie()
            billingService.purchasePremium(
                onSuccess = { 
                    analyticsService.logEvent("purchase_success")
                    selectionService.markeerSysteemActie() // Extra veiligheid voor terugkeer
                    _isLaden.value = false 
                },
                onError = { error ->
                    analyticsService.logEvent("purchase_failed", mapOf("error" to error))
                    selectionService.markeerSysteemActie() // Extra veiligheid voor terugkeer bij fout
                    _foutMelding.value = error
                    _isLaden.value = false
                }
            )
        }
    }

    fun restorePurchases() {
        analyticsService.logEvent("click_restore_purchases")
        viewModelScope.launch {
            _isLaden.value = true
            selectionService.markeerSysteemActie()
            billingService.restorePurchases(
                onSuccess = { 
                    analyticsService.logEvent("restore_success")
                    selectionService.markeerSysteemActie()
                    _isLaden.value = false 
                },
                onError = { error ->
                    analyticsService.logEvent("restore_failed", mapOf("error" to error))
                    selectionService.markeerSysteemActie()
                    _foutMelding.value = error
                    _isLaden.value = false
                }
            )
        }
    }

    fun devResetPremium() {
        viewModelScope.launch {
            premiumService.setPremium(false)
        }
    }

    fun unlinkGarden() {
        viewModelScope.launch {
            _isLaden.value = true
            val profile = authService.currentUser.first()
            if (profile != null) {
                userRepository.unlinkGarden(profile.uid)
                    .onFailure { _foutMelding.value = it.message }
            }
            _isLaden.value = false
        }
    }

    fun shareInvitation() {
        viewModelScope.launch {
            val profile = authService.currentUser.first()
            if (profile != null) {
                // Voorkom dat de app blokkeert bij het openen van het share-menu
                selectionService.markeerSysteemActie()
                val shareText = """
                    Kom je meehelpen in mijn tuin op TuinMaat? 
                    
                    Mijn Tuin Code: ${profile.uid}
                    
                    Als je de app al hebt, kun je deze code plakken bij 'Tuin Delen'.
                    Of probeer de link: https://tuinmaat.rvodevelopment.nl/join?gardenId=${profile.uid}
                """.trimIndent()
                sharingService.shareText("Uitnodiging TuinMaat", shareText)
            }
        }
    }

    fun joinGardenManually(gardenId: String) {
        viewModelScope.launch {
            _isLaden.value = true
            deepLinkHandler.handleJoinGarden(gardenId)
            _isLaden.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
        }
    }

    fun deleteAccount(reason: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLaden.value = true
            _foutMelding.value = null
            
            try {
                val profile = authService.currentUser.first()
                val currentUserData = _userData.value
                
                if (profile != null && currentUserData != null) {
                    val uid = profile.uid
                    val email = currentUserData.email
                    val voornaam = currentUserData.voornaam
                    
                    // 1. Verwijder EERST het Firebase Auth account.
                    // Firebase vereist een 'recent login' voor deze actie.
                    // Door dit als eerste te doen, voorkomen we dat we data in de database wissen
                    // terwijl de eigenlijke accountverwijdering daarna alsnog faalt.
                    authService.deleteAccount()
                        .onSuccess {
                            // Als het account weg is, ruimen we de rest op.
                            // We doen dit in een GlobalScope of NonCancellable context
                            // omdat de gebruiker nu elk moment 'uitgelogd' kan worden door Firebase.
                            kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                                try {
                                    // Stop database luisteraars
                                    userDataJob?.cancel()

                                    // Trigger e-mails
                                    userRepository.triggerDeletionEmail(email, voornaam, reason)
                                    
                                    // Wis data uit Firestore
                                    // Opmerking: Dit kan soms falen als de permissies direct verlopen,
                                    // maar we hebben de e-mails in ieder geval getriggerd.
                                    tuinRepository.deleteGardenData(uid)
                                    userRepository.deleteUserData(uid)
                                } catch (e: Exception) {
                                    println("Cleanup after deletion had issues: ${e.message}")
                                }

                                // Navigeer naar inlogscherm
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    onSuccess()
                                }
                            }
                        }
                        .onFailure { error ->
                            val msg = error.message ?: ""
                            if (msg.contains("recent-login", ignoreCase = true) || msg.contains("sensitive-operation", ignoreCase = true)) {
                                _foutMelding.value = "Om veiligheidsredenen moet je opnieuw inloggen voordat je je account kunt verwijderen. Log uit en log opnieuw in, en probeer het dan nogmaals."
                            } else {
                                _foutMelding.value = "Het verwijderen van je account is mislukt: ${error.message}"
                            }
                        }
                }
            } catch (e: Exception) {
                _foutMelding.value = "Er is een fout opgetreden: ${e.message}"
            } finally {
                _isLaden.value = false
            }
        }
    }

    private fun restartUserDataFlow() {
        userDataJob?.cancel()
        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        val userFlow = authService.currentUser.flatMapLatest { profile ->
            if (profile != null) {
                userRepository.getUserData(profile.uid)
            } else {
                flowOf(null)
            }
        }
        userDataJob = viewModelScope.launch {
            userFlow.catch { emit(null) }.collect { _userData.value = it }
        }
    }
}
