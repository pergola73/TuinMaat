package com.rvodevelopment.tuinmaat.repository

import kotlinx.coroutines.flow.Flow

data class UserData(
    val voornaam: String,
    val achternaam: String,
    val email: String,
    val tuinnaam: String = "Mijn Tuin",
    val sharedGardenId: String? = null,
    val biometrieIngeschakeld: Boolean = false,
    val locaties: List<String> = listOf("Tuin", "Balkon", "Kas"),
    val standaardLocatie: String = "Tuin"
)

interface UserRepository {
    fun getUserData(uid: String): Flow<UserData?>
    suspend fun updateSharedGardenId(uid: String, sharedGardenId: String?): Result<Unit>
    suspend fun updateProfile(uid: String, voornaam: String, achternaam: String, tuinnaam: String): Result<Unit>
    suspend fun updateBiometrie(uid: String, ingeschakeld: Boolean): Result<Unit>
    suspend fun updateLocaties(uid: String, locaties: List<String>, standaardLocatie: String): Result<Unit>
    suspend fun unlinkGarden(uid: String): Result<Unit>
}
