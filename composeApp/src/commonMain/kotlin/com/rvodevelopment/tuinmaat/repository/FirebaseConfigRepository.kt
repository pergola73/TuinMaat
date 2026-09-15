package com.rvodevelopment.tuinmaat.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

class FirebaseConfigRepository : ConfigRepository {
    private val firestore = Firebase.firestore
    private var cachedModel: String? = null

    override suspend fun getGeminiModel(): String {
        // Retourneer cache als we die al hebben
        cachedModel?.let { return it }

        return try {
            val snapshot = firestore.collection("config").document("app_settings").get()
            val model = snapshot.get<String?>("gemini_model") ?: "gemini-3.1-flash-lite"
            cachedModel = model
            model
        } catch (e: Exception) {
            println("FirebaseConfigRepository: Error fetching config: ${e.message}")
            "gemini-3.1-flash-lite" // Fallback
        }
    }
}
