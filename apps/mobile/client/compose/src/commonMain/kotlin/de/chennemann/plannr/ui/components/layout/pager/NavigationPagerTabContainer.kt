package de.chennemann.plannr.ui.components.layout.pager

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.chennemann.plannr.ui.components.button.RippleIconButton
import de.chennemann.plannr.ui.theme.colors



data class NavigationItem(
    val title: String,
    val icon: Painter
)

object NavigationTabDefaults {
    @Composable
    fun navigationItemColors(
        tabColor: Color = MaterialTheme.colors.onBackgroundMuted,
        tabIconColor: Color = MaterialTheme.colors.onBackgroundMuted,
        selectedTabColor: Color = MaterialTheme.colors.onBackground,
        selectedTabIconColor: Color = MaterialTheme.colors.onBackground,
    ): NavigationColors = NavigationColors(
        tabColor = tabColor,
        tabIconColor = tabIconColor,
        selectedTabColor = selectedTabColor,
        selectedTabIconColor = selectedTabIconColor,
    )
}

@Immutable
data class NavigationColors(
    val tabColor: Color,
    val tabIconColor: Color,
    val selectedTabColor: Color,
    val selectedTabIconColor: Color,
)

@Composable
fun NavigationPagerTabContainer(
    state: NavigationPagerState,
    modifier: Modifier = Modifier,
    colors: NavigationColors = NavigationTabDefaults.navigationItemColors(),
    highlightingEnabled: Boolean = true,
    onPageActivationRequest: (Int) -> Unit = {
        state.animateToPage(it)
    },
    itemFactory: @Composable (Int) -> NavigationItem,
) {
    BoxWithConstraints(
        modifier = Modifier
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { _ ->
                }
            )
    ) {
        Navigation(
            state = state,
            modifier = modifier,
            colors = colors,
            itemFactory = itemFactory,
            containerWidth = maxWidth,
            highlightingEnabled = highlightingEnabled,
            onPageActivationRequest = onPageActivationRequest
        )
    }
}


@Composable
private fun Navigation(
    state: NavigationPagerState,
    modifier: Modifier,
    colors: NavigationColors,
    itemFactory: @Composable (Int) -> NavigationItem,
    containerWidth: Dp,
    highlightingEnabled: Boolean,
    onPageActivationRequest: (Int) -> Unit
) {

    val iconHeight = 48
    val navBarPadding = 12

    val contentWidth = (containerWidth - (2 * navBarPadding).dp)

    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
            .height((2 * navBarPadding + iconHeight).dp)
            .fillMaxWidth()
            .padding(navBarPadding.dp)
    ) {

        val reservedIconSpace = (state.pageCount * iconHeight).dp
        val maxLabelWidth = contentWidth - reservedIconSpace

        val scrollState by state.scrollState.collectAsState()

        val sourcePage = remember(scrollState) {
            @Suppress("NAME_SHADOWING")
            when (val scrollState = scrollState) {
                is ScrollState.TransitionState -> scrollState.sourcePage
                is ScrollState.Static -> scrollState.activePage
            }
        }

        val targetPage = remember(scrollState) {
            @Suppress("NAME_SHADOWING")
            when (val scrollState = scrollState) {
                is ScrollState.TransitionState -> scrollState.targetPage
                is ScrollState.Static -> scrollState.activePage
            }
        }

        val scrollInProgress = when (scrollState) {
            is ScrollState.TransitionState -> true
            is ScrollState.Static -> false
        }

        val progress = remember(scrollState) {
            @Suppress("NAME_SHADOWING")
            when (val scrollState = scrollState) {
                is ScrollState.TransitionState -> when {
                    targetPage != sourcePage -> scrollState.progress
                    else -> 1f
                }

                else -> 1f
            }
        }

        repeat(state.pageCount) { pageIndex ->

            val navigationItem = itemFactory(pageIndex)

            val isTargetTab = targetPage == pageIndex
            val isSourceTab = sourcePage == pageIndex

            val tabLabelWidth: Dp = when (scrollInProgress) {
                true -> when {
                    isTargetTab -> maxLabelWidth.times(progress)
                    isSourceTab -> maxLabelWidth - maxLabelWidth.times(progress)
                    else -> 0.dp
                }

                else -> when {
                    isTargetTab -> maxLabelWidth
                    else -> 0.dp
                }
            }

            val highlightAnimationProgress = when {
                isTargetTab -> progress
                isSourceTab -> 1f - progress
                else -> Float.NaN
            }

            NavigationTab(
                label = navigationItem.title,
                icon = navigationItem.icon,
                highlighted = isTargetTab && highlightingEnabled,
                colors = colors,
                animationProgress = highlightAnimationProgress,
                labelWidth = tabLabelWidth,
                selectionEnabled = !scrollInProgress && !isTargetTab,
                modifier = Modifier,
                onTabSelected = {
                    onPageActivationRequest(pageIndex)
                }
            )
        }
    }
}

@Composable
private fun NavigationTab(
    label: String,
    icon: Painter,
    highlighted: Boolean,
    colors: NavigationColors,
    animationProgress: Float,
    labelWidth: Dp,
    selectionEnabled: Boolean,
    modifier: Modifier = Modifier,
    onTabSelected: () -> Unit,
) {

    val defaultColor = colors.tabColor
    val highlightedColor = colors.selectedTabColor

    val textColor = when {
        animationProgress.isNaN() -> if (highlighted) highlightedColor else defaultColor
        else -> lerp(defaultColor, highlightedColor, animationProgress)
    }

    val defaultIconColor = colors.tabIconColor
    val highlightedIconColor = colors.selectedTabIconColor

    val iconColor = when {
        animationProgress.isNaN() -> if (highlighted) highlightedIconColor else defaultIconColor
        else -> lerp(defaultIconColor, highlightedIconColor, animationProgress)
    }


    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        RippleIconButton(
            icon = icon,
            iconColor = iconColor,
            enabled = selectionEnabled,
            contentDescription = label.lowercase().replaceFirstChar { it.uppercase() },
            onClick = onTabSelected
        )

        NavigationTabLabel(
            label = label,
            textColor = textColor,
            modifier = Modifier.width(labelWidth)
        )
    }
}


@Composable
private fun NavigationTabLabel(label: String, textColor: Color, modifier: Modifier) {
    Text(
        text = label,
        color = textColor,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        maxLines = 1,
        letterSpacing = 3.5.sp,
        modifier = modifier
    )
}