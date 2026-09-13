package de.chennemann.plannr.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


@Composable
expect fun DecorationColors(darkTheme: Boolean, statusBarColor: Color, darkStatusBarIcons: Boolean, navigationBarColor: Color, darkNavigationBarIcons: Boolean)