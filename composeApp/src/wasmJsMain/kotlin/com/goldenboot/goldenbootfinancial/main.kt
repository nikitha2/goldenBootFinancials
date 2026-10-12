package com.goldenboot.goldenbootfinancial

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.goldenboot.goldenbootfinancial.di.initKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()
    ComposeViewport(viewportContainerId = "ComposeTarget") {
        App()
    }
}
