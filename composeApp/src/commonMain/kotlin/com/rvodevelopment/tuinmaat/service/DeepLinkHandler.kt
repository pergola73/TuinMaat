package com.rvodevelopment.tuinmaat.service

import com.rvodevelopment.tuinmaat.repository.UserRepository
import kotlinx.coroutines.flow.first

class DeepLinkHandler(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val messageService: MessageService
) {
    suspend fun handleJoinGarden(gardenId: String): Result<Unit> {
        val user = authService.currentUser.first()
        return if (user != null) {
            val result = userRepository.updateSharedGardenId(user.uid, gardenId)
            if (result.isSuccess) {
                messageService.showMessage("Tuin succesvol gekoppeld!")
            } else {
                messageService.showMessage("Fout bij koppelen: ${result.exceptionOrNull()?.message}")
            }
            result
        } else {
            Result.failure(Exception("Gebruiker niet ingelogd"))
        }
    }
}
