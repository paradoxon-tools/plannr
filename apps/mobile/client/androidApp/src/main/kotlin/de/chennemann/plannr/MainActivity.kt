package de.chennemann.plannr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import de.chennemann.plannr.ui.PlannrApp

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val decorationColors: @Composable (Boolean, Color, Boolean, Color, Boolean) -> Unit =
                { darkTheme, statusBarColor, darkStatusBarIcons, navigationBarColor, darkNavigationBarIcons ->
                    DisposableEffect(darkTheme) {

                        val systemBarStyle = if (darkTheme) {
                            SystemBarStyle.dark(
                                scrim = android.graphics.Color.TRANSPARENT,
                            )
                        } else {
                            SystemBarStyle.light(
                                scrim = android.graphics.Color.TRANSPARENT,
                                darkScrim = android.graphics.Color.TRANSPARENT,
                            )
                        }

                        enableEdgeToEdge(
                            statusBarStyle = systemBarStyle,
                            navigationBarStyle = systemBarStyle
                        )
                        onDispose {}
                    }
                }

            PlannrApp(
                platformContext = applicationContext,
                decorationColors = decorationColors
            )
        }
    }
}
