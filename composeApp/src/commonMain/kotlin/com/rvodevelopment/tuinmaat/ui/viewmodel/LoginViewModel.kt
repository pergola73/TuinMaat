package com.rvodevelopment.tuinmaat.ui.viewmodel

import com.rvodevelopment.tuinmaat.service.AuthService
import com.rvodevelopment.tuinmaat.service.BiometricService
import com.rvodevelopment.tuinmaat.service.StorageService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

class LoginViewModel(
    private val authService: AuthService,
    private val biometricService: BiometricService,
    private val storageService: StorageService,
    private val tuinRepository: com.rvodevelopment.tuinmaat.repository.TuinRepository
) : ViewModel() {

    private val _email = MutableStateFlow(storageService.getString("remembered_email", ""))
    val email: StateFlow<String> = _email

    private val _wachtwoord = MutableStateFlow("")
    val wachtwoord: StateFlow<String> = _wachtwoord

    private val _voornaam = MutableStateFlow("")
    val voornaam: StateFlow<String> = _voornaam

    private val _achternaam = MutableStateFlow("")
    val achternaam: StateFlow<String> = _achternaam

    private val _isRegistreren = MutableStateFlow(false)
    val isRegistreren: StateFlow<Boolean> = _isRegistreren

    private val _wachtwoordZichtbaar = MutableStateFlow(false)
    val wachtwoordZichtbaar: StateFlow<Boolean> = _wachtwoordZichtbaar

    private val _onthoudEmail = MutableStateFlow(storageService.getBoolean("should_remember_email", false))
    val onthoudEmail: StateFlow<Boolean> = _onthoudEmail

    private val _biometrieIngeschakeld = MutableStateFlow(storageService.getBoolean("biometric_enabled", false))
    val biometrieIngeschakeld: StateFlow<Boolean> = _biometrieIngeschakeld

    private val _isLaden = MutableStateFlow(false)
    val isLaden: StateFlow<Boolean> = _isLaden

    private val _foutMelding = MutableStateFlow<String?>(null)
    val foutMelding: StateFlow<String?> = _foutMelding

    fun onEmailChanged(value: String) { _email.value = value; _foutMelding.value = null }
    fun onWachtwoordChanged(value: String) { _wachtwoord.value = value; _foutMelding.value = null }
    fun onVoornaamChanged(value: String) { _voornaam.value = value }
    fun onAchternaamChanged(value: String) { _achternaam.value = value }
    fun toggleRegistreren() { _isRegistreren.value = !_isRegistreren.value; _foutMelding.value = null }
    fun toggleWachtwoordZichtbaarheid() { _wachtwoordZichtbaar.value = !_wachtwoordZichtbaar.value }
    fun toggleOnthoudEmail(value: Boolean) { 
        _onthoudEmail.value = value
        storageService.setBoolean("should_remember_email", value)
    }
    fun toggleBiometrie(value: Boolean) {
        if (value && !biometricService.isBiometricAvailable()) return
        _biometrieIngeschakeld.value = value
        storageService.setBoolean("biometric_enabled", value)
    }

    fun voerActieUit(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (_isRegistreren.value) {
                if (_email.value.isBlank() || _wachtwoord.value.isBlank() || _voornaam.value.isBlank() || _achternaam.value.isBlank()) {
                    _foutMelding.value = "Vul alle velden in."
                    return@launch
                }
                _isLaden.value = true
                authService.signUp(_email.value, _wachtwoord.value, _voornaam.value, _achternaam.value)
                    .onSuccess { profile ->
                        savePrefs()
                        // Na registratie migratie draaien (ook al is er waarschijnlijk nog niks)
                        tuinRepository.migrateLegacyData(profile.uid, profile.uid)
                        onSuccess()
                    }
                    .onFailure { _foutMelding.value = it.message }
            } else {
                if (_email.value.isBlank() || _wachtwoord.value.isBlank()) {
                    _foutMelding.value = "Vul e-mail en wachtwoord in."
                    return@launch
                }
                _isLaden.value = true
                authService.signIn(_email.value, _wachtwoord.value)
                    .onSuccess { profile ->
                        savePrefs()
                        // Migreer data na inloggen
                        tuinRepository.migrateLegacyData(profile.uid, profile.uid)
                        onSuccess()
                    }
                    .onFailure { _foutMelding.value = it.message }
            }
            _isLaden.value = false
        }
    }

    private fun savePrefs() {
        if (_onthoudEmail.value) {
            storageService.setString("remembered_email", _email.value)
        } else {
            storageService.remove("remembered_email")
        }
    }

    fun loginWithGoogle(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLaden.value = true
            authService.signInWithGoogle()
                .onSuccess { profile ->
                    // Migreer data na inloggen
                    tuinRepository.migrateLegacyData(profile.uid, profile.uid)
                    onSuccess()
                }
                .onFailure { _foutMelding.value = it.message }
            _isLaden.value = false
        }
    }

    fun herstelWachtwoord() {
        if (_email.value.isBlank()) {
            _foutMelding.value = "Vul je e-mailadres in."
            return
        }
        viewModelScope.launch {
            _isLaden.value = true
            authService.sendPasswordResetEmail(_email.value)
                .onSuccess { _foutMelding.value = "Herstellink verzonden naar ${_email.value}" }
                .onFailure { _foutMelding.value = it.message }
            _isLaden.value = false
        }
    }

    fun tryAutoBiometric(onSuccess: () -> Unit) {
        if (_biometrieIngeschakeld.value) {
            viewModelScope.launch {
                biometricService.authenticate().onSuccess {
                    // In a real app, you'd securely store credentials and retrieve them here
                    // For now, assume authService.currentUser will handle the session if valid
                    // or trigger a specific login flow.
                    onSuccess()
                }
            }
        }
    }
}
