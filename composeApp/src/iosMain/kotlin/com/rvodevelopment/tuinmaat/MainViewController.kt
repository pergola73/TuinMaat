package com.rvodevelopment.tuinmaat

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    println("MainViewController: Starting App")
    App()
}
