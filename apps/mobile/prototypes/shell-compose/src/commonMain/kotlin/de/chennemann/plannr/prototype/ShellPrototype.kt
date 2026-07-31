package de.chennemann.plannr.prototype

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

// PROTOTYPE — one reference Compose pager, three secondary-destination treatments.

private val Paper = Color(0xFFF3F2EE)
private val PaperRaised = Color(0xFFFFFFFF)
private val Ink = Color(0xFF181A1E)
private val InkMuted = Color(0xFF777A80)
private val Hairline = Color(0xFFD9D8D3)
private val Positive = Color(0xFF276340)
private val Negative = Color(0xFF963D35)

private enum class Feature(val shortLabel: String, val glyph: String) {
    Dashboard("Dashboard", "D"),
    Chat("Chat", "C"),
    Groceries("Groceries", "G"),
    Finances("Finances", "F"),
}

private enum class PrototypeVariant(val key: String, val label: String) {
    Takeover("A", "Full takeover"),
    FeatureStack("B", "Feature stack"),
    FocusedSheet("C", "Focused sheet"),
}

private enum class FeatureLayer {
    Overview,
    Activity,
}

@Composable
fun ShellPrototypeApp() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            background = Paper,
            surface = PaperRaised,
            onBackground = Ink,
            onSurface = Ink,
            primary = Ink,
            onPrimary = PaperRaised,
            outline = Hairline,
        ),
    ) {
        var variantName by rememberSaveable { mutableStateOf(PrototypeVariant.Takeover.name) }
        var layerName by rememberSaveable { mutableStateOf(FeatureLayer.Overview.name) }
        val variant = PrototypeVariant.valueOf(variantName)
        val layer = FeatureLayer.valueOf(layerName)
        val pagerState = rememberPagerState(initialPage = Feature.Finances.ordinal) { Feature.entries.size }
        val focusRequester = remember { FocusRequester() }

        fun changeVariant(delta: Int) {
            val variants = PrototypeVariant.entries
            val next = (variant.ordinal + delta + variants.size) % variants.size
            variantName = variants[next].name
        }

        fun closeSecondary(): Boolean {
            if (layer == FeatureLayer.Overview) return false
            layerName = FeatureLayer.Overview.name
            return true
        }

        LaunchedEffect(Unit) {
            pagerState.scrollToPage(Feature.Finances.ordinal)
            focusRequester.requestFocus()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Paper)
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.DirectionLeft -> {
                            changeVariant(-1)
                            true
                        }

                        Key.DirectionRight -> {
                            changeVariant(1)
                            true
                        }

                        Key.Escape -> closeSecondary()
                        else -> false
                    }
                },
        ) {
            ReferencePagerShell(
                modifier = Modifier.fillMaxSize().padding(top = 108.dp),
                pagerState = pagerState,
                layer = layer,
                variant = variant,
                onOpenActivity = { layerName = FeatureLayer.Activity.name },
                onCloseActivity = { layerName = FeatureLayer.Overview.name },
            )

            when {
                layer == FeatureLayer.Activity && variant == PrototypeVariant.Takeover -> {
                    ActivityDestination(
                        modifier = Modifier.fillMaxSize().padding(top = 108.dp),
                        label = "FULL TAKEOVER",
                        onBack = { layerName = FeatureLayer.Overview.name },
                    )
                }

                layer == FeatureLayer.Activity && variant == PrototypeVariant.FocusedSheet -> {
                    FocusedActivitySheet(onBack = { layerName = FeatureLayer.Overview.name })
                }
            }

            StateReadout(
                modifier = Modifier.align(Alignment.TopStart).padding(start = 12.dp, top = 62.dp),
                pagerState = pagerState,
                layer = layer,
                variant = variant,
            )

            PrototypeSwitcher(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp),
                variant = variant,
                onPrevious = { changeVariant(-1) },
                onNext = { changeVariant(1) },
            )
        }
    }
}

@Composable
private fun ReferencePagerShell(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    layer: FeatureLayer,
    variant: PrototypeVariant,
    onOpenActivity: () -> Unit,
    onCloseActivity: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerEnabled = layer == FeatureLayer.Overview

    Column(modifier) {
        HorizontalPager(
            state = pagerState,
            key = { Feature.entries[it].name },
            userScrollEnabled = pagerEnabled,
            beyondViewportPageCount = 0,
            modifier = Modifier.weight(1f),
        ) { page ->
            val feature = Feature.entries[page]
            if (
                feature == Feature.Finances &&
                layer == FeatureLayer.Activity &&
                variant == PrototypeVariant.FeatureStack
            ) {
                ActivityDestination(
                    modifier = Modifier.fillMaxSize(),
                    label = "FEATURE-LOCAL STACK",
                    onBack = onCloseActivity,
                )
            } else {
                FeatureOverview(feature = feature, onOpenActivity = onOpenActivity)
            }
        }

        ExpandingPagerNavigation(
            pagerState = pagerState,
            enabled = pagerEnabled,
            onSelect = { page ->
                coroutineScope.launch { pagerState.animateScrollToPage(page) }
            },
        )
    }
}

@Composable
private fun ExpandingPagerNavigation(
    pagerState: PagerState,
    enabled: Boolean,
    onSelect: (Int) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .background(Paper)
            .semantics {
                stateDescription = if (enabled) "Feature navigation available" else "Feature navigation locked"
            },
    ) {
        val iconWidth = 48.dp
        val horizontalPadding = 12.dp
        val labelSpace = (maxWidth - horizontalPadding * 2 - iconWidth * Feature.entries.size)
            .coerceAtLeast(0.dp)

        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = horizontalPadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            Feature.entries.forEachIndexed { index, feature ->
                val distance = (
                    pagerState.currentPage - index + pagerState.currentPageOffsetFraction
                ).absoluteValue
                val emphasis = (1f - distance).coerceIn(0f, 1f)
                val foreground = lerp(InkMuted, Ink, emphasis)

                Row(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(
                            enabled = enabled && index != pagerState.settledPage,
                            role = Role.Button,
                            onClickLabel = "Open ${feature.shortLabel}",
                        ) { onSelect(index) }
                        .alpha(if (enabled) 1f else 0.45f)
                        .semantics {
                            contentDescription = feature.shortLabel
                            stateDescription = when {
                                index == pagerState.settledPage -> "Selected"
                                enabled -> "Not selected"
                                else -> "Unavailable while focused content is open"
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.size(iconWidth),
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(
                            color = if (emphasis > 0.5f) PaperRaised else Color.Transparent,
                            shape = CircleShape,
                        ) {
                            Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                Text(feature.glyph, color = foreground, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(labelSpace * emphasis)
                            .height(48.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            text = feature.shortLabel,
                            color = foreground,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureOverview(feature: Feature, onOpenActivity: () -> Unit) {
    if (feature != Feature.Finances) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(30.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("0${feature.ordinal + 1}", color = Hairline, fontSize = 78.sp, fontWeight = FontWeight.Light)
            Spacer(Modifier.height(16.dp))
            Text(feature.shortLabel, color = Ink, fontSize = 34.sp, fontWeight = FontWeight.Black)
            Text(
                "A future feature pane. Present only to exercise the real Compose pager and page-local state.",
                color = InkMuted,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                modifier = Modifier.padding(top = 8.dp).widthIn(max = 310.dp),
            )
            Surface(
                modifier = Modifier.padding(top = 24.dp),
                border = BorderStroke(1.dp, Hairline),
                color = Color.Transparent,
                shape = RoundedCornerShape(6.dp),
            ) {
                Text(
                    "PLACEHOLDER FIXTURE",
                    color = InkMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            top = 28.dp,
            end = 20.dp,
            bottom = 30.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "summary") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Overline("NET POSITION")
                    Text("€ 12,480.20", color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Text("+ € 640.80 this month", color = Positive, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {},
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                ) {
                    Text("+", fontSize = 22.sp)
                }
            }
        }

        item(key = "metrics") {
            Card(colors = CardDefaults.cardColors(containerColor = PaperRaised), shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp)) {
                    Metric("INCOME", "€ 3,240", Positive, Modifier.weight(1f))
                    Metric("SPENT", "€ 2,599", Negative, Modifier.weight(1f))
                }
            }
        }

        item(key = "accounts-title") { SectionTitle("Accounts") }
        item(key = "everyday") { EntityRow("Everyday", "N26 · 4 pockets", "€ 4,821.60") }
        item(key = "long-term") { EntityRow("Long term", "ING · 2 pockets", "€ 7,658.60") }
        item(key = "activity-title") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { SectionTitle("Recent activity") }
                OutlinedButton(onClick = onOpenActivity) { Text("VIEW ALL", fontSize = 10.sp) }
            }
        }
        item(key = "groceries") { EntityRow("Weekly groceries", "Today · Groceries", "− € 64.20") }
        item(key = "salary") { EntityRow("Salary", "Yesterday · Income", "+ € 3,240") }
        item(key = "scroll-proof") {
            Surface(color = Color(0xFFE7E6E1), shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Vertical scroll stays with this pane", color = Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(
                        "Drag this list vertically, then test the real Compose horizontal pager at the overview and while activity is open.",
                        color = InkMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityDestination(modifier: Modifier = Modifier, label: String, onBack: () -> Unit) {
    Surface(modifier = modifier, color = Paper) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onBack, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp)) {
                    Text("← Back")
                }
                Column(Modifier.padding(start = 12.dp)) {
                    Overline(label)
                    Text("Recent activity", color = Ink, fontSize = 23.sp, fontWeight = FontWeight.Black)
                }
            }
            ActivityList(Modifier.weight(1f).padding(top = 18.dp))
        }
    }
}

@Composable
private fun FocusedActivitySheet(onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0x66000000))) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.82f).align(Alignment.BottomCenter),
            color = Paper,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            shadowElevation = 18.dp,
        ) {
            Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
                Box(Modifier.width(38.dp).height(4.dp).clip(CircleShape).background(Color(0xFFAAA8A2)).align(Alignment.CenterHorizontally))
                Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = onBack) { Text("Close") }
                    Column(Modifier.padding(start = 12.dp)) {
                        Overline("FOCUSED SHEET")
                        Text("Recent activity", color = Ink, fontSize = 23.sp, fontWeight = FontWeight.Black)
                    }
                }
                ActivityList(Modifier.weight(1f).padding(top = 12.dp))
            }
        }
    }
}

@Composable
private fun ActivityList(modifier: Modifier = Modifier) {
    val entries = remember {
        listOf(
            Triple("Weekly groceries", "Today · Everyday", "− € 64.20"),
            Triple("Coffee", "Today · Everyday", "− € 4.60"),
            Triple("Salary", "Yesterday · Everyday", "+ € 3,240"),
            Triple("Rent", "Yesterday · Everyday", "− € 980"),
            Triple("Savings transfer", "Yesterday · Long term", "− € 400"),
        )
    }
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(entries, key = { it.first }) { entry ->
            EntityRow(entry.first, entry.second, entry.third)
        }
    }
}

@Composable
private fun PrototypeSwitcher(
    modifier: Modifier,
    variant: PrototypeVariant,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Surface(modifier = modifier, color = Ink, shape = RoundedCornerShape(22.dp), shadowElevation = 10.dp) {
        Row(
            modifier = Modifier.height(44.dp).padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SwitcherButton("←", "Previous prototype", onPrevious)
            Column(Modifier.width(210.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("COMPOSE PROTOTYPE", color = Color(0xFFA4A8B0), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.1.sp)
                Text("${variant.key} — ${variant.label}", color = PaperRaised, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            SwitcherButton("→", "Next prototype", onNext)
        }
    }
}

@Composable
private fun SwitcherButton(label: String, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(0xFF30333A))
            .clickable(role = Role.Button, onClickLabel = description, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = PaperRaised, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StateReadout(
    modifier: Modifier,
    pagerState: PagerState,
    layer: FeatureLayer,
    variant: PrototypeVariant,
) {
    val feature = Feature.entries[pagerState.settledPage]
    val owner = if (layer == FeatureLayer.Overview) "root HorizontalPager" else "secondary destination · pager locked"
    Surface(modifier = modifier.widthIn(max = 330.dp), color = Color(0xE6181A1E), shape = RoundedCornerShape(9.dp)) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
            Text(
                "STATE · ${feature.shortLabel} · ${layer.name.lowercase()} · ${variant.key}",
                color = Color(0xFFA4A8B0),
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )
            Text(
                "$owner · page ${pagerState.currentPage} / settled ${pagerState.settledPage} / offset ${pagerState.currentPageOffsetFraction}",
                color = PaperRaised,
                fontSize = 9.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun Metric(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = 8.dp)) {
        Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Text(value, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun EntityRow(label: String, meta: String, value: String) {
    Card(colors = CardDefaults.cardColors(containerColor = PaperRaised), shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 66.dp).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Color(0xFFECEBE7), shape = RoundedCornerShape(11.dp)) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    Text(label.take(1), color = Ink, fontWeight = FontWeight.Black)
                }
            }
            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(label, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(meta, color = InkMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
            }
            Text(value, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun Overline(text: String) {
    Text(text, color = InkMuted, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.3.sp)
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 0.2.sp)
}
