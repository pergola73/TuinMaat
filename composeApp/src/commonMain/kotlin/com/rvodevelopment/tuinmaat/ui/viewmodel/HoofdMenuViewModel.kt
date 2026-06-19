package com.rvodevelopment.tuinmaat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rvodevelopment.tuinmaat.repository.TuinRepository
import com.rvodevelopment.tuinmaat.repository.UserRepository
import com.rvodevelopment.tuinmaat.service.*
import kotlinx.datetime.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HoofdMenuState(
    val voornaam: String = "Tuinder",
    val tuinnaam: String = "Mijn Tuin",
    val aantalPlanten: Int = 0,
    val eigenaarNaam: String? = null,
    val tuintips: List<String> = emptyList(),
    val huidigeTipIndex: Int = 0,
    val weerBericht: WeerBericht? = null,
    val isTuintipLaden: Boolean = false,
    val gekoppeldeGid: String? = null,
    val eigenGid: String? = null,
    val actieveGid: String? = null,
    val huidigeMaand: Int = 1,
    val planten: List<String> = emptyList(),
    val isPremium: Boolean = false,
    val isEmailVerified: Boolean = true,
    val isLoading: Boolean = false,
    val toonNieuwLabel: Boolean = true,
    val toonTuintekenaar: Boolean = false,
    val toonReviewVraag: Boolean = false,
    val openReviewUrl: String? = null,
    val heeftOngelezenBerichten: Boolean = false
) {
    val huidigeTip: String get() = if (tuintips.isNotEmpty()) tuintips[huidigeTipIndex] else ""
}

class HoofdMenuViewModel(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val tuinRepository: TuinRepository,
    private val tuintipService: TuintipService,
    private val deepLinkHandler: DeepLinkHandler,
    private val premiumService: PremiumService,
    private val premiumManager: com.rvodevelopment.tuinmaat.premium.PremiumManager,
    private val agendaService: com.rvodevelopment.tuinmaat.premium.notifications.TuinAgendaService,
    private val storageService: StorageService,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _state = MutableStateFlow(HoofdMenuState())
    val state: StateFlow<HoofdMenuState> = _state.asStateFlow()

    init {
        observeUserData()
        fetchTuintip()
        observePremium()
        observeNotifications()
        checkNewFeatureDuration()
        checkFeatureVisibility()
        checkReviewRequest()
        analyticsService.logScreenView("HoofdMenu", "HoofdMenuViewModel")
        
        viewModelScope.launch {
            deepLinkHandler.checkPendingDeepLink()
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            agendaService.getNotifications().collect { notifications ->
                val ongelezen = notifications.any { !it.isRead }
                _state.update { it.copy(heeftOngelezenBerichten = ongelezen) }
            }
        }
    }

    private fun checkReviewRequest() {
        val alGereviewed = storageService.getBoolean("app_reviewed", false)
        if (!alGereviewed) {
            val aantalOpens = storageService.getInt("app_open_count", 0) + 1
            storageService.setInt("app_open_count", aantalOpens)
            
            // Toon review vraag bij de 5e keer openen
            if (aantalOpens == 5) {
                _state.update { it.copy(toonReviewVraag = true) }
                analyticsService.logEvent("review_request_shown")
            }
        }
    }

    fun markReviewDone(positive: Boolean) {
        storageService.setBoolean("app_reviewed", true)
        _state.update { it.copy(toonReviewVraag = false) }
        analyticsService.logEvent("review_response", mapOf("positive" to positive))
        
        if (positive) {
            val appId = "com.rvodevelopment.tuinmaat"
            val url = if (com.rvodevelopment.tuinmaat.getPlatform() == com.rvodevelopment.tuinmaat.PlatformType.IOS) {
                "https://apps.apple.com/app/id6738981600?action=write-review"
            } else {
                "market://details?id=$appId"
            }
            _state.update { it.copy(openReviewUrl = url) }
        }
    }

    fun reviewUrlGeopend() {
        _state.update { it.copy(openReviewUrl = null) }
    }

    fun sluitReview() {
        _state.update { it.copy(toonReviewVraag = false) }
    }

    private fun checkFeatureVisibility() {
        _state.update { it.copy(
            toonTuintekenaar = premiumManager.isFeatureVisible(com.rvodevelopment.tuinmaat.premium.PremiumFeature.GARDEN_PLANNER)
        ) }
    }

    private fun checkNewFeatureDuration() {
        // Markeer features als 'Nieuw' voor 14 dagen na release
        // TODO: Aanpassen bij elke nieuwe feature release
        val releaseDate = LocalDate(2025, 1, 10)
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val daysSinceRelease = today.toEpochDays() - releaseDate.toEpochDays()
        _state.update { it.copy(toonNieuwLabel = daysSinceRelease in 0..14) }
    }

    fun switchGarden(gardenId: String) {
        analyticsService.logEvent("switch_garden", mapOf("garden_id" to gardenId))
        viewModelScope.launch {
            val uid = _state.value.eigenGid ?: return@launch
            userRepository.setActiveGarden(uid, gardenId)
        }
    }

    private fun observePremium() {
        viewModelScope.launch {
            premiumService.isPremium.collect { isPremium ->
                _state.update { it.copy(isPremium = isPremium) }
            }
        }
    }

    private fun observeUserData() {
        viewModelScope.launch {
            authService.currentUser.collectLatest { user ->
                if (user != null) {
                    userRepository.getUserData(user.uid).collectLatest { userData ->
                        val isVerified = user.isEmailVerified || userData?.isHandmatigGeverifieerd == true
                        _state.update { it.copy(isEmailVerified = isVerified) }
                        
                        if (!isVerified) {
                            _state.update { it.copy(isLoading = false) }
                            return@collectLatest
                        }

                        if (userData != null) {
                            val activeGid = userData.activeGardenId ?: user.uid
                            val isEigenTuin = activeGid == user.uid
                            
                            _state.update { it.copy(
                                voornaam = userData.voornaam,
                                gekoppeldeGid = userData.sharedGardenId,
                                eigenGid = user.uid,
                                actieveGid = activeGid
                            ) }

                            launch {
                                tuinRepository.getTuinnaam(activeGid).collect { naam ->
                                    _state.update { it.copy(tuinnaam = naam) }
                                }
                            }

                            if (isEigenTuin) {
                                _state.update { it.copy(eigenaarNaam = null) }
                            } else {
                                launch {
                                    userRepository.getUserData(activeGid).collect { ownerData ->
                                        _state.update { it.copy(eigenaarNaam = ownerData?.voornaam) }
                                    }
                                }
                            }

                            observeGardenData(activeGid)
                        }
                    }
                }
            }
        }
    }

    private var gardenJob: kotlinx.coroutines.Job? = null
    private fun observeGardenData(gardenId: String) {
        gardenJob?.cancel()
        gardenJob = viewModelScope.launch {
            tuinRepository.getPlanten(gardenId).collect { planten ->
                val namen = planten.map { it.naam }
                val wasLeeg = _state.value.planten.isEmpty() && _state.value.tuintips.isEmpty()
                
                _state.update { it.copy(
                    aantalPlanten = planten.size,
                    planten = namen
                ) }

                if (wasLeeg && namen.isNotEmpty()) {
                    fetchTuintip()
                }
            }
        }
    }

    private fun fetchTuintip() {
        val nu = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        _state.update { it.copy(huidigeMaand = nu.monthNumber) }

        viewModelScope.launch {
            _state.update { it.copy(isTuintipLaden = true) }
            tuintipService.getActueelTuintip(_state.value.planten).onSuccess { weer ->
                _state.update { it.copy(
                    weerBericht = weer,
                    tuintips = listOf(weer.advies),
                    huidigeTipIndex = 0
                ) }
            }.onFailure {
                tuintipService.getTuintips().onSuccess { tips ->
                    _state.update { it.copy(tuintips = tips, huidigeTipIndex = 0) }
                }
            }
            _state.update { it.copy(isTuintipLaden = false) }
        }
    }

    fun reloadUserStatus() {
        viewModelScope.launch {
            authService.reloadUser()
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            authService.sendEmailVerification()
        }
    }

    fun volgendeTip() {
        analyticsService.logEvent("tuintip_volgende")
        if (_state.value.huidigeTipIndex < _state.value.tuintips.size - 1) {
            _state.update { it.copy(huidigeTipIndex = it.huidigeTipIndex + 1) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isTuintipLaden = true) }
            tuintipService.getActueelTuintip(_state.value.planten).onSuccess { weer ->
                _state.update { 
                    val nieuweTips = it.tuintips + weer.advies
                    it.copy(
                        weerBericht = weer,
                        tuintips = nieuweTips,
                        huidigeTipIndex = nieuweTips.size - 1
                    )
                }
            }
            _state.update { it.copy(isTuintipLaden = false) }
        }
    }

    fun vorigeTip() {
        analyticsService.logEvent("tuintip_vorige")
        _state.update {
            if (it.huidigeTipIndex > 0) {
                it.copy(huidigeTipIndex = it.huidigeTipIndex - 1)
            } else {
                it
            }
        }
    }
}
