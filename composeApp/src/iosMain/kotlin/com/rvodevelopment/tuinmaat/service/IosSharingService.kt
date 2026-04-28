package com.rvodevelopment.tuinmaat.service

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

class IosSharingService : SharingService {
    override fun shareText(title: String, text: String) {
        val activityViewController = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null
        )
        
        val window = UIApplication.sharedApplication.keyWindow
        val rootViewController = window?.rootViewController
        
        rootViewController?.presentViewController(
            viewControllerToPresent = activityViewController,
            animated = true,
            completion = null
        )
    }
}
