package de.chennemann.plannr.ui.components.layout.twopane

import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import de.chennemann.plannr.extensions.derive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.properties.Delegates


/**
 * Remembers and provides an instance of [TwoPanePagerState], which manages the state of a two-pane
 * pager with integrated scrollable states and an offset transformation for transition effects.
 *
 * @param initialPane The initial page index (0-based) to display. Defaults to 0.
 * @param mainScrollableState The [ScrollableState] associated with the main pane. Defaults to a remembered [ScrollState].
 * @param secondaryScrollableState The [ScrollableState] associated with the secondary pane. Defaults to a remembered [ScrollState].
 * @param offsetTransformation A lambda function that transforms the scroll offset into a desired value,
 *                             controlling the visual effect during page transitions. Defaults to a function
 *                             with diminishing effect based on offset value.
 * @return A remembered instance of [TwoPanePagerState] managing the pager's behavior and transitions.
 */
@Composable
fun rememberTwoPanePagerState(
    initialPane: Pane = Pane.Main,
    mainScrollableState: ScrollableState = rememberScrollState(),
    secondaryScrollableState: ScrollableState = rememberScrollState(),
    offsetTransformation: (Float) -> Float = { offset ->
        val strength = 0.005f // Controls how quickly the effect diminishes
        when {
            offset != 0f -> (offset / (1f + strength * offset.absoluteValue))
            else -> 0f
        }
    },
): TwoPanePagerState {

    val coroutineScope = rememberCoroutineScope()

    return remember {
        TwoPanePagerState(
            initialPane,
            mainScrollableState,
            secondaryScrollableState,
            offsetTransformation,
            coroutineScope
        )
    }
}

enum class Pane(val pageNumber: Int) {
    Main(0),
    Secondary(1);

    companion object {
        fun fromNumber(number: Int): Pane = when (number) {
            0 -> Main
            1 -> Secondary
            else -> throw IllegalArgumentException("Pane number must be 0 or 1")
        }
    }
}

fun delayedProgress(p: Float, delay: Float = 0.3f): Float =
    if (p < delay) 0f else ((p - delay) / (1f - delay)).coerceIn(0f, 1f)



class TwoPanePagerState internal constructor(
    initialPane: Pane,
    val mainScrollableState: ScrollableState,
    val secondaryScrollableState: ScrollableState,
    private val offsetTransformation: (Float) -> Float,
    private val coroutineScope: CoroutineScope
) {

    private var containerHeight by Delegates.notNull<Float>()
    private var pageTransitionOffsetThreshold by Delegates.notNull<Float>()
    private var transformedOffsetThreshold by Delegates.notNull<Float>()
    private var initialized = false

    private val _currentPage = MutableStateFlow(initialPane.pageNumber)
    private val currentPage: StateFlow<Int> = _currentPage

    val currentPane: StateFlow<Pane> = derive(currentPage) {
        Pane.fromNumber(it)
    }.asStateFlow(Pane.fromNumber(initialPane.pageNumber))

    private val targetPage = MutableStateFlow(currentPage.value)

    private val _isManualScroll = MutableStateFlow(false)
    private val isManualScroll: StateFlow<Boolean> = _isManualScroll

    private val _scrollOffset = MutableStateFlow(0f)
    private val scrollOffset: StateFlow<Float> = _scrollOffset

    private val manualScrollOffset: StateFlow<Float> = derive(
        scrollOffset
    ) { scrollOffset ->
        offsetTransformation(scrollOffset)
    }.asStateFlow(0f)

    private val _isPageTransitionInProgress = MutableStateFlow(false)
    val isPageTransitionInProgress: StateFlow<Boolean> = _isPageTransitionInProgress


    private var pageTransitionJob: Job? = null
    private val _pageOffset = MutableStateFlow(0f)
    val pageOffset: StateFlow<Float> = _pageOffset

    val mainPageOffset = derive(pageOffset) { it.roundToInt() }.asStateFlow(0)
    val secondaryPageOffset = derive(pageOffset) { (containerHeight + it).roundToInt() }.asStateFlow(0)

    private val transitionProgress: StateFlow<Float> = derive(pageOffset) { it.absoluteValue / containerHeight }.asStateFlow(0f)
    val mainPageTransitionProgress: StateFlow<Float> = derive(transitionProgress) { 1f - it }.asStateFlow(1f)
    val secondaryPageTransitionProgress: StateFlow<Float> = transitionProgress

    private val transitionIndicatorProgress: StateFlow<Float> = combine(currentPage, manualScrollOffset) { currentPage, manualScrollOffset ->
        when (currentPage) {
            0 -> (manualScrollOffset.absoluteValue / transformedOffsetThreshold).coerceIn(0f, 1f)
            else -> 1f - (manualScrollOffset.absoluteValue / transformedOffsetThreshold).coerceIn(0f, 1f)
        }
    }.asStateFlow(0f)
    val mainPageTransitionIndicatorProgress: StateFlow<Float> = derive(transitionIndicatorProgress) { 1f - it }.asStateFlow(1f)
    val secondaryPageTransitionIndicatorProgress: StateFlow<Float> = transitionIndicatorProgress

    val thresholdReached: StateFlow<Boolean> = combine(currentPage, transitionIndicatorProgress) { currentPage, transitionIndicatorProgress ->
        when (currentPage) {
            0 -> transitionIndicatorProgress == 1.0f
            else -> transitionIndicatorProgress == 0.0f
        }
    }.asStateFlow(false)

    fun animateToPane(pane: Pane) {
        if (isPageTransitionInProgress.value) return
        if (pane.pageNumber == currentPage.value) return

        targetPage.value = pane.pageNumber
        _isPageTransitionInProgress.update { true }
    }

    fun initialize(containerHeight: Float, requiredOffsetPercentage: Float) {
        this.containerHeight = containerHeight
        this.pageTransitionOffsetThreshold = containerHeight * requiredOffsetPercentage
        this.transformedOffsetThreshold = offsetTransformation(pageTransitionOffsetThreshold)

        // Layout recomposes while lists expand and animations run. Register collectors only once.
        if (initialized) return
        initialized = true

        coroutineScope.launch {
            combine(
                currentPage,
                manualScrollOffset,
                isPageTransitionInProgress
            ) { currentPage, manualScrollOffset, isPageTransitionInProgress ->
                if (isPageTransitionInProgress) {
                    pageTransitionJob = coroutineScope.launch {

                        val initialOffset = when (currentPage) {
                            0 -> manualScrollOffset
                            else -> -this@TwoPanePagerState.containerHeight + manualScrollOffset
                        }

                        println("scrollOffset: ${scrollOffset.value.absoluteValue}, threshold: $pageTransitionOffsetThreshold")
                        val targetPage = when {
                            targetPage.value != currentPage -> {
                                println("Animate to targetPage")
                                targetPage.value
                            }
                            scrollOffset.value.absoluteValue > pageTransitionOffsetThreshold -> 1 - currentPage
                            else -> currentPage
                        }

                        val targetOffset = when (targetPage) {
                            0 -> 0f
                            else -> -this@TwoPanePagerState.containerHeight
                        }

                        animate(0f, 1f) { value, _ ->

                            _pageOffset.update { initialOffset * (1f - value) + targetOffset * value }

                            if (value == 1f) {
                                _isPageTransitionInProgress.update { false }
                                _currentPage.update { targetPage }
                                this@TwoPanePagerState.targetPage.update { targetPage }
                                _scrollOffset.update { 0f }
                            }
                        }
                    }
                } else {
                    pageTransitionJob?.cancel()
                    pageTransitionJob = null
                }
            }.collect()
        }

        coroutineScope.launch {
            combine(
                currentPage,
                pageOffset,
                isManualScroll,
                manualScrollOffset
            ) { currentPage, pageOffset, isManualScroll, manualScrollOffset ->
                if (isManualScroll) {
                    _pageOffset.update {
                        when (currentPage) {
                            0 -> manualScrollOffset
                            else -> -this@TwoPanePagerState.containerHeight + manualScrollOffset
                        }
                    }
                }

            }.collect()
        }
    }



    val nestedScrollConnection = object: NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {

            _isManualScroll.update {
                when (source) {
                    NestedScrollSource.Companion.UserInput -> true
                    else -> false
                }
            }

            val delta = available.y
            val currentScrollOffset = scrollOffset.value

            if (currentPage.value == 0 && !(mainScrollableState.canScrollForward)) {
                val newOffset = (currentScrollOffset + delta).coerceIn(-containerHeight, 0f)
                val consumed = newOffset - currentScrollOffset

                _scrollOffset.update { newOffset }
                return Offset(x = 0f, y = consumed)
            }

            if (currentPage.value == 1 && !(secondaryScrollableState.canScrollBackward)) {
                val newOffset = (currentScrollOffset + delta).coerceIn(0f, containerHeight)
                val consumed = newOffset - currentScrollOffset

                _scrollOffset.update { newOffset }
                return Offset(x = 0f, y = consumed)
            }

            return super.onPreScroll(available, source)
        }

        override suspend fun onPreFling(available: Velocity): Velocity {

            if (isManualScroll.value) {
                _isManualScroll.update { false }
            }

            if (scrollOffset.value != 0f) {
                _isPageTransitionInProgress.update { true }
                return available
            }

            return super.onPreFling(available)
        }
    }



    private fun <T> Flow<T>.asStateFlow(
        initialValue: T,
        started: SharingStarted = SharingStarted.Companion.WhileSubscribed()
    ): StateFlow<T> = stateIn(
        scope = coroutineScope,
        started = started,
        initialValue = initialValue
    )
}
