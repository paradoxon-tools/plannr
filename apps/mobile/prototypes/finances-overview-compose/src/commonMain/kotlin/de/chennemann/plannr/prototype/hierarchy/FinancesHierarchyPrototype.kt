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

// PROTOTYPE — three Finances information hierarchies, switchable in one Compose host.
// The selected Ledger visual language and full-takeover secondary screens are fixed inputs.

private enum class HierarchyVariant(val key: String, val label: String, val thesis: String) {
    Position("A", "Position first", "What do I have, what changes soon, and why?"),
    Time("B", "Time first", "What happens next, and where does it land?"),
    Structure("C", "Structure first", "Where is everything organized, and what belongs together?"),
}

private enum class Appearance { Light, Dark }

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
    var variant by remember { mutableStateOf(HierarchyVariant.Position) }
    var appearance by remember { mutableStateOf(Appearance.Light) }
    var largeText by remember { mutableStateOf(false) }
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
                                HierarchyVariant.Position -> PositionFirstPane(tokens, onOpen = { focused = it })
                                HierarchyVariant.Time -> TimeFirstPane(tokens, onOpen = { focused = it })
                                HierarchyVariant.Structure -> StructureFirstPane(tokens, onOpen = { focused = it })
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
                focused = focused,
            )
            PrototypeControls(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                variant = variant,
                appearance = appearance,
                largeText = largeText,
                onPrevious = { cycleVariant(-1) },
                onNext = { cycleVariant(1) },
                onTheme = { appearance = appearance.other() },
                onFontScale = { largeText = !largeText },
            )
        }
    }
}

private fun Appearance.other() = if (this == Appearance.Light) Appearance.Dark else Appearance.Light

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
private fun PositionFirstPane(tokens: LedgerTokens, onOpen: (FocusedDestination) -> Unit) {
    LedgerList(HierarchyVariant.Position) {
        item { PrototypeHeading(HierarchyVariant.Position, tokens) }
        item {
            ProfileLine("Personal", "Default financial profile", tokens) {
                onOpen(profilesDestination())
            }
            HeroPosition(
                label = "NET POSITION",
                amount = "€ 12,480.20",
                detail = "€4,821.60 spendable · €7,658.60 reserved",
                tokens = tokens,
            )
            PrimaryAction("ADD TRANSACTION", tokens) { onOpen(transactionDestination()) }
        }
        item {
            SectionHeading("NEXT 30 DAYS", "+ €2,732.80 forecast", "ALL UPCOMING", tokens) {
                onOpen(upcomingDestination())
            }
            LedgerRow("Electricity", "Tomorrow · Home · Personal", "− €87.00", tokens, status = "PENDING") {
                onOpen(upcomingDestination())
            }
            LedgerRow("Rent", "1 Aug · Home · Personal", "− €1,120.00", tokens) {
                onOpen(contractDestination("Rent · apartment"))
            }
            LedgerRow("Salary", "2 Aug · Everyday · Personal", "+ €3,940.00", tokens) {
                onOpen(upcomingDestination())
            }
        }
        item {
            SectionHeading("ACCOUNTS", "2 · €12,480.20", "MANAGE", tokens) { onOpen(accountsDestination()) }
            LedgerRow("Everyday", "N26 · 4 pockets", "€ 4,821.60", tokens, marker = tokens.blue) {
                onOpen(accountDestination("Everyday"))
            }
            LedgerRow("Long term", "ING · 2 pockets", "€ 7,658.60", tokens, marker = tokens.violet) {
                onOpen(accountDestination("Long term"))
            }
        }
        item {
            SectionHeading("POCKETS & GOALS", "€6,304 allocated", "VIEW ALL", tokens) { onOpen(pocketsDestination()) }
            ProgressRow("Emergency reserve", "Saving goal · €6,840 of €10,000", "68%", .68f, tokens.green, tokens) {
                onOpen(goalDestination("Emergency reserve"))
            }
            ProgressRow("Japan", "Saving goal · €720 of €3,000", "24%", .24f, tokens.violet, tokens) {
                onOpen(goalDestination("Japan"))
            }
            LedgerRow("Bills", "Everyday · 4 linked entries", "€ 960.00", tokens, marker = tokens.blue) {
                onOpen(pocketDestination("Bills"))
            }
        }
        item {
            SectionHeading("CONTRACTS", "3 active · €1,287/mo", "MANAGE", tokens) { onOpen(contractsDestination()) }
            LedgerRow("Rent · apartment", "Home · due monthly", "€ 1,120.00", tokens, marker = tokens.green) {
                onOpen(contractDestination("Rent · apartment"))
            }
            LedgerRow("MagentaMobil", "Connectivity · through Jun 2028", "€ 49.95", tokens) {
                onOpen(contractDestination("MagentaMobil"))
            }
        }
        item {
            SectionHeading("FINANCIAL PROFILES", "2 · Personal default", "MANAGE", tokens) { onOpen(profilesDestination()) }
            LedgerRow("Personal", "Default · 7 entries · 2 goals", "", tokens, marker = tokens.blue) { onOpen(profilesDestination()) }
            LedgerRow("Rental property", "1 contract · no upcoming gaps", "", tokens, marker = tokens.violet) { onOpen(profilesDestination()) }
        }
    }
}

@Composable
private fun TimeFirstPane(tokens: LedgerTokens, onOpen: (FocusedDestination) -> Unit) {
    LedgerList(HierarchyVariant.Time) {
        item { PrototypeHeading(HierarchyVariant.Time, tokens) }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Label("CURRENT POSITION", tokens)
                    Text("€ 12,480.20", color = tokens.strong, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold)
                }
                CompactAction("+ ADD", tokens) { onOpen(transactionDestination()) }
            }
            ForecastRail(tokens)
        }
        item {
            SectionHeading("TODAY", "€12,480.20 after activity", "HISTORY", tokens) { onOpen(upcomingDestination()) }
            LedgerRow("Weekly groceries", "Booked · Everyday / Groceries", "− €64.20", tokens, marker = tokens.blue) {
                onOpen(pocketDestination("Groceries"))
            }
        }
        item {
            SectionHeading("NEXT 7 DAYS", "+ €2,733 net", "OPEN TIMELINE", tokens) { onOpen(upcomingDestination()) }
            LedgerRow("Tomorrow · Electricity", "Home contract · Personal", "− €87.00", tokens, status = "PENDING") {
                onOpen(contractDestination("Electricity"))
            }
            LedgerRow("1 Aug · Rent", "Home contract · Personal", "− €1,120.00", tokens) {
                onOpen(contractDestination("Rent · apartment"))
            }
            LedgerRow("2 Aug · Salary", "Everyday account · Personal", "+ €3,940.00", tokens) {
                onOpen(accountDestination("Everyday"))
            }
        }
        item {
            SectionHeading("REST OF 30 DAYS", "6 scheduled · − €1,084", "ALL UPCOMING", tokens) { onOpen(upcomingDestination()) }
            LedgerRow("12 Aug · Insurances", "3 contracts grouped", "− €214.10", tokens) { onOpen(contractsDestination()) }
            LedgerRow("20 Aug · Japan", "Goal contribution", "− €300.00", tokens, marker = tokens.violet) {
                onOpen(goalDestination("Japan"))
            }
            LedgerRow("28 Aug · Subscriptions", "4 contracts grouped", "− €82.45", tokens) { onOpen(contractsDestination()) }
        }
        item {
            SectionHeading("WHERE IT LANDS", "30-day forecast", "ACCOUNTS", tokens) { onOpen(accountsDestination()) }
            LedgerRow("Everyday", "4 pockets · forecast €6,470.25", "+ €1,648.65", tokens, marker = tokens.blue) {
                onOpen(accountDestination("Everyday"))
            }
            LedgerRow("Long term", "2 pockets · forecast €8,742.75", "+ €1,084.15", tokens, marker = tokens.violet) {
                onOpen(accountDestination("Long term"))
            }
        }
        item {
            SectionHeading("PLANS & COMMITMENTS", "2 goals · 3 contracts", "REVIEW", tokens) { onOpen(contractsDestination()) }
            SummaryPair("SAVING GOALS", "€7,560 / €13,000", "CONTRACTS", "€1,287 / month", tokens)
        }
        item {
            SectionHeading("ORGANIZE", "Accounts · pockets · profiles", "OPEN", tokens) { onOpen(financesDirectoryDestination()) }
            DirectoryRow(tokens, onOpen)
        }
    }
}

@Composable
private fun StructureFirstPane(tokens: LedgerTokens, onOpen: (FocusedDestination) -> Unit) {
    LedgerList(HierarchyVariant.Structure) {
        item { PrototypeHeading(HierarchyVariant.Structure, tokens) }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Label("ALL ACCOUNTS", tokens)
                    Text("€ 12,480.20", color = tokens.strong, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
                    Text("2 accounts · 6 pockets", color = tokens.secondary, fontSize = 11.sp)
                }
                CompactAction("+ ADD", tokens) { onOpen(transactionDestination()) }
            }
        }
        item {
            StructureAccountHeader("Everyday", "N26", "€ 4,821.60", tokens.blue, tokens) { onOpen(accountDestination("Everyday")) }
            NestedRow("Default", "Free to spend", "€ 3,210.40", tokens) { onOpen(pocketDestination("Default")) }
            NestedRow("Bills", "4 linked entries", "€ 960.00", tokens) { onOpen(pocketDestination("Bills")) }
            NestedRow("Groceries", "€435 left this month", "€ 651.20", tokens) { onOpen(pocketDestination("Groceries")) }
            TextAction("MANAGE ACCOUNT & POCKETS", tokens) { onOpen(accountDestination("Everyday")) }
        }
        item {
            StructureAccountHeader("Long term", "ING", "€ 7,658.60", tokens.violet, tokens) { onOpen(accountDestination("Long term")) }
            NestedRow("Emergency reserve", "Goal pocket · 68% funded", "€ 6,840.00", tokens) {
                onOpen(goalDestination("Emergency reserve"))
            }
            NestedRow("Japan", "Goal pocket · 24% funded", "€ 720.00", tokens) { onOpen(goalDestination("Japan")) }
            NestedRow("Unallocated", "Free to assign", "€ 98.60", tokens) { onOpen(pocketDestination("Unallocated")) }
            TextAction("MANAGE ACCOUNT & POCKETS", tokens) { onOpen(accountDestination("Long term")) }
        }
        item {
            SectionHeading("COMMITMENTS", "3 contracts · €1,287/mo", "MANAGE", tokens) { onOpen(contractsDestination()) }
            ProfileSubheading("PERSONAL · DEFAULT", "2 contracts · 2 goals", tokens) { onOpen(profilesDestination()) }
            LedgerRow("Rent · apartment", "Home · next 1 Aug", "€ 1,120.00", tokens, marker = tokens.green) {
                onOpen(contractDestination("Rent · apartment"))
            }
            LedgerRow("MagentaMobil", "Connectivity · next 5 Aug", "€ 49.95", tokens) {
                onOpen(contractDestination("MagentaMobil"))
            }
            ProfileSubheading("RENTAL PROPERTY", "1 contract", tokens) { onOpen(profilesDestination()) }
            LedgerRow("Building insurance", "Annual · next 12 Aug", "€ 117.05", tokens, marker = tokens.violet) {
                onOpen(contractDestination("Building insurance"))
            }
        }
        item {
            SectionHeading("SAVING GOALS", "€7,560 of €13,000", "MANAGE", tokens) { onOpen(pocketsDestination()) }
            ProgressRow("Emergency reserve", "Long term · target Dec 2026", "68%", .68f, tokens.green, tokens) {
                onOpen(goalDestination("Emergency reserve"))
            }
            ProgressRow("Japan", "Long term · target Apr 2027", "24%", .24f, tokens.violet, tokens) {
                onOpen(goalDestination("Japan"))
            }
        }
        item {
            SectionHeading("UPCOMING", "3 in next 7 days", "VIEW TIMELINE", tokens) { onOpen(upcomingDestination()) }
            LedgerRow("Electricity", "Tomorrow · Personal", "− €87.00", tokens, status = "PENDING") { onOpen(upcomingDestination()) }
            LedgerRow("Rent", "1 Aug · Personal", "− €1,120.00", tokens) { onOpen(upcomingDestination()) }
            LedgerRow("Salary", "2 Aug · Personal", "+ €3,940.00", tokens) { onOpen(upcomingDestination()) }
        }
        item {
            SectionHeading("FINANCIAL PROFILES", "2 · Personal default", "MANAGE", tokens) { onOpen(profilesDestination()) }
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
    focused: FocusedDestination?,
) {
    Surface(modifier = modifier.widthIn(max = 430.dp), color = Color(0xEC1B1D21), shape = RoundedCornerShape(9.dp)) {
        Text(
            "STATE · ${variant.key}/${variant.label} · ${appearance.name} · ${if (largeText) "200%" else "100%"} text · ${focused?.title ?: "overview"}",
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
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onTheme: () -> Unit,
    onFontScale: () -> Unit,
) {
    Surface(modifier = modifier, color = Color(0xF21B1D21), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SettingButton(if (appearance == Appearance.Light) "LIGHT" else "DARK", "Toggle theme", onTheme)
                SettingButton(if (largeText) "TEXT 200" else "TEXT 100", "Toggle text scale", onFontScale)
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
