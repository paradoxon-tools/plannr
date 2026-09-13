package de.chennemann.plannr.ui.components.layout.pager

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import de.chennemann.plannr.extensions.derive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import de.chennemann.plannr.ui.components.layout.pager.NavigationPagerAnimationDirection as ScrollDirection


/**
 * Creates and remember a [NavigationPagerState] to be used with a [NavigationPager]
 *
 * @param initialPage The pager that should be shown first.
 * @param pageCount The number of pages this Pager will have.
 */
@Composable
fun rememberNavigationPagerState(
    initialPage: Int = 0,
    pageCount: () -> Int
): NavigationPagerState {

    val internalPagerState = rememberPagerState(initialPage = initialPage, pageCount = pageCount)
    val coroutineScope = rememberCoroutineScope()

    return remember {
        NavigationPagerState(
            internalPagerState,
            coroutineScope
        )
    }
}

object NavigationPagerAnimationDirection {
    const val LEFT = -1
    const val NONE = 0
    const val RIGHT = 1
}

sealed interface ScrollState {
    data class Static(
        val activePage: Int,
    ) : ScrollState

    sealed interface TransitionState : ScrollState {
        val sourcePage: Int
        val targetPage: Int
        val progress: Float
        val direction: Int
    }

    data class Manual(
        override val sourcePage: Int,
        override val targetPage: Int,
        override val progress: Float,
        override val direction: Int,
    ) : TransitionState

    data class Programmatic(
        override val sourcePage: Int,
        override val targetPage: Int,
        override val progress: Float,
        override val direction: Int,
    ) : TransitionState
}

class NavigationPagerState internal constructor(
    val pagerState: PagerState,
    private val coroutineScope: CoroutineScope
): ScrollableState by pagerState {

    val pageCount by pagerState::pageCount

    val currentPage by pagerState::currentPage
    val targetPage by pagerState::targetPage
    val settledPage by pagerState::settledPage

    val currentPageOffsetFraction by pagerState::currentPageOffsetFraction

    val layoutInfo by pagerState::layoutInfo
    val interactionSource by pagerState::interactionSource

    private val currentPageFlow = snapshotFlow { currentPage }
    private val settledPageFlow = snapshotFlow { settledPage }
    private val pageOffsetFractionFlow = snapshotFlow { currentPageOffsetFraction }
    private val isScrollInProgressFlow = snapshotFlow { isScrollInProgress }
    private val scrolling = snapshotFlow { isScrollInProgress }

    private val staticScrollState = derive(currentPageFlow) {
        ScrollState.Static(it)
    }.asStateFlow(ScrollState.Static(pagerState.currentPage))

    private var _scrollSourcePage: Int? = null
    private val manualScrollState = combine(
        scrolling,
        currentPageFlow,
        settledPageFlow,
        pageOffsetFractionFlow
    ) { scrolling, currentPage, settledPage, offset ->

        val scrollSourcePage = when {
            scrolling -> {
                if (_scrollSourcePage == null) {
                    _scrollSourcePage = settledPage
                }
                _scrollSourcePage!!
            }

            else -> {
                _scrollSourcePage = null
                return@combine staticScrollState.value
            }
        }

        val direction = when {
            currentPage < scrollSourcePage -> ScrollDirection.LEFT
            currentPage > scrollSourcePage -> ScrollDirection.RIGHT
            else -> {
                when {
                    offset > 0f && settledPage < pageCount -> ScrollDirection.RIGHT
                    offset < 0f && settledPage > 0 -> ScrollDirection.LEFT
                    else -> ScrollDirection.NONE
                }
            }
        }

        val absoluteOffset = (currentPage - scrollSourcePage + offset) * direction
        val pageOffset = absoluteOffset.toInt()
        val partialOffset = absoluteOffset - pageOffset
        val sourcePage = (scrollSourcePage + pageOffset * direction).coerceIn(0, pageCount - 1)
        val targetPage =
            (scrollSourcePage + (pageOffset + 1) * direction).coerceIn(0, pageCount - 1)

        ScrollState.Manual(
            sourcePage = sourcePage,
            targetPage = targetPage,
            progress = partialOffset,
            direction = direction
        )
    }.asStateFlow(staticScrollState.value)

    private val programmaticScrollState = MutableStateFlow<ScrollState>(staticScrollState.value)
    private val programmaticTransitionTargetPage = MutableStateFlow(null as Int?)
    private var animationJob: Job? = null
    private val animating = combine(
        isScrollInProgressFlow,
        settledPageFlow,
        programmaticTransitionTargetPage
    ) { scrolling, sourcePage, targetPage ->

        val animating = targetPage != null && sourcePage != targetPage && !scrolling

        if (targetPage != null && animating) {
            animationJob = coroutineScope.launch {
                animate(0f, 1f, animationSpec = tween(durationMillis = 300)) { value, _ ->
                    programmaticScrollState.update {
                        ScrollState.Programmatic(
                            sourcePage = sourcePage,
                            targetPage = targetPage,
                            progress = value,
                            direction = when {
                                targetPage < sourcePage -> ScrollDirection.LEFT
                                targetPage > sourcePage -> ScrollDirection.RIGHT
                                else -> ScrollDirection.NONE
                            }
                        )
                    }

                    if (value == 1f) {
                        // Reset animation onFinish
                        pagerState.requestScrollToPage(targetPage)
                        programmaticTransitionTargetPage.update { null }
                        programmaticScrollState.update { staticScrollState.value }
                    }
                }
            }
        } else {
            animationJob?.cancel()
            animationJob = null
            programmaticTransitionTargetPage.update { null }
            programmaticScrollState.update { staticScrollState.value }
        }

        animating

    }.asStateFlow(false)

    val scrollState = combine(
        scrolling,
        animating,
        staticScrollState,
        manualScrollState,
        programmaticScrollState,
    ) { scrolling, animating, staticScrollState, manualScrollState, programmaticScrollState ->
        when {
            scrolling -> manualScrollState
            animating -> programmaticScrollState
            else -> staticScrollState
        }
    }.asStateFlow(staticScrollState.value)

    fun animateToPage(page: Int) {
        programmaticTransitionTargetPage.update { page }
    }

    private fun <T> Flow<T>.asStateFlow(
        initialValue: T,
        started: SharingStarted = SharingStarted.WhileSubscribed()
    ): StateFlow<T> = stateIn(
        scope = coroutineScope,
        started = started,
        initialValue = initialValue
    )
}