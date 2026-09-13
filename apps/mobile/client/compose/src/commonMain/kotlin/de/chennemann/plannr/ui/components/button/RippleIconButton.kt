package de.chennemann.plannr.ui.components.button

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter



@Composable
fun RippleIconButton(
    icon: Painter,
    iconColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    onClick: () -> Unit,
) {
    RippleIconButton(
        icon = {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = iconColor
            )
        },
        modifier = modifier,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RippleIconButton(
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {

    val rippleConfiguration = rememberIconButtonRippleConfiguration()

    CompositionLocalProvider(LocalRippleConfiguration provides rippleConfiguration) {
        IconButton(
            modifier = modifier,
            enabled = enabled,
            onClick = onClick
        ) {
            icon()
        }
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
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