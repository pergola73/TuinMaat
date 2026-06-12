package com.rvodevelopment.tuinmaat.service

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics

interface AnalyticsService {
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
    fun setUserProperty(name: String, value: String)
    fun setUserId(userId: String?)
    
    fun logAdImpression(adUnitId: String, adType: String)
    fun logScreenView(screenName: String, screenClass: String)
    fun logUserSegment(segment: String)
    
    // Fraude detectie
    fun trackPotentialFraud(type: String, details: String)
}

class FirebaseAnalyticsService : AnalyticsService {
    private val analytics get() = Firebase.analytics
    private val actionTimestamps = mutableMapOf<String, MutableList<Long>>()

    override fun logEvent(name: String, params: Map<String, Any>) {
        try {
            analytics.logEvent(name, params)
            
            // Automatische click-rate monitoring voor AdMob
            if (name == "ad_click_android" || name == "ad_click_ios") {
                checkFrequency("ad_click", 3, 60000) // Max 3 clicks per minuut
            }
        } catch (e: Exception) {
            println("Analytics Error: ${e.message}")
        }
    }

    private fun checkFrequency(action: String, maxCount: Int, timeFrameMs: Long) {
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val timestamps = actionTimestamps.getOrPut(action) { mutableListOf() }
        
        timestamps.add(now)
        timestamps.removeAll { it < now - timeFrameMs }
        
        if (timestamps.size > maxCount) {
            trackPotentialFraud("high_frequency_$action", "Count: ${timestamps.size} in ${timeFrameMs/1000}s")
        }
    }

    override fun trackPotentialFraud(type: String, details: String) {
        logEvent("suspicious_activity", mapOf(
            "fraud_type" to type,
            "details" to details,
            "platform" to com.rvodevelopment.tuinmaat.getPlatform().name
        ))
    }

    override fun setUserProperty(name: String, value: String) {
        try {
            analytics.setUserProperty(name, value)
        } catch (e: Exception) {
            println("Analytics Error: ${e.message}")
        }
    }

    override fun setUserId(userId: String?) {
        try {
            analytics.setUserId(userId)
        } catch (e: Exception) {
            println("Analytics Error: ${e.message}")
        }
    }

    override fun logAdImpression(adUnitId: String, adType: String) {
        logEvent("ad_impression_custom", mapOf(
            "ad_unit_id" to adUnitId,
            "ad_type" to adType
        ))
    }

    override fun logScreenView(screenName: String, screenClass: String) {
        logEvent("screen_view_custom", mapOf(
            "screen_name" to screenName,
            "screen_class" to screenClass
        ))
    }

    override fun logUserSegment(segment: String) {
        setUserProperty("user_segment", segment)
    }
}
