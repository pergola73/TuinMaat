package com.rvodevelopment.tuinmaat.service

import com.rvodevelopment.tuinmaat.repository.UserRepository
import kotlinx.coroutines.flow.first

class DeepLinkHandler(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val messageService: MessageService
) {
    private var pendingGardenId: String? = null

    suspend fun handleJoinGarden(gardenId: String): Result<Unit> {
        val user = authService.currentUser.first()
        return if (user != null) {
            val result = userRepository.updateSharedGardenId(user.uid, gardenId)
            if (result.isSuccess) {
                messageService.showMessage("Tuin succesvol gekoppeld!")
            } else {
                messageService.showMessage("Koppelen mislukt: ${result.exceptionOrNull()?.message}")
            }
            result
        } else {
            pendingGardenId = gardenId
            messageService.showMessage("Log in om de tuin te koppelen")
            Result.failure(Exception("Gebruiker niet ingelogd"))
        }
    }

    suspend fun checkPendingDeepLink() {
        val gardenId = pendingGardenId ?: return
        val user = authService.currentUser.first() ?: return
        
        val result = userRepository.updateSharedGardenId(user.uid, gardenId)
        if (result.isSuccess) {
            messageService.showMessage("Tuin succesvol gekoppeld!")
            pendingGardenId = null
        }
    }
}
