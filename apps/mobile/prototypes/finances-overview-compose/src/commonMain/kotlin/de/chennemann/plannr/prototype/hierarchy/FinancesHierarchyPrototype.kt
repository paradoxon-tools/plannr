package de.chennemann.plannr.prototype.hierarchy

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.runtime.key
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// PROTOTYPE — three Guided-attention Finances hierarchies, switchable in one Compose host.
// The reviewed content inventory, Ledger visual language, and full-takeover secondary screens are fixed inputs.

private enum class HierarchyVariant(val key: String, val label: String, val thesis: String) {
    Questions("A", "Question ladder", "Answer soon, attention, and money in one predictable scan."),
    Accounts("B", "Account register", "Lead with every account, then explain change and exceptions."),
    Triage("C", "Triage briefing", "Lead with action and exceptions, then reveal activity and holdings."),
}

private enum class Appearance { Light, Dark }

private enum class AttentionState { NeedsReview, Clear }

private data class LedgerTokens(
    val canvas: Color,
    val surface: Color,
    val strong: Color,
    val secondary: Color,
    val outline: Color,
    val inverse: Color,
    val onInverse: Color,
    val interaction: Color,
    val positive: Color,
    val warning: Color,
    val blue: Color,
    val violet: Color,
    val green: Color,
)

private data class FocusedDestination(
    val eyebrow: String,
    val title: String,
    val summary: String,
    val primaryAction: String,
)

@Composable
fun FinancesHierarchyPrototypeApp() {
    var variant by remember { mutableStateOf(HierarchyVariant.Questions) }
    var appearance by remember { mutableStateOf(Appearance.Light) }
    var largeText by remember { mutableStateOf(false) }
    var attentionState by remember { mutableStateOf(AttentionState.NeedsReview) }
    var focused by remember { mutableStateOf<FocusedDestination?>(null) }
    val focusRequester = remember { FocusRequester() }
    val tokens = tokensFor(appearance)

    fun cycleVariant(delta: Int) {
        val all = HierarchyVariant.entries
        variant = all[(variant.ordinal + delta + all.size) % all.size]
        focused = null
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
                        Key.F -> { largeText = !largeText; true }
                        Key.A -> { attentionState = attentionState.other(); true }
                        Key.Escape -> if (focused != null) { focused = null; true } else false
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
                    animationSpec = tween(160),
                    label = "hierarchy-variant",
                ) { selected ->
                    key(selected) {
                            when (selected) {
                                HierarchyVariant.Questions -> QuestionLadderPane(tokens, attentionState, onOpen = { focused = it })
                                HierarchyVariant.Accounts -> AccountRegisterPane(tokens, attentionState, onOpen = { focused = it })
                                HierarchyVariant.Triage -> TriageBriefingPane(tokens, attentionState, onOpen = { focused = it })
                            }
                    }
                }
                focused?.let { destination ->
                    Surface(modifier = Modifier.fillMaxSize(), color = tokens.canvas) {
                        FocusedDestinationPane(destination, tokens, onBack = { focused = null })
                    }
                }
            }

            StateStrip(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
                variant = variant,
                appearance = appearance,
                largeText = largeText,
                attentionState = attentionState,
                focused = focused,
            )
            PrototypeControls(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                variant = variant,
                appearance = appearance,
                largeText = largeText,
                attentionState = attentionState,
                onPrevious = { cycleVariant(-1) },
                onNext = { cycleVariant(1) },
                onTheme = { appearance = appearance.other() },
                onFontScale = { largeText = !largeText },
                onAttentionState = { attentionState = attentionState.other() },
            )
        }
    }
}

private fun Appearance.other() = if (this == Appearance.Light) Appearance.Dark else Appearance.Light

private fun AttentionState.other() = if (this == AttentionState.NeedsReview) AttentionState.Clear else AttentionState.NeedsReview

private fun tokensFor(appearance: Appearance) = if (appearance == Appearance.Light) {
    LedgerTokens(
        canvas = Color(0xFFF7F7F3),
        surface = Color(0xFFFFFFFF),
        strong = Color(0xFF151614),
        secondary = Color(0xFF5B5F59),
        outline = Color(0xFFC6C9C2),
        inverse = Color(0xFF151614),
        onInverse = Color(0xFFFFFFFF),
        interaction = Color(0xFF244FBA),
        positive = Color(0xFF23613E),
        warning = Color(0xFF765400),
        blue = Color(0xFF315C9B),
        violet = Color(0xFF6B4B8D),
        green = Color(0xFF347052),
    )
} else {
    LedgerTokens(
        canvas = Color(0xFF0B0C0B),
        surface = Color(0xFF171817),
        strong = Color(0xFFF5F6F2),
        secondary = Color(0xFFB6BAB2),
        outline = Color(0xFF444740),
        inverse = Color(0xFFF5F6F2),
        onInverse = Color(0xFF101110),
        interaction = Color(0xFF8EADFF),
        positive = Color(0xFF86D5A5),
        warning = Color(0xFFFFCF69),
        blue = Color(0xFF91B9FF),
        violet = Color(0xFFC6A4EB),
        green = Color(0xFF8CD3AE),
    )
}

@Composable
private fun QuestionLadderPane(
    tokens: LedgerTokens,
    attentionState: AttentionState,
    onOpen: (FocusedDestination) -> Unit,
) {
    LedgerList(HierarchyVariant.Questions) {
        item { PrototypeHeading(HierarchyVariant.Questions, tokens) }
        item { PrimaryAction("ADD TRANSACTION", tokens) { onOpen(transactionDestination()) } }
        item {
            SectionHeading("WHAT CHANGES SOON?", "+ €2,732.80 · 9 entries · 30 days", "ALL TRANSACTIONS", tokens) {
                onOpen(allTransactionsDestination())
            }
            LedgerRow("Electricity", "Next · tomorrow · Personal profile", "− €87.00", tokens, status = "PENDING") {
                onOpen(contractDestination("Electricity"))
            }
        }
        item {
            SectionHeading("WHAT NEEDS ATTENTION?", attentionSummary(attentionState), "REVIEW ALL", tokens) {
                onOpen(plansDestination())
            }
            if (attentionState == AttentionState.NeedsReview) {
                LedgerRow("Japan", "Saving goal · Personal profile · 3 weeks behind*", "€ 720.00", tokens, status = "BEHIND") {
                    onOpen(goalDestination("Japan"))
                }
                LedgerRow("Electricity", "Contract · Personal profile · due tomorrow", "€ 87.00", tokens, status = "DUE") {
                    onOpen(contractDestination("Electricity"))
                }
            } else {
                clearAttentionState(tokens)
            }
        }
        item {
            SectionHeading("WHERE IS MY MONEY?", "Every account", "OPEN ACCOUNTS", tokens) {
                onOpen(accountsDestination())
            }
            LedgerRow("Everyday", "Account €4,821.60 · Available €3,210.40", "€ 4,821.60", tokens, marker = tokens.blue) {
                onOpen(accountDestination("Everyday"))
            }
            LedgerRow("Long term", "Account €7,658.60 · Available €98.60", "€ 7,658.60", tokens, marker = tokens.violet) {
                onOpen(accountDestination("Long term"))
            }
        }
    }
}

@Composable
private fun AccountRegisterPane(
    tokens: LedgerTokens,
    attentionState: AttentionState,
    onOpen: (FocusedDestination) -> Unit,
) {
    LedgerList(HierarchyVariant.Accounts) {
        item { PrototypeHeading(HierarchyVariant.Accounts, tokens) }
        item {
            SectionHeading("ACCOUNT REGISTER", "2 complete accounts", "OPEN ACCOUNTS", tokens) {
                onOpen(accountsDestination())
            }
            StructureAccountHeader("Everyday", "N26", "€ 4,821.60", tokens.blue, tokens) {
                onOpen(accountDestination("Everyday"))
            }
            SummaryPair("ACCOUNT BALANCE", "€ 4,821.60", "AVAILABLE BALANCE", "€ 3,210.40", tokens)
            TextAction("OPEN EVERYDAY", tokens) { onOpen(accountDestination("Everyday")) }

            StructureAccountHeader("Long term", "ING", "€ 7,658.60", tokens.violet, tokens) {
                onOpen(accountDestination("Long term"))
            }
            SummaryPair("ACCOUNT BALANCE", "€ 7,658.60", "AVAILABLE BALANCE", "€ 98.60", tokens)
            TextAction("OPEN LONG TERM", tokens) { onOpen(accountDestination("Long term")) }
        }
        item {
            SectionHeading("UPCOMING SUMMARY", "+ €2,732.80 · 9 entries · 30 days", "ALL TRANSACTIONS", tokens) {
                onOpen(allTransactionsDestination())
            }
            LedgerRow("Electricity", "Nearest · tomorrow · Personal profile", "− €87.00", tokens, status = "PENDING") {
                onOpen(contractDestination("Electricity"))
            }
            PrimaryAction("ADD TRANSACTION", tokens) { onOpen(transactionDestination()) }
        }
        item {
            SectionHeading("ATTENTION REGISTER", attentionSummary(attentionState), "REVIEW ALL", tokens) {
                onOpen(plansDestination())
            }
            if (attentionState == AttentionState.NeedsReview) {
                LedgerRow("01 · Japan", "SAVING GOAL · Personal profile · 3 weeks behind*", "€ 720.00", tokens, status = "BEHIND") {
                    onOpen(goalDestination("Japan"))
                }
                LedgerRow("02 · Electricity", "CONTRACT · Personal profile · due tomorrow", "€ 87.00", tokens, status = "DUE") {
                    onOpen(contractDestination("Electricity"))
                }
            } else {
                clearAttentionState(tokens)
            }
        }
    }
}

@Composable
private fun TriageBriefingPane(
    tokens: LedgerTokens,
    attentionState: AttentionState,
    onOpen: (FocusedDestination) -> Unit,
) {
    LedgerList(HierarchyVariant.Triage) {
        item { PrototypeHeading(HierarchyVariant.Triage, tokens) }
        item {
            Surface(color = tokens.surface, shape = RoundedCornerShape(2.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Label("FINANCES BRIEFING", tokens)
                    SummaryPair(
                        "NEXT 30 DAYS",
                        "+ €2,732.80 · 9 entries",
                        "NEEDS ATTENTION",
                        if (attentionState == AttentionState.NeedsReview) "2 items" else "Clear",
                        tokens,
                    )
                    PrimaryAction("ADD TRANSACTION", tokens) { onOpen(transactionDestination()) }
                }
            }
        }
        item {
            SectionHeading("REVIEW FIRST", attentionSummary(attentionState), "ALL CONTRACTS & GOALS", tokens) {
                onOpen(plansDestination())
            }
            if (attentionState == AttentionState.NeedsReview) {
                LedgerRow("Japan", "Saving goal · Personal profile", "€ 720.00", tokens, marker = tokens.violet, status = "3 WEEKS BEHIND*") {
                    onOpen(goalDestination("Japan"))
                }
                LedgerRow("Electricity", "Contract · Personal profile", "€ 87.00", tokens, marker = tokens.green, status = "DUE TOMORROW") {
                    onOpen(contractDestination("Electricity"))
                }
            } else {
                clearAttentionState(tokens)
            }
        }
        item {
            SectionHeading("THEN, WHAT CHANGES?", "+ €2,732.80 · 9 entries · 30 days", "ALL TRANSACTIONS", tokens) {
                onOpen(allTransactionsDestination())
            }
            LedgerRow("Tomorrow · Electricity", "Nearest upcoming entry · Personal profile", "− €87.00", tokens, status = "PENDING") {
                onOpen(contractDestination("Electricity"))
            }
        }
        item {
            SectionHeading("FINALLY, WHERE IS IT?", "Complete account inventory", "OPEN ACCOUNTS", tokens) {
                onOpen(accountsDestination())
            }
            LedgerRow("Everyday", "N26 · Account €4,821.60 · Available €3,210.40", "€ 4,821.60", tokens, marker = tokens.blue) {
                onOpen(accountDestination("Everyday"))
            }
            LedgerRow("Long term", "ING · Account €7,658.60 · Available €98.60", "€ 7,658.60", tokens, marker = tokens.violet) {
                onOpen(accountDestination("Long term"))
            }
        }
    }
}

@Composable
private fun LedgerList(variant: HierarchyVariant, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    val state = remember(variant) { LazyListState() }
    LaunchedEffect(variant) { state.scrollToItem(0) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(start = 22.dp, top = 62.dp, end = 22.dp, bottom = 150.dp),
        content = content,
    )
}

@Composable
private fun PrototypeHeading(variant: HierarchyVariant, tokens: LedgerTokens) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("${variant.key} / ${variant.label.uppercase()}", color = tokens.strong, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.1.sp)
        Text(
            "FIXTURE · HIERARCHY ONLY",
            color = tokens.secondary,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
    Text(variant.thesis, color = tokens.secondary, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))
}

@Composable
private fun ProfileLine(name: String, detail: String, tokens: LedgerTokens, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(tokens.blue))
        Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text(name, color = tokens.strong, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(detail, color = tokens.secondary, fontSize = 9.sp)
        }
        Text("CHANGE →", color = tokens.interaction, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
    HorizontalDivider(color = tokens.outline)
}

@Composable
private fun HeroPosition(label: String, amount: String, detail: String, tokens: LedgerTokens) {
    Column(Modifier.padding(top = 24.dp)) {
        Label(label, tokens)
        Text(amount, color = tokens.strong, fontSize = 36.sp, lineHeight = 42.sp, fontWeight = FontWeight.SemiBold)
        Text(detail, color = tokens.secondary, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
        Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Box(Modifier.weight(.386f).height(5.dp).background(tokens.blue))
            Box(Modifier.weight(.614f).height(5.dp).background(tokens.violet))
        }
    }
}

@Composable
private fun PrimaryAction(label: String, tokens: LedgerTokens, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.padding(top = 20.dp).fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onClick),
        color = tokens.inverse,
        contentColor = tokens.onInverse,
        shape = RoundedCornerShape(2.dp),
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, modifier = Modifier.weight(1f))
            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CompactAction(label: String, tokens: LedgerTokens, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onClick),
        color = tokens.inverse,
        contentColor = tokens.onInverse,
        shape = RoundedCornerShape(2.dp),
    ) {
        Box(Modifier.padding(horizontal = 15.dp), contentAlignment = Alignment.Center) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun SectionHeading(title: String, summary: String, action: String, tokens: LedgerTokens, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 30.dp, bottom = 8.dp).clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = tokens.strong, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.1.sp)
            Text(summary, color = tokens.secondary, fontSize = 9.sp, modifier = Modifier.padding(top = 3.dp))
        }
        Text("$action →", color = tokens.interaction, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = .5.sp)
    }
    HorizontalDivider(color = tokens.outline)
}

@Composable
private fun LedgerRow(
    title: String,
    detail: String,
    amount: String,
    tokens: LedgerTokens,
    marker: Color? = null,
    status: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp).clickable(role = Role.Button, onClick = onClick).padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        marker?.let { Box(Modifier.size(width = 4.dp, height = 34.dp).background(it)) }
        Column(Modifier.weight(1f).padding(start = if (marker == null) 0.dp else 11.dp, end = 10.dp)) {
            Text(title, color = tokens.strong, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = tokens.secondary, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
        }
        Column(horizontalAlignment = Alignment.End) {
            if (amount.isNotEmpty()) Text(amount, color = tokens.strong, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            if (status != null) Text(status, color = tokens.warning, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 3.dp))
        }
    }
    HorizontalDivider(color = tokens.outline)
}

@Composable
private fun ProgressRow(
    title: String,
    detail: String,
    percent: String,
    progress: Float,
    color: Color,
    tokens: LedgerTokens,
    onClick: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick).padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = tokens.strong, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(detail, color = tokens.secondary, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
            }
            Text(percent, color = tokens.strong, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Row(Modifier.fillMaxWidth().padding(top = 9.dp), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            Box(Modifier.weight(progress).height(3.dp).background(color))
            Box(Modifier.weight(1f - progress).height(3.dp).background(tokens.outline))
        }
    }
    HorizontalDivider(color = tokens.outline)
}

@Composable
private fun ForecastRail(tokens: LedgerTokens) {
    Column(Modifier.fillMaxWidth().padding(top = 22.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Label("NOW", tokens, Modifier.weight(1f))
            Label("7 DAYS", tokens, Modifier.weight(1f))
            Label("30 DAYS", tokens, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(Modifier.weight(1f).height(7.dp).background(tokens.strong))
            Box(Modifier.weight(1f).height(7.dp).background(tokens.positive))
            Box(Modifier.weight(1f).height(7.dp).background(tokens.green))
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
            AmountLabel("€12,480", tokens, Modifier.weight(1f))
            AmountLabel("€15,213", tokens, Modifier.weight(1f))
            AmountLabel("€14,129", tokens, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryPair(leftLabel: String, leftValue: String, rightLabel: String, rightValue: String, tokens: LedgerTokens) {
    Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        SummaryMetric(leftLabel, leftValue, tokens, Modifier.weight(1f))
        SummaryMetric(rightLabel, rightValue, tokens, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, tokens: LedgerTokens, modifier: Modifier) {
    Column(modifier) {
        Label(label, tokens)
        Text(value, color = tokens.strong, fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
private fun DirectoryRow(tokens: LedgerTokens, onOpen: (FocusedDestination) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        DirectoryButton("ACCOUNTS\n2", tokens, Modifier.weight(1f)) { onOpen(accountsDestination()) }
        DirectoryButton("POCKETS\n6", tokens, Modifier.weight(1f)) { onOpen(pocketsDestination()) }
        DirectoryButton("PROFILES\n2", tokens, Modifier.weight(1f)) { onOpen(profilesDestination()) }
    }
}

@Composable
private fun DirectoryButton(label: String, tokens: LedgerTokens, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.heightIn(min = 58.dp).clickable(role = Role.Button, onClick = onClick),
        color = tokens.surface,
        shape = RoundedCornerShape(2.dp),
    ) {
        Box(Modifier.padding(10.dp), contentAlignment = Alignment.CenterStart) {
            Text(label, color = tokens.strong, fontSize = 9.sp, lineHeight = 14.sp, fontWeight = FontWeight.Black, letterSpacing = .7.sp)
        }
    }
}

@Composable
private fun StructureAccountHeader(
    name: String,
    institution: String,
    amount: String,
    color: Color,
    tokens: LedgerTokens,
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(top = 28.dp).heightIn(min = 62.dp).clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(width = 5.dp, height = 44.dp).background(color))
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(name, color = tokens.strong, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(institution, color = tokens.secondary, fontSize = 10.sp)
        }
        Text(amount, color = tokens.strong, fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
    HorizontalDivider(color = tokens.strong)
}

@Composable
private fun NestedRow(title: String, detail: String, amount: String, tokens: LedgerTokens, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 57.dp).clickable(role = Role.Button, onClick = onClick).padding(start = 18.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("└", color = tokens.outline, fontFamily = FontFamily.Monospace, fontSize = 13.sp, modifier = Modifier.padding(end = 9.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = tokens.strong, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = tokens.secondary, fontSize = 9.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Text(amount, color = tokens.strong, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
    HorizontalDivider(color = tokens.outline)
}

@Composable
private fun TextAction(label: String, tokens: LedgerTokens, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Text("$label →", color = tokens.interaction, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = .7.sp)
    }
}

private fun attentionSummary(attentionState: AttentionState) = when (attentionState) {
    AttentionState.NeedsReview -> "2 items · healthy plans hidden"
    AttentionState.Clear -> "Nothing needs attention"
}

@Composable
private fun clearAttentionState(tokens: LedgerTokens) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        color = tokens.surface,
        shape = RoundedCornerShape(2.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("CLEAR", color = tokens.positive, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text(
                "Contracts and saving goals are on track. Review all remains available.",
                color = tokens.secondary,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun ProfileSubheading(label: String, summary: String, tokens: LedgerTokens, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 45.dp).clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = tokens.secondary, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = .8.sp, modifier = Modifier.weight(1f))
        Text(summary, color = tokens.secondary, fontSize = 9.sp)
    }
}

@Composable
private fun FocusedDestinationPane(destination: FocusedDestination, tokens: LedgerTokens, onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 22.dp, top = 64.dp, end = 22.dp, bottom = 150.dp),
    ) {
        item {
            TextAction("BACK", tokens, onBack)
            Label(destination.eyebrow, tokens)
            Text(destination.title, color = tokens.strong, fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Text(destination.summary, color = tokens.secondary, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 12.dp))
            PrimaryAction(destination.primaryAction, tokens) {}
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 28.dp),
                color = tokens.surface,
                shape = RoundedCornerShape(2.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Label("STACKED SECONDARY SCREEN", tokens)
                    Text(
                        "This takeover is intentionally schematic. It confirms what the overview reveals and where the row leads; the focused workflow belongs to its own later decision ticket.",
                        color = tokens.secondary,
                        fontSize = 11.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Label(text: String, tokens: LedgerTokens, modifier: Modifier = Modifier) {
    Text(text, color = tokens.secondary, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.1.sp, modifier = modifier)
}

@Composable
private fun AmountLabel(text: String, tokens: LedgerTokens, modifier: Modifier = Modifier) {
    Text(text, color = tokens.strong, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = modifier)
}

private fun transactionDestination() = FocusedDestination(
    "TRANSACTION ENTRY",
    "Add transaction",
    "Create a one-off or recurring income, expense, or transfer. The editor's field sequence is deliberately deferred to the transaction-editor prototype.",
    "START ENTRY",
)

private fun upcomingDestination() = FocusedDestination(
    "UPCOMING ACTIVITY",
    "30-day timeline",
    "Review scheduled transaction entries across accounts, pockets, contracts, and financial profiles, then open the responsible entity or entry.",
    "FILTER TIMELINE",
)

private fun allTransactionsDestination() = FocusedDestination(
    "ALL-ACCOUNT TRANSACTIONS",
    "All transactions",
    "Review transaction history and upcoming activity across every account. Whether these form one combined timeline remains deliberately deferred.",
    "REVIEW ACTIVITY",
)

private fun accountsDestination() = FocusedDestination(
    "ACCOUNTS",
    "All accounts",
    "Compare account balances and institutions, create or archive accounts, and open each account's pocket structure and transaction feed.",
    "ADD ACCOUNT",
)

private fun accountDestination(name: String) = FocusedDestination(
    "ACCOUNT",
    name,
    "See current and projected balance, pockets, upcoming activity, and the account-scoped transaction feed before entering focused management.",
    "OPEN TRANSACTIONS",
)

private fun pocketsDestination() = FocusedDestination(
    "POCKETS & SAVING GOALS",
    "Allocation overview",
    "Review ordinary and managed pockets across accounts. Saving goals retain their target and progress while their dedicated pockets show where the money sits.",
    "ADD POCKET OR GOAL",
)

private fun pocketDestination(name: String) = FocusedDestination(
    "POCKET",
    name,
    "Review this pocket's current balance, upcoming activity, and transaction feed. Managed contract and saving-goal pockets redirect edits to their owner.",
    "OPEN TRANSACTIONS",
)

private fun goalDestination(name: String) = FocusedDestination(
    "SAVING GOAL",
    name,
    "Review target progress across its dedicated account pockets, planned contributions, and focused goal management.",
    "MANAGE GOAL",
)

private fun contractsDestination() = FocusedDestination(
    "CONTRACTS",
    "All commitments",
    "Review recurring commitments by financial profile, partner, next occurrence, and monthly impact without promoting Contracts to a feature pane.",
    "ADD CONTRACT",
)

private fun plansDestination() = FocusedDestination(
    "CONTRACTS & SAVING GOALS",
    "Review every plan",
    "Reach the complete contract and saving-goal inventories, including healthy plans omitted from the overview attention section.",
    "CHOOSE AN INVENTORY",
)

private fun contractDestination(name: String) = FocusedDestination(
    "CONTRACT",
    name,
    "Review terms, linked transaction entries, upcoming occurrences, dedicated pockets where applicable, and the contract-scoped transaction feed.",
    "MANAGE CONTRACT",
)

private fun profilesDestination() = FocusedDestination(
    "FINANCIAL PROFILES",
    "Personal and rental property",
    "Organize contracts, saving goals, and transaction entries into purposeful scopes; choose the default and handle archived or fallback profiles.",
    "MANAGE PROFILES",
)

private fun financesDirectoryDestination() = FocusedDestination(
    "FINANCES DIRECTORY",
    "Organize finances",
    "Reach the complete account, pocket, contract, saving-goal, partner, and financial-profile indexes when the overview summary is not enough.",
    "CHOOSE A COLLECTION",
)

@Composable
private fun StateStrip(
    modifier: Modifier,
    variant: HierarchyVariant,
    appearance: Appearance,
    largeText: Boolean,
    attentionState: AttentionState,
    focused: FocusedDestination?,
) {
    Surface(modifier = modifier.widthIn(max = 430.dp), color = Color(0xEC1B1D21), shape = RoundedCornerShape(9.dp)) {
        Text(
            "STATE · ${variant.key}/${variant.label} · ${appearance.name} · ${if (largeText) "200%" else "100%"} text · ${if (attentionState == AttentionState.NeedsReview) "2 attention" else "clear"} · ${focused?.title ?: "overview"}",
            color = Color(0xFFF5F5F3),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun PrototypeControls(
    modifier: Modifier,
    variant: HierarchyVariant,
    appearance: Appearance,
    largeText: Boolean,
    attentionState: AttentionState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onTheme: () -> Unit,
    onFontScale: () -> Unit,
    onAttentionState: () -> Unit,
) {
    Surface(modifier = modifier, color = Color(0xF21B1D21), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SettingButton(if (appearance == Appearance.Light) "LIGHT" else "DARK", "Toggle theme", onTheme)
                SettingButton(if (largeText) "TEXT 200" else "TEXT 100", "Toggle text scale", onFontScale)
                SettingButton(if (attentionState == AttentionState.NeedsReview) "ATTN 2" else "ATTN 0", "Toggle attention fixture", onAttentionState)
            }
            Row(Modifier.padding(top = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                SwitcherButton("←", "Previous hierarchy", onPrevious)
                Column(Modifier.width(250.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("THROWAWAY COMPOSE PROTOTYPE", color = Color(0xFFAEB2BA), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Text("${variant.key} — ${variant.label}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                SwitcherButton("→", "Next hierarchy", onNext)
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
        Box(Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
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
