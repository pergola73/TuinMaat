package com.rvodevelopment.tuinmaat.service

interface SharingService {
    fun shareText(title: String, text: String)
    fun openUrl(url: String)
}
