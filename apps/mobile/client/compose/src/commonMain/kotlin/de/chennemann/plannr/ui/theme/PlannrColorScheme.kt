package de.chennemann.plannr.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

data class PlannrColorScheme(
    val materialColors: ColorScheme,
    val onBackgroundMuted: Color = materialColors.onBackground,
    val onSurfaceMuted: Color = materialColors.onSurface,
    val onPrimaryMuted: Color = materialColors.onPrimary,
    val onSecondaryMuted: Color = materialColors.onSecondary,
    val onTertiaryMuted: Color = materialColors.onTertiary,
    val primaryDarker: Color = materialColors.primary,
    val primaryDark: Color = materialColors.primary,
    val primaryLighter: Color = materialColors.primary,
    val primaryLight: Color = materialColors.primary,
    val secondaryDarker: Color = materialColors.secondary,
    val secondaryDark: Color = materialColors.secondary,
    val secondaryLighter: Color = materialColors.secondary,
    val secondaryLight: Color = materialColors.secondary,
    val tertiaryDarker: Color = materialColors.tertiary,
    val tertiaryDark: Color = materialColors.tertiary,
    val tertiaryLighter: Color = materialColors.tertiary,
    val tertiaryLight: Color = materialColors.tertiary,
    val statusbarColor: Color = materialColors.background
) {
    val primary: Color get() = materialColors.primary
    val onPrimary: Color get() = materialColors.onPrimary
    val primaryContainer: Color get() = materialColors.primaryContainer
    val onPrimaryContainer: Color get() = materialColors.onPrimaryContainer
    val inversePrimary: Color get() = materialColors.inversePrimary

    val secondary: Color get() = materialColors.secondary
    val onSecondary: Color get() = materialColors.onSecondary
    val secondaryContainer: Color get() = materialColors.secondaryContainer
    val onSecondaryContainer: Color get() = materialColors.onSecondaryContainer

    val tertiary: Color get() = materialColors.tertiary
    val onTertiary: Color get() = materialColors.onTertiary
    val tertiaryContainer: Color get() = materialColors.tertiaryContainer
    val onTertiaryContainer: Color get() = materialColors.onTertiaryContainer

    val background: Color get() = materialColors.background
    val onBackground: Color get() = materialColors.onBackground

    val surface: Color get() = materialColors.surface
    val onSurface: Color get() = materialColors.onSurface
    val surfaceVariant: Color get() = materialColors.surfaceVariant
    val onSurfaceVariant: Color get() = materialColors.onSurfaceVariant
    val surfaceTint: Color get() = materialColors.surfaceTint
    val inverseSurface: Color get() = materialColors.inverseSurface
    val inverseOnSurface: Color get() = materialColors.inverseOnSurface

    val error: Color get() = materialColors.error
    val onError: Color get() = materialColors.onError
    val errorContainer: Color get() = materialColors.errorContainer
    val onErrorContainer: Color get() = materialColors.onErrorContainer

    val outline: Color get() = materialColors.outline
}
