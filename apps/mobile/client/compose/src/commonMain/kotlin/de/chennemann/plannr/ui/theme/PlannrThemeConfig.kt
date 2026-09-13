package de.chennemann.plannr.ui.theme

data class PlannrThemeConfig(
    val isDark: Boolean = true,
    val darkStatusBarIcons: Boolean = !isDark
)