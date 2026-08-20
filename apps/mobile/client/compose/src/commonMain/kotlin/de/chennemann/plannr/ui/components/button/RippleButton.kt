package de.chennemann.plannr.ui.components.button

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RippleButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {

    val rippleConfiguration = rememberIconButtonRippleConfiguration()

    CompositionLocalProvider(LocalRippleConfiguration provides rippleConfiguration) {
        Button(
            modifier = modifier,
            enabled = enabled,
            onClick = onClick
        )  {
            content()
        }
    }
}


@ExperimentalMaterial3Api
@Composable
private fun rememberIconButtonRippleConfiguration(rippleColor: Color = MaterialTheme.colorScheme.onBackground) =
    remember {
        RippleConfiguration(
            color = rippleColor,
            rippleAlpha = RippleAlpha(
                pressedAlpha = 0.20f,
                focusedAlpha = 0.22f,
                draggedAlpha = 0.18f,
                hoveredAlpha = 0.14f
            )
        )
    }