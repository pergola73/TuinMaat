package com.rvodevelopment.tuinmaat.service

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class AndroidNotificationService : NotificationService {
    override suspend fun getFcmToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            null
        }
    }
}
