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
}

class FirebaseAnalyticsService : AnalyticsService {
    // Gebruik een lazy delegate of getter om te voorkomen dat Firebase te vroeg wordt aangeroepen
    private val analytics get() = Firebase.analytics

    override fun logEvent(name: String, params: Map<String, Any>) {
        try {
            analytics.logEvent(name, params)
        } catch (e: Exception) {
            println("Analytics Error: ${e.message}")
        }
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
