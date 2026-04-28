package com.rvodevelopment.tuinmaat

import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.rvodevelopment.tuinmaat.service.DeepLinkHandler
import com.rvodevelopment.tuinmaat.ui.theme.ZachtBeige
import com.rvodevelopment.tuinmaat.util.ActivityProvider
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity() {
    private val deepLinkHandler: DeepLinkHandler by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                ZachtBeige.toArgb(), ZachtBeige.toArgb()
            )
        )
        super.onCreate(savedInstanceState)
        ActivityProvider.setCurrentActivity(this)

        Firebase.appCheck.installAppCheckProviderFactory(
            if (BuildConfig.DEBUG) {
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
        )

        setContent {
            App()
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val appLinkData = intent?.data
        if (appLinkData != null) {
            val gardenId = appLinkData.getQueryParameter("gardenId")
            if (gardenId != null) {
                lifecycleScope.launch {
                    deepLinkHandler.handleJoinGarden(gardenId)
                }
            }
        }
    }
}

