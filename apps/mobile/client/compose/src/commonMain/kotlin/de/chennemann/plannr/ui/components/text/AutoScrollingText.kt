package de.chennemann.plannr.ui.components.text

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun AutoScrollingText(
    text: String,
    modifier: Modifier = Modifier,
    scrollSpeed: Int = 50, // pixels per second
    delayBeforeScroll: Long = 1000 // milliseconds
) {
    val scrollState = rememberScrollState()
    var textWidth by remember { mutableIntStateOf(0) }
    var containerWidth by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    val infiniteTransition = rememberInfiniteTransition(label = "autoScrollTransition")

    // Calculate the duration for one full scroll
    val scrollDuration = remember(textWidth, containerWidth, scrollSpeed) {
        if (textWidth > containerWidth && scrollSpeed > 0) {
            val distanceToScroll = (textWidth - containerWidth).toFloat()
            (distanceToScroll / (scrollSpeed / 1000f)).toLong() // Convert speed to pixels/millisecond
        } else {
            0L
        }
    }

    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (textWidth > containerWidth) (textWidth - containerWidth).toFloat() else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = scrollDuration.toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scrollOffsetAnimation"
    )

    LaunchedEffect(textWidth, containerWidth, scrollDuration) {
        if (scrollDuration > 0) {
            delay(delayBeforeScroll)
            // Animate scroll position if needed.
            // The animatedOffset already drives the scroll.
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                containerWidth = coordinates.size.width
            }
            .horizontalScroll(scrollState, enabled = false) // Disable user scroll
    ) {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Clip, // Important to clip for smooth scrolling
            modifier = Modifier.onGloballyPositioned { coordinates ->
                textWidth = coordinates.size.width
            }
        )
    }

    // Manually set scroll position using the animated offset
    LaunchedEffect(animatedOffset) {
        scrollState.scrollTo(animatedOffset.toInt())
    }
}


@Composable
fun AutoScrollingTextInfinite(
    text: String,
    modifier: Modifier = Modifier,
    scrollSpeedPxPerSec: Int = 50, // pixels per second
    delayBeforeScrollMs: Long = 1000 // milliseconds
) {
    val scrollState = rememberScrollState()
    var textWidth by remember { mutableIntStateOf(0) }
    var containerWidth by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    val infiniteTransition = rememberInfiniteTransition(label = "autoScrollInfiniteTransition")

    // The target scroll value is the width of the text content itself.
    // When we scroll by this amount, the second text instance will be in view,
    // making it appear as if it's continuously looping.
    val targetScrollValue = remember(textWidth) {
        if (textWidth > 0) textWidth.toFloat() else 0f
    }

    // Calculate the duration for one full "loop" (scrolling by textWidth)
    val scrollDurationMillis = remember(targetScrollValue, scrollSpeedPxPerSec) {
        if (targetScrollValue > 0 && scrollSpeedPxPerSec > 0) {
            // duration = distance / speed
            (targetScrollValue / (scrollSpeedPxPerSec / 1000f)).toLong()
        } else {
            0L
        }
    }

    val animatedScrollOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = targetScrollValue, // Scroll exactly by the text's width
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = scrollDurationMillis.toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart // Restart causes the jump back, but it's hidden by the duplicated text
        ),
        label = "infiniteScrollOffset"
    )

    // Initially scroll to 0, and then start animation
    LaunchedEffect(targetScrollValue) {
        if (targetScrollValue > 0) {
            delay(delayBeforeScrollMs)
            // No direct scroll here, the animation handles it
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                containerWidth = coordinates.size.width
            }
            .horizontalScroll(scrollState, enabled = false) // Disable user scroll
    ) {
        Row {
            // Display the text content twice to create the seamless loop effect
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    textWidth = coordinates.size.width
                }
            )

            Spacer(Modifier.width(16.dp))

            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }

    // Manually set scroll position using the animated offset
    LaunchedEffect(animatedScrollOffset) {
        // Only scroll if the text is wider than the container
        if (textWidth > containerWidth) {
            scrollState.scrollTo(animatedScrollOffset.toInt())
        }
    }
}