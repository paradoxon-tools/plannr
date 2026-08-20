package de.chennemann.plannr.ui.components.layout.pager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

@Composable
fun NavigationPagerScaffold(
    state: NavigationPagerState,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    content: @Composable (Int) -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = topBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        content = { innerPadding ->
            NavigationPager(
                state = state,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        bottom = innerPadding.calculateBottomPadding(),
                        top = innerPadding.calculateTopPadding()
                    ),
                pageContent = content
            )
        },
        bottomBar = bottomBar,
    )
}


@Composable
private fun NavigationPager(
    state: NavigationPagerState,
    modifier: Modifier = Modifier,
    pageContent: @Composable (Int) -> Unit
) {

    val scrollState by state.scrollState.collectAsState()

    BoxWithConstraints(modifier = modifier) {
        when (@Suppress("NAME_SHADOWING") val scrollState = scrollState) {
            is ScrollState.Programmatic -> {

                val sourcePage = scrollState.sourcePage
                val targetPage = scrollState.targetPage
                val animationProgress = scrollState.progress
                val animationDirection = scrollState.direction

                //Source Page
                val sourcePageOffset: Dp =
                    maxWidth.times(animationProgress).times((-1) * animationDirection)

                Box(modifier = Modifier.offset(x = sourcePageOffset)) {
                    pageContent(sourcePage)
                }

                //Target Page
                val targetPageOffset: Dp =
                    maxWidth.times((1 - animationProgress)).times(animationDirection)

                Box(modifier = Modifier.offset(x = targetPageOffset)) {
                    pageContent(targetPage)
                }
            }

            else -> {
                HorizontalPager(
                    state = state.pagerState,
                    pageContent = { page -> pageContent(page) }
                )
            }
        }
    }
}