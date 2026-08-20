package de.chennemann.plannr.ui.components.layout.twopane

import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp


@Composable
fun TwoPaneLayout(
    floatingActionButton: @Composable () -> Unit = {},
    pagerState: TwoPanePagerState = rememberTwoPanePagerState(),
    mainPageContent: @Composable () -> Unit,
    secondaryPageContent: @Composable () -> Unit
) {

    BoxWithConstraints(Modifier.fillMaxSize()) {

        val containerHeight = with(LocalDensity.current) { maxHeight.toPx() }
        pagerState.initialize(containerHeight, 0.4f)

        val mainPageOffset by pagerState.mainPageOffset.collectAsState()
        val secondaryPageOffset by pagerState.secondaryPageOffset.collectAsState()
        val thresholdReached by pagerState.thresholdReached.collectAsState()

        val haptic = LocalHapticFeedback.current

        LaunchedEffect(thresholdReached) {
            if (thresholdReached) haptic.performHapticFeedback(HapticFeedbackType.GestureEnd)
        }

        Box(
            Modifier
                .fillMaxSize()
                .clipScrollableContainer(Orientation.Vertical)
                .nestedScroll(pagerState.nestedScrollConnection)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, mainPageOffset) },
                content = {
                    mainPageContent()
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, secondaryPageOffset) },
                content = {
                    secondaryPageContent()
                }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .padding(bottom = 16.dp),
                content = {
                    floatingActionButton()
                }
            )
        }
    }
}
