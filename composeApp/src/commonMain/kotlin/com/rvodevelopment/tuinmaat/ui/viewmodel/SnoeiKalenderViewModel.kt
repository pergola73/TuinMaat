package com.rvodevelopment.tuinmaat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rvodevelopment.tuinmaat.model.Plant
import com.rvodevelopment.tuinmaat.repository.TuinRepository
import com.rvodevelopment.tuinmaat.repository.UserRepository
import com.rvodevelopment.tuinmaat.service.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SnoeiKalenderViewModel(
    private val tuinRepository: TuinRepository,
    private val userRepository: UserRepository,
    private val authService: AuthService
) : ViewModel() {

    private val _planten = MutableStateFlow<List<Plant>>(emptyList())
    val planten: StateFlow<List<Plant>> = _planten.asStateFlow()

    init {
        loadPlanten()
    }

    private fun loadPlanten() {
        viewModelScope.launch {
            authService.currentUser.collectLatest { userProfile ->
                userProfile?.let { profile ->
                    userRepository.getUserData(profile.uid).collectLatest { userData ->
                        val gardenId = userData?.sharedGardenId ?: profile.uid
                        tuinRepository.getPlanten(gardenId).collect { plantenlijst ->
                            _planten.value = plantenlijst
                        }
                    }
                }
            }
        }
    }
}
