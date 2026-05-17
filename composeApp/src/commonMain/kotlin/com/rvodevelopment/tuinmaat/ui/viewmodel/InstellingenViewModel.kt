package com.rvodevelopment.tuinmaat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rvodevelopment.tuinmaat.repository.UserData
import com.rvodevelopment.tuinmaat.repository.UserRepository
import com.rvodevelopment.tuinmaat.service.AuthService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InstellingenViewModel(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val tuinRepository: com.rvodevelopment.tuinmaat.repository.TuinRepository,
    private val sharingService: com.rvodevelopment.tuinmaat.service.SharingService,
    private val biometricService: com.rvodevelopment.tuinmaat.service.BiometricService
) : ViewModel() {

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    private val _isBiometrieBeschikbaar = MutableStateFlow(false)
    val isBiometrieBeschikbaar: StateFlow<Boolean> = _isBiometrieBeschikbaar

    private val _isLaden = MutableStateFlow(false)
    val isLaden: StateFlow<Boolean> = _isLaden

    private val _foutMelding = MutableStateFlow<String?>(null)
    val foutMelding: StateFlow<String?> = _foutMelding

    private var userDataJob: kotlinx.coroutines.Job? = null

    init {
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
                }
        }
    }

    fun updateProfile(voornaam: String, achternaam: String, tuinnaam: String) {
        viewModelScope.launch {
            _isLaden.value = true
            val profile = authService.currentUser.first()
            if (profile != null) {
                userRepository.updateProfile(profile.uid, voornaam, achternaam, tuinnaam)
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
                val shareText = "Kom je meehelpen in mijn tuin op TuinMaat? Klik op de link om te koppelen: https://tuinmaat.rvodevelopment.com/join?gardenId=${profile.uid}"
                sharingService.shareText("Uitnodiging TuinMaat", shareText)
            }
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
