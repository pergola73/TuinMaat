package com.rvodevelopment.tuinmaat.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SelectionService {
    private val _geselecteerdeLocatie = MutableStateFlow("Alle")
    val geselecteerdeLocatie: StateFlow<String> = _geselecteerdeLocatie.asStateFlow()

    fun updateLocatie(locatie: String) {
        _geselecteerdeLocatie.value = locatie
    }
}
