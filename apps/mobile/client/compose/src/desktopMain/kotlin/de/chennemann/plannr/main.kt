package de.chennemann.plannr

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.chennemann.plannr.ui.PlannrApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "plannr-kmp",
    ) {
        PlannrApp()
    }
}