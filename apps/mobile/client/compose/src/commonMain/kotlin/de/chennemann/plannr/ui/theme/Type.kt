package de.chennemann.plannr.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import de.chennemann.plannr.resources.Res
import dev.icerock.moko.resources.FontResource
import dev.icerock.moko.resources.compose.asFont

@Composable
private fun generateFont(
    fontResource: FontResource,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal
): Font {
    return fontResource.asFont(
        weight,
        style
    ) ?: throw RuntimeException("Could not create Font from resource $fontResource")
}

val roboto_condensed: FontFamily
    @Composable
    get() = FontFamily(
        generateFont(Res.fonts.roboto_condensed_regular),
        generateFont(Res.fonts.roboto_condensed_light, weight = FontWeight.Light),
        generateFont(Res.fonts.roboto_condensed_bold, weight = FontWeight.Bold)
    )

val eczar: FontFamily
    @Composable
    get() = FontFamily(
        generateFont(Res.fonts.eczar_regular),
        generateFont(Res.fonts.eczar_bold, weight = FontWeight.Bold)
    )

val kimberley: FontFamily
    @Composable
    get() = FontFamily(
        generateFont(Res.fonts.kimberley_regular)
    )

fun textStyle(
    family: FontFamily,
    weight: FontWeight,
    size: TextUnit,
    letterSpacing: TextUnit = TextUnit.Unspecified,
): TextStyle = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size,
    letterSpacing = letterSpacing
)

// Set of Material typography styles to start with
val Typography: Typography
    @Composable
    get() = Typography(
        displayLarge = textStyle(roboto_condensed, FontWeight.Light, 72.sp),          // H1
        displayMedium = textStyle(roboto_condensed, FontWeight.Light, 56.sp),         // H2
        displaySmall = textStyle(eczar, FontWeight.Normal, 48.sp),          // H3
        headlineLarge = textStyle(roboto_condensed, FontWeight.Normal, 34.sp),        // H4
        headlineMedium = textStyle(roboto_condensed, FontWeight.Normal, 24.sp),       // H5
        headlineSmall = textStyle(roboto_condensed, FontWeight.Medium, 20.sp),        // H6
        titleLarge = textStyle(eczar, FontWeight.Normal, 20.sp, 4.sp),
        titleMedium = textStyle(roboto_condensed, FontWeight.Normal, 18.sp),          // Subtitle 1
        titleSmall = textStyle(roboto_condensed, FontWeight.Medium, 16.sp),           // Subtitle 2
        bodyLarge = textStyle(roboto_condensed, FontWeight.Normal, 16.sp),
        bodyMedium = textStyle(eczar, FontWeight.Normal, 16.sp),            // Body 1
        bodySmall = textStyle(roboto_condensed, FontWeight.Normal, 14.sp, 2.sp),            // Body 2
        labelLarge = textStyle(roboto_condensed, FontWeight.Bold, 14.sp),             // Button
        labelMedium = textStyle(roboto_condensed, FontWeight.Normal, 10.sp)           // Overline
    )
