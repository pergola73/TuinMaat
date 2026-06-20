package com.rvodevelopment.tuinmaat.service

import kotlinx.coroutines.flow.Flow

interface NotificationService {
    suspend fun getFcmToken(): String?
}
