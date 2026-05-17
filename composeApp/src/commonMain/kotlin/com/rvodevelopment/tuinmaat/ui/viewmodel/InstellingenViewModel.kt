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

    init {
        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        val userFlow = authService.currentUser.flatMapLatest { profile ->
            if (profile != null) {
                userRepository.getUserData(profile.uid)
            } else {
                flowOf(null)
            }
        }
        
        viewModelScope.launch {
            _isBiometrieBeschikbaar.value = biometricService.isBiometricAvailable()
            userFlow
                .catch { emit(null) } // Voorkom crash bij verwijderen account of verlies van permissies
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
            try {
                val profile = authService.currentUser.first()
                val currentUserData = _userData.value
                
                if (profile != null && currentUserData != null) {
                    val uid = profile.uid
                    val email = currentUserData.email
                    val voornaam = currentUserData.voornaam
                    
                    // 1. Trigger e-mails (voordat data weg is)
                    userRepository.triggerDeletionEmail(email, voornaam, reason)
                    
                    // 2. Verwijder tuindata
                    tuinRepository.deleteGardenData(uid)
                    
                    // 3. Verwijder userdata
                    userRepository.deleteUserData(uid)
                    
                    // Stop de loader even voor de laatste stap
                    _isLaden.value = false

                    // 4. Verwijder Firebase Auth account
                    // Belangrijk: hierna is de sessie direct ongeldig
                    authService.deleteAccount()
                        .onSuccess {
                            onSuccess()
                        }
                        .onFailure { 
                            _foutMelding.value = "Het verwijderen van je account is mislukt. Mogelijk moet je opnieuw inloggen om deze actie te bevestigen."
                        }
                }
            } catch (e: Exception) {
                _foutMelding.value = "Er is een fout opgetreden: ${e.message}"
            } finally {
                _isLaden.value = false
            }
        }
    }
}
