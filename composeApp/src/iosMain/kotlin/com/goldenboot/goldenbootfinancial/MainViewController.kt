package com.goldenboot.goldenbootfinancial

import androidx.compose.ui.window.ComposeUIViewController
import com.goldenboot.goldenbootfinancial.di.initKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController { App() }
}
