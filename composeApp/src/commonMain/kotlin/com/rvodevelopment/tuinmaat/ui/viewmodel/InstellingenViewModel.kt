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
    private val sharingService: com.rvodevelopment.tuinmaat.service.SharingService
) : ViewModel() {

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

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
            userFlow.collect {
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
}
