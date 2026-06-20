package com.rvodevelopment.tuinmaat.service

class IosNotificationService : NotificationService {
    override suspend fun getFcmToken(): String? {
        // FCM token fetching voor iOS via native Firebase SDK indien nodig
        // Voor nu laten we dit leeg of implementeren we later
        return null
    }
}
