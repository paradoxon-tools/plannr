package de.chennemann.plannr.prototype.visual

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// PROTOTYPE — three visual systems, switchable in one Compose host.
// Finances content and order are fixtures; only visual-system properties are under review.

private enum class VisualVariant(val key: String, val label: String) {
    Ledger("A", "Ledger"),
    Tonal("B", "Tonal"),
    Signal("C", "Signal"),
}

private enum class Appearance { Light, Dark }

private data class VisualTokens(
    val canvas: Color,
    val surface: Color,
    val raised: Color,
    val strong: Color,
    val secondary: Color,
    val outline: Color,
    val inverse: Color,
    val onInverse: Color,
    val interaction: Color,
    val positive: Color,
    val negative: Color,
    val warning: Color,
    val pocketBlue: Color,
    val pocketViolet: Color,
)

private data class TransactionFixture(
    val title: String,
    val meta: String,
    val amount: String,
    val marker: Color? = null,
    val status: String? = null,
)

@Composable
fun VisualSystemPrototypeApp() {
    var variant by remember { mutableStateOf(VisualVariant.Ledger) }
    var appearance by remember { mutableStateOf(Appearance.Light) }
    var reducedMotion by remember { mutableStateOf(false) }
    var largeText by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val tokens = tokensFor(variant, appearance)

    fun cycleVariant(delta: Int) {
        val all = VisualVariant.entries
        variant = all[(variant.ordinal + delta + all.size) % all.size]
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    MaterialTheme(
        colorScheme = if (appearance == Appearance.Light) {
            lightColorScheme(
                background = tokens.canvas,
                surface = tokens.surface,
                onBackground = tokens.strong,
                onSurface = tokens.strong,
                primary = tokens.inverse,
                onPrimary = tokens.onInverse,
                outline = tokens.outline,
            )
        } else {
            darkColorScheme(
                background = tokens.canvas,
                surface = tokens.surface,
                onBackground = tokens.strong,
                onSurface = tokens.strong,
                primary = tokens.inverse,
                onPrimary = tokens.onInverse,
                outline = tokens.outline,
            )
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.canvas)
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.DirectionLeft -> { cycleVariant(-1); true }
                        Key.DirectionRight -> { cycleVariant(1); true }
                        Key.T -> { appearance = appearance.other(); true }
                        Key.M -> { reducedMotion = !reducedMotion; true }
                        Key.F -> { largeText = !largeText; true }
                        else -> false
                    }
                },
        ) {
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = baseDensity.density,
                    fontScale = if (largeText) 2f else 1f,
                ),
            ) {
                Crossfade(
                    targetState = variant,
                    animationSpec = tween(if (reducedMotion) 0 else 180),
                    label = "visual-system-variant",
                ) { selected ->
                    val selectedTokens = tokensFor(selected, appearance)
                    when (selected) {
                        VisualVariant.Ledger -> LedgerPane(selectedTokens)
                        VisualVariant.Tonal -> TonalPane(selectedTokens)
                        VisualVariant.Signal -> SignalPane(selectedTokens)
                    }
                }
            }

            StateStrip(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
                variant = variant,
                appearance = appearance,
                reducedMotion = reducedMotion,
                largeText = largeText,
            )

            PrototypeControls(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                variant = variant,
                appearance = appearance,
                reducedMotion = reducedMotion,
                largeText = largeText,
                onPrevious = { cycleVariant(-1) },
                onNext = { cycleVariant(1) },
                onTheme = { appearance = appearance.other() },
                onMotion = { reducedMotion = !reducedMotion },
                onFontScale = { largeText = !largeText },
            )
        }
    }
}

private fun Appearance.other() = if (this == Appearance.Light) Appearance.Dark else Appearance.Light

private fun tokensFor(variant: VisualVariant, appearance: Appearance): VisualTokens = when (variant) {
    VisualVariant.Ledger -> if (appearance == Appearance.Light) {
        VisualTokens(
            canvas = Color(0xFFF7F7F3), surface = Color(0xFFF7F7F3), raised = Color(0xFFFFFFFF),
            strong = Color(0xFF151614), secondary = Color(0xFF5B5F59), outline = Color(0xFFC6C9C2),
            inverse = Color(0xFF151614), onInverse = Color(0xFFFFFFFF), interaction = Color(0xFF244FBA),
            positive = Color(0xFF23613E), negative = Color(0xFF9A342E), warning = Color(0xFF765400),
            pocketBlue = Color(0xFF315C9B), pocketViolet = Color(0xFF6B4B8D),
        )
    } else {
        VisualTokens(
            canvas = Color(0xFF0B0C0B), surface = Color(0xFF0B0C0B), raised = Color(0xFF171817),
            strong = Color(0xFFF5F6F2), secondary = Color(0xFFB6BAB2), outline = Color(0xFF444740),
            inverse = Color(0xFFF5F6F2), onInverse = Color(0xFF101110), interaction = Color(0xFF8EADFF),
            positive = Color(0xFF86D5A5), negative = Color(0xFFFF9B92), warning = Color(0xFFFFCF69),
            pocketBlue = Color(0xFF91B9FF), pocketViolet = Color(0xFFC6A4EB),
        )
    }

    VisualVariant.Tonal -> if (appearance == Appearance.Light) {
        VisualTokens(
            canvas = Color(0xFFF2F0EA), surface = Color(0xFFE7E4DB), raised = Color(0xFFFFFEFA),
            strong = Color(0xFF171914), secondary = Color(0xFF565C53), outline = Color(0xFFC4C5BB),
            inverse = Color(0xFF171914), onInverse = Color(0xFFFFFFFF), interaction = Color(0xFF174FC4),
            positive = Color(0xFF1D6540), negative = Color(0xFF9C3730), warning = Color(0xFF735400),
            pocketBlue = Color(0xFF315E9E), pocketViolet = Color(0xFF70518F),
        )
    } else {
        VisualTokens(
            canvas = Color(0xFF0C0D0F), surface = Color(0xFF1A1C20), raised = Color(0xFF25282D),
            strong = Color(0xFFF7F7F4), secondary = Color(0xFFBABDC1), outline = Color(0xFF41454C),
            inverse = Color(0xFFF7F7F4), onInverse = Color(0xFF111215), interaction = Color(0xFF91B3FF),
            positive = Color(0xFF86D6A6), negative = Color(0xFFFFA199), warning = Color(0xFFFFD275),
            pocketBlue = Color(0xFF8AB7FF), pocketViolet = Color(0xFFC7A4EC),
        )
    }

    VisualVariant.Signal -> if (appearance == Appearance.Light) {
        VisualTokens(
            canvas = Color(0xFFFFFFFF), surface = Color(0xFFF1F1F1), raised = Color(0xFF000000),
            strong = Color(0xFF000000), secondary = Color(0xFF555555), outline = Color(0xFF000000),
            inverse = Color(0xFF000000), onInverse = Color(0xFFFFFFFF), interaction = Color(0xFF004EC2),
            positive = Color(0xFF126232), negative = Color(0xFF9A241F), warning = Color(0xFF6F5100),
            pocketBlue = Color(0xFF0055A5), pocketViolet = Color(0xFF70428F),
        )
    } else {
        VisualTokens(
            canvas = Color(0xFF000000), surface = Color(0xFF111111), raised = Color(0xFFFFFFFF),
            strong = Color(0xFFFFFFFF), secondary = Color(0xFFBEBEBE), outline = Color(0xFFFFFFFF),
            inverse = Color(0xFFFFFFFF), onInverse = Color(0xFF000000), interaction = Color(0xFF8DB6FF),
            positive = Color(0xFF79DB9A), negative = Color(0xFFFF9990), warning = Color(0xFFFFD169),
            pocketBlue = Color(0xFF7EB9FF), pocketViolet = Color(0xFFD0A2F2),
        )
    }
}

@Composable
private fun LedgerPane(tokens: VisualTokens) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 22.dp, top = 64.dp, end = 22.dp, bottom = 160.dp),
    ) {
        item {
            PrototypeStamp("A / LEDGER", "ruled · quiet · monochrome first", tokens)
            Spacer(Modifier.height(28.dp))
            Label("FINANCES · ALL ACCOUNTS", tokens, FontFamily.SansSerif)
            Text(
                "€ 12,480.20",
                color = tokens.strong,
                fontSize = 36.sp,
                lineHeight = 41.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text("Available now · refreshed 2 min ago", color = tokens.secondary, fontSize = 12.sp)
            LedgerAction("ADD TRANSACTION", tokens)
        }
        item { LedgerHeading("ACCOUNTS", "2", tokens) }
        item { LedgerEntity("Everyday", "N26 · 4 pockets", "€ 4,821.60", tokens.pocketBlue, tokens) }
        item { LedgerEntity("Long term", "ING · 2 pockets", "€ 7,658.60", tokens.pocketViolet, tokens) }
        item { LedgerHeading("RECENT ACTIVITY", "VIEW ALL", tokens) }
        transactionFixtures(tokens).forEach { fixture -> item { LedgerTransaction(fixture, tokens) } }
        item {
            MotionSample(
                title = "Motion · 160 ms crossfade",
                body = "Continuity only; reduced motion removes travel and keeps focus order.",
                tokens = tokens,
                shape = RoundedCornerShape(2.dp),
            )
        }
    }
}

@Composable
private fun LedgerAction(label: String, tokens: VisualTokens) {
    Surface(
        modifier = Modifier.padding(top = 22.dp).fillMaxWidth().heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClickLabel = label) {},
        color = tokens.inverse,
        contentColor = tokens.onInverse,
        shape = RoundedCornerShape(2.dp),
    ) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp)) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Text("+", modifier = Modifier.align(Alignment.CenterEnd), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LedgerHeading(title: String, action: String, tokens: VisualTokens) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 30.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Label(title, tokens, FontFamily.SansSerif, Modifier.weight(1f))
        Text(action, color = tokens.interaction, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
    HorizontalDivider(color = tokens.outline)
}

@Composable
private fun LedgerEntity(name: String, meta: String, amount: String, marker: Color, tokens: VisualTokens) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 66.dp).clickable(role = Role.Button) {}
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(width = 4.dp, height = 38.dp).background(marker))
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(name, color = tokens.strong, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(meta, color = tokens.secondary, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
        }
        Text(amount, color = tokens.strong, fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
    HorizontalDivider(color = tokens.outline)
}

@Composable
private fun LedgerTransaction(fixture: TransactionFixture, tokens: VisualTokens) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp).clickable(role = Role.Button) {}
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(fixture.title, color = tokens.strong, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(fixture.meta, color = tokens.secondary, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(fixture.amount, color = tokens.strong, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            fixture.status?.let { StatusText(it, tokens.warning) }
        }
    }
    HorizontalDivider(color = tokens.outline)
}

@Composable
private fun TonalPane(tokens: VisualTokens) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 64.dp, end = 16.dp, bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            PrototypeStamp("B / TONAL", "layered · soft · one cool accent", tokens)
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                color = tokens.surface,
                shape = RoundedCornerShape(24.dp),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Label("TOTAL POSITION", tokens, FontFamily.SansSerif)
                    Text("€ 12,480.20", color = tokens.strong, fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold)
                    Text("Across 2 accounts · refreshed 2 min ago", color = tokens.secondary, fontSize = 12.sp)
                    Row(Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TonalPill("+ Add", true, tokens)
                        TonalPill("Activity", false, tokens)
                    }
                }
            }
        }
        item {
            TonalSectionHeader("Accounts", "2", tokens)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TonalAccount("Everyday", "€ 4,821.60", "4 pockets", tokens.pocketBlue, tokens, Modifier.weight(1f))
                TonalAccount("Long term", "€ 7,658.60", "2 pockets", tokens.pocketViolet, tokens, Modifier.weight(1f))
            }
        }
        item {
            TonalSectionHeader("Recent activity", "View all", tokens)
            Surface(color = tokens.raised, shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(horizontal = 14.dp)) {
                    transactionFixtures(tokens).forEachIndexed { index, fixture ->
                        TonalTransaction(fixture, tokens)
                        if (index != transactionFixtures(tokens).lastIndex) HorizontalDivider(color = tokens.outline)
                    }
                }
            }
        }
        item {
            MotionSample(
                title = "Motion · 220 ms tonal fade",
                body = "Selection shifts surface tone; reduced motion swaps state immediately.",
                tokens = tokens,
                shape = RoundedCornerShape(20.dp),
            )
        }
    }
}

@Composable
private fun TonalPill(text: String, primary: Boolean, tokens: VisualTokens) {
    Surface(
        modifier = Modifier.heightIn(min = 48.dp).clickable(role = Role.Button) {},
        color = if (primary) tokens.interaction else tokens.raised,
        contentColor = if (primary) tokens.onInverse else tokens.strong,
        shape = CircleShape,
        border = if (primary) null else BorderStroke(1.dp, tokens.outline),
    ) {
        Box(Modifier.padding(horizontal = 17.dp), contentAlignment = Alignment.Center) {
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TonalSectionHeader(title: String, action: String, tokens: VisualTokens) {
    Row(Modifier.fillMaxWidth().padding(start = 4.dp, top = 8.dp, end = 4.dp, bottom = 7.dp)) {
        Text(title, color = tokens.strong, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(action, color = tokens.interaction, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TonalAccount(
    name: String,
    amount: String,
    detail: String,
    marker: Color,
    tokens: VisualTokens,
    modifier: Modifier,
) {
    Surface(modifier = modifier.clickable(role = Role.Button) {}, color = tokens.raised, shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(15.dp)) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(marker).semantics { contentDescription = "$name identity marker" })
            Text(name, color = tokens.strong, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 14.dp))
            Text(amount, color = tokens.strong, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 3.dp))
            Text(detail, color = tokens.secondary, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp))
        }
    }
}

@Composable
private fun TonalTransaction(fixture: TransactionFixture, tokens: VisualTokens) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 68.dp).clickable(role = Role.Button) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(color = tokens.surface, shape = RoundedCornerShape(12.dp)) {
            Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                Text(fixture.title.take(1), color = tokens.strong, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        Column(Modifier.weight(1f).padding(horizontal = 11.dp)) {
            Text(fixture.title, color = tokens.strong, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(fixture.meta, color = tokens.secondary, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(fixture.amount, color = tokens.strong, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            fixture.status?.let { StatusText(it, tokens.warning) }
        }
    }
}

@Composable
private fun SignalPane(tokens: VisualTokens) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, top = 64.dp, end = 14.dp, bottom = 160.dp),
    ) {
        item {
            PrototypeStamp("C / SIGNAL", "framed · hard contrast · labelled color", tokens)
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                color = tokens.raised,
                contentColor = tokens.onInverse,
                shape = RoundedCornerShape(0.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("TOTAL / ALL ACCOUNTS", fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("€12,480.20", fontFamily = FontFamily.Monospace, fontSize = 31.sp, lineHeight = 37.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp))
                    Text("LIVE · REFRESHED 2M", fontFamily = FontFamily.Monospace, fontSize = 10.sp, modifier = Modifier.padding(top = 7.dp))
                }
            }
            SignalAction("+ NEW TRANSACTION", tokens)
        }
        item { SignalHeader("01", "ACCOUNTS", tokens) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SignalAccount("A", "EVERYDAY", "€4,821.60", "BLUE / 4 POCKETS", tokens.pocketBlue, tokens, Modifier.weight(1f))
                SignalAccount("B", "LONG TERM", "€7,658.60", "VIOLET / 2 POCKETS", tokens.pocketViolet, tokens, Modifier.weight(1f))
            }
        }
        item { SignalHeader("02", "RECENT ACTIVITY", tokens) }
        transactionFixtures(tokens).forEach { fixture -> item { SignalTransaction(fixture, tokens) } }
        item {
            MotionSample(
                title = "MOTION / 120 MS CUT",
                body = "Hard state change with a short opacity bridge; reduced mode uses a direct cut.",
                tokens = tokens,
                shape = RoundedCornerShape(0.dp),
            )
        }
    }
}

@Composable
private fun SignalAction(label: String, tokens: VisualTokens) {
    Surface(
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable(role = Role.Button) {},
        color = tokens.canvas,
        contentColor = tokens.strong,
        border = BorderStroke(2.dp, tokens.outline),
        shape = RoundedCornerShape(0.dp),
    ) {
        Row(Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Text("ENTER →", color = tokens.interaction, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun SignalHeader(index: String, title: String, tokens: VisualTokens) {
    Row(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp), verticalAlignment = Alignment.Bottom) {
        Text(index, color = tokens.interaction, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Text(title, color = tokens.strong, fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(start = 8.dp).weight(1f))
        Text("OPEN →", color = tokens.secondary, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
    }
}

@Composable
private fun SignalAccount(
    code: String,
    name: String,
    amount: String,
    identity: String,
    marker: Color,
    tokens: VisualTokens,
    modifier: Modifier,
) {
    Surface(modifier = modifier.clickable(role = Role.Button) {}, color = tokens.surface, border = BorderStroke(2.dp, tokens.outline), shape = RoundedCornerShape(0.dp)) {
        Column {
            Row(Modifier.fillMaxWidth().background(marker).padding(horizontal = 9.dp, vertical = 6.dp)) {
                Text(code, color = tokens.onInverse, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 10.sp)
                Text(identity, color = tokens.onInverse, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 8.sp, modifier = Modifier.padding(start = 8.dp))
            }
            Column(Modifier.padding(11.dp)) {
                Text(name, color = tokens.strong, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(amount, color = tokens.strong, fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 10.dp))
            }
        }
    }
}

@Composable
private fun SignalTransaction(fixture: TransactionFixture, tokens: VisualTokens) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp).clickable(role = Role.Button) {}
            .background(tokens.surface).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("→", color = fixture.marker ?: tokens.secondary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
        Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text(fixture.title.uppercase(), color = tokens.strong, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(fixture.meta.uppercase(), color = tokens.secondary, fontFamily = FontFamily.Monospace, fontSize = 8.sp, modifier = Modifier.padding(top = 3.dp))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(fixture.amount, color = tokens.strong, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Black)
            fixture.status?.let { StatusText(it.uppercase(), tokens.warning, FontFamily.Monospace) }
        }
    }
    HorizontalDivider(thickness = 2.dp, color = tokens.outline)
}

@Composable
private fun PrototypeStamp(code: String, detail: String, tokens: VisualTokens) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(code, color = tokens.strong, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
        Text(
            "FIXTURE · VISUAL SYSTEM ONLY",
            color = tokens.secondary,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = .8.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
    Text(detail, color = tokens.secondary, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun Label(text: String, tokens: VisualTokens, font: FontFamily, modifier: Modifier = Modifier) {
    Text(text, color = tokens.secondary, fontFamily = font, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp, modifier = modifier)
}

@Composable
private fun MotionSample(title: String, body: String, tokens: VisualTokens, shape: RoundedCornerShape) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
        color = tokens.surface,
        border = BorderStroke(1.dp, tokens.outline),
        shape = shape,
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = tokens.strong, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = .8.sp)
            Text(body, color = tokens.secondary, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun StatusText(text: String, color: Color, font: FontFamily = FontFamily.SansSerif) {
    Text(text, color = color, fontFamily = font, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 3.dp))
}

private fun transactionFixtures(tokens: VisualTokens) = listOf(
    TransactionFixture("Weekly groceries", "Today · Everyday", "− €64.20", tokens.pocketBlue),
    TransactionFixture("Salary", "Yesterday · Long term", "+ €3,240.00", tokens.pocketViolet),
    TransactionFixture("Electricity", "Tomorrow · Everyday", "− €87.00", tokens.warning, "PENDING"),
)

@Composable
private fun StateStrip(
    modifier: Modifier,
    variant: VisualVariant,
    appearance: Appearance,
    reducedMotion: Boolean,
    largeText: Boolean,
) {
    Surface(modifier = modifier.widthIn(max = 430.dp), color = Color(0xEC1B1D21), shape = RoundedCornerShape(9.dp)) {
        Text(
            "STATE · ${variant.key}/${variant.label} · ${appearance.name} · ${if (reducedMotion) "reduced" else "full"} motion · ${if (largeText) "200%" else "100%"} text",
            color = Color(0xFFF5F5F3),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = .5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun PrototypeControls(
    modifier: Modifier,
    variant: VisualVariant,
    appearance: Appearance,
    reducedMotion: Boolean,
    largeText: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onTheme: () -> Unit,
    onMotion: () -> Unit,
    onFontScale: () -> Unit,
) {
    Surface(modifier = modifier, color = Color(0xF21B1D21), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SettingButton(if (appearance == Appearance.Light) "LIGHT" else "DARK", "Toggle theme", onTheme)
                SettingButton(if (reducedMotion) "REDUCED" else "MOTION", "Toggle reduced motion", onMotion)
                SettingButton(if (largeText) "TEXT 200" else "TEXT 100", "Toggle text scale", onFontScale)
            }
            Row(Modifier.padding(top = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                SwitcherButton("←", "Previous visual system", onPrevious)
                Column(Modifier.width(230.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("THROWAWAY COMPOSE PROTOTYPE", color = Color(0xFFAEB2BA), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Text("${variant.key} — ${variant.label}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                SwitcherButton("→", "Next visual system", onNext)
            }
        }
    }
}

@Composable
private fun SettingButton(label: String, description: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.heightIn(min = 36.dp).clickable(role = Role.Button, onClickLabel = description, onClick = onClick)
            .semantics { contentDescription = description; stateDescription = label },
        color = Color(0xFF30333A),
        shape = RoundedCornerShape(9.dp),
    ) {
        Box(Modifier.padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
            Text(label, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = .7.sp)
        }
    }
}

@Composable
private fun SwitcherButton(label: String, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(42.dp).clip(CircleShape).background(Color(0xFF30333A))
            .clickable(role = Role.Button, onClickLabel = description, onClick = onClick)
            .semantics { contentDescription = description; role = Role.Button },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
