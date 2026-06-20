package com.rvodevelopment.tuinmaat

import com.rvodevelopment.tuinmaat.App
import com.rvodevelopment.tuinmaat.BuildConfig
import android.content.Intent
import android.os.Build
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.android.gms.ads.MobileAds
import com.google.firebase.messaging.FirebaseMessaging
import com.rvodevelopment.tuinmaat.service.DeepLinkHandler
import com.rvodevelopment.tuinmaat.ui.theme.ZachtBeige
import com.rvodevelopment.tuinmaat.util.ActivityProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity() {
    private val deepLinkHandler: DeepLinkHandler by inject()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM", "Notification permission granted")
        } else {
            Log.d("FCM", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ActivityProvider.setCurrentActivity(this)

        createNotificationChannel()
        askNotificationPermission()

        // Initialiseer Mobile Ads
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            MobileAds.initialize(this@MainActivity) {}
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ActivityProvider.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        ActivityProvider.onPermissionResult(requestCode, permissions, grantResults)
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Tuin-Updates"
            val descriptionText = "Berichten over onderhoud en tips voor je tuin"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("tuin_updates", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Toestemming is er al
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
