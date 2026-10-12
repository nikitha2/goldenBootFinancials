package com.goldenboot.goldenbootfinancial

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource
import com.goldenboot.goldenbootfinancial.di.initKoin
import goldenbootfinancial.composeapp.generated.resources.Res
import goldenbootfinancial.composeapp.generated.resources.ic_app_icon
import java.awt.Taskbar
import javax.imageio.ImageIO


fun main() = application {
    // Set macOS dock icon (replaces default Java icon)
    try {
        val iconStream = Thread.currentThread().contextClassLoader
            .getResourceAsStream("ic_app_icon.png")
        if (iconStream != null) {
            val image = ImageIO.read(iconStream)
            if (Taskbar.isTaskbarSupported()) {
                Taskbar.getTaskbar().iconImage = image
            }
        }
    } catch (_: Exception) {
        // Taskbar API not supported on this platform
    }

    initKoin()
    Window(
        onCloseRequest = ::exitApplication,
        title = "goldenbootfinancial",
        icon = painterResource(Res.drawable.ic_app_icon)
    ) {
        App()
    }
}
