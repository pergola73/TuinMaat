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
            // Geen foutmelding tonen op inlogscherm, maar een vriendelijke hint
            messageService.showMessage("Welkom! Log in om de gedeelde tuin te openen.")
            Result.success(Unit) // We retourneren succes omdat we het hebben opgevangen
        }
    }

    suspend fun checkPendingDeepLink() {
        val gardenId = pendingGardenId ?: return
        val user = authService.currentUser.first() ?: return
        
        val result = userRepository.updateSharedGardenId(user.uid, gardenId)
        if (result.isSuccess) {
            messageService.showMessage("De gedeelde tuin is nu gekoppeld!")
            pendingGardenId = null
        }
    }
}
