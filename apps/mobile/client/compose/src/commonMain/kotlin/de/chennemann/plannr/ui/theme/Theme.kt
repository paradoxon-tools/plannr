package de.chennemann.plannr.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.TextStyle

private val DarkColorScheme = PlannrColorScheme(
    materialColors = darkColorScheme(
        background = PitchBlack,
        onBackground = SoftWhite,

        surface = CarbonBlack,
        onSurface = SoftWhite,
        surfaceVariant = Graphite,
        onSurfaceVariant = MutedLilac,
        surfaceTint = PitchBlack,

        primary = PersianGreen,
        onPrimary = PitchBlack,
        primaryContainer = MetallicSeaweed,
        onPrimaryContainer = SoftWhite,

        secondary = SoftWhite,
        onSecondary = PitchBlack,
        secondaryContainer = Graphite,
        onSecondaryContainer = SoftWhite,

        tertiary = MutedTeal,
        onTertiary = PitchBlack
    ),
    onBackgroundMuted = MutedLilac,
    onSurfaceMuted = MutedLilac,
    onPrimaryMuted = CarbonBlack.copy(alpha = 0.6f),
    onSecondaryMuted = CarbonBlack.copy(alpha = 0.6f),
    onTertiaryMuted = CarbonBlack.copy(alpha = 0.6f),
    primaryDarker = MetallicSeaweed,
    primaryDark = BlueSapphire,
    primaryLighter = MountainMedow,
    primaryLight = SeaGreenCrayola,
    statusbarColor = PitchBlack
)

private val LightColorScheme = PlannrColorScheme(
    materialColors = lightColorScheme(
        background = Color(0xFFE5E0E8),
        onBackground = RaisinBlack,

        surface = Color(0xFFF7F2FA),
        onSurface = Color(0xFF605A62),

        primary = BlueSapphire,
        onPrimary = White
    ),
    onBackgroundMuted = Color(0xFF444646),
    onSurfaceMuted = Color(0xFFA09AA2),
    onPrimaryMuted = LightMuted,
    onSecondaryMuted = LightMuted,
    onTertiaryMuted = LightMuted,
    primaryDarker = MetallicSeaweed,
    primaryDark = BlueSapphire,
    primaryLighter = MountainMedow,
    primaryLight = SeaGreenCrayola,
    statusbarColor = RaisinBlack
)

private val DarkThemeConfig = PlannrThemeConfig(
    isDark = true,
    darkStatusBarIcons = false
)

private val LightThemeConfig = PlannrThemeConfig()

val colorGroups = ColorGroups


private val LocalColors = staticCompositionLocalOf { DarkColorScheme }
private val LocalColorGroups = staticCompositionLocalOf { colorGroups }
private val LocalConfig = staticCompositionLocalOf { DarkThemeConfig }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlannrTheme(
    darkTheme: Boolean,
    decorationColors: @Composable (Boolean, Color, Boolean, Color, Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalConfig provides evaluateThemeConfig(darkTheme),
        LocalColors provides evaluateColorScheme(darkTheme),
        LocalColorGroups provides colorGroups
    ) {

        MaterialExpressiveTheme(
            colorScheme = MaterialTheme.colors.materialColors,
            motionScheme = MotionScheme.expressive(),
        ) {

            decorationColors(
                darkTheme,
                MaterialTheme.colors.statusbarColor,
                MaterialTheme.config.darkStatusBarIcons,
                MaterialTheme.colors.background,
                MaterialTheme.config.darkStatusBarIcons
            )

            ProvideTextStyle(
                value = TextStyle(color = MaterialTheme.colors.onBackground)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun evaluateThemeConfig(darkTheme: Boolean) = if (darkTheme) {
    DarkThemeConfig
} else {
    LightThemeConfig
}

@Composable
private fun evaluateColorScheme(darkTheme: Boolean) = if (darkTheme) {
    DarkColorScheme
} else {
    LightColorScheme
}

val MaterialTheme.colors: PlannrColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalColors.current

val MaterialTheme.colorGroups: ColorGroups
    @Composable
    @ReadOnlyComposable
    get() = LocalColorGroups.current


val MaterialTheme.config: PlannrThemeConfig
    @Composable
    @ReadOnlyComposable
    get() = LocalConfig.current
