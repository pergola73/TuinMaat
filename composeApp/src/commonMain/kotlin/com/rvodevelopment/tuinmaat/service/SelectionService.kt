package com.rvodevelopment.tuinmaat.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SelectionService(private val storageService: StorageService) {
    private val _geselecteerdeLocatie = MutableStateFlow("Alle")
    val geselecteerdeLocatie: StateFlow<String> = _geselecteerdeLocatie.asStateFlow()

    fun updateLocatie(locatie: String) {
        _geselecteerdeLocatie.value = locatie
    }

    // Gebruik StorageService voor persistentie bij process death (bijv. tijdens camera gebruik)
    var isVerwachtSysteemActie: Boolean
        get() = storageService.getBoolean("expect_system_action", false)
        set(value) = storageService.setBoolean("expect_system_action", value)

    fun markeerSysteemActie() {
        isVerwachtSysteemActie = true
    }

    fun resetSysteemActie() {
        isVerwachtSysteemActie = false
    }
}
