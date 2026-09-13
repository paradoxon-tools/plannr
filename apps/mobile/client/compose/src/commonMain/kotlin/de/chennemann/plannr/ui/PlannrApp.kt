package de.chennemann.plannr.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import de.chennemann.plannr.di.dependencyGraph
import de.chennemann.plannr.ui.screen.root.RootScreen
import de.chennemann.plannr.ui.theme.DecorationColors
import de.chennemann.plannr.ui.theme.PlannrTheme
import de.chennemann.plannr.ui.theme.colors
import org.koin.compose.KoinApplication

@Composable
fun PlannrApp(
    platformContext: Any? = null,
    darkTheme: Boolean = true,
    decorationColors: @Composable (Boolean, Color, Boolean, Color, Boolean) -> Unit = { darkTheme, statusBarColor, darkStatusBarIcons, navigationBarColor, darkNavigationBarIcons ->
        DecorationColors(
            darkTheme = darkTheme,
            statusBarColor = statusBarColor,
            darkStatusBarIcons = darkStatusBarIcons,
            navigationBarColor = navigationBarColor,
            darkNavigationBarIcons = darkNavigationBarIcons
        )
    },
) {
    KoinApplication(application = dependencyGraph(platformContext)) {
        PlannrTheme(darkTheme, decorationColors) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
                RootScreen()
            }
        }
    }
}
