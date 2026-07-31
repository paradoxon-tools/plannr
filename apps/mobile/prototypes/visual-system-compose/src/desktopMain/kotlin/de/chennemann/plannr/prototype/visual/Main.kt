package de.chennemann.plannr.prototype.visual

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(width = 460.dp, height = 900.dp),
        title = "Plannr reversible monochrome visual system — prototype",
    ) {
        VisualSystemPrototypeApp()
    }
}
