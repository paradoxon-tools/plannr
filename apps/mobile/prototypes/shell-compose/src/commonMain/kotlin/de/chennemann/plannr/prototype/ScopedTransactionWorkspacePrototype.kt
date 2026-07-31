package de.chennemann.plannr.prototype

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

// PROTOTYPE — three scoped overview-to-transaction workspace transitions, switchable in-app.

private val WorkspacePaper = Color(0xFFF3F2EE)
private val WorkspaceRaised = Color(0xFFFFFFFF)
private val WorkspaceInk = Color(0xFF181A1E)
private val WorkspaceMuted = Color(0xFF73767C)
private val WorkspaceHairline = Color(0xFFD7D6D1)
private val WorkspacePositive = Color(0xFF276340)
private val WorkspaceNegative = Color(0xFF963D35)
private val WorkspaceSignal = Color(0xFF305FA8)

private enum class WorkspaceFeature(val label: String, val glyph: String) {
    Dashboard("Dashboard", "D"),
    Chat("Chat", "C"),
    Groceries("Groceries", "G"),
    Finances("Finances", "F"),
}

private enum class WorkspaceVariant(val key: String, val label: String) {
    Boundary("A", "Boundary handoff"),
    Gateway("B", "In-content gateway"),
    Dock("C", "Persistent dock"),
}

private enum class WorkspacePane {
    Main,
    Transactions,
}

private enum class TransactionScope(
    val title: String,
    val kind: String,
    val serverScope: String,
) {
    AllFinances("All Finances", "FINANCES OVERVIEW", "scope=finances"),
    EverydayAccount("Everyday", "ACCOUNT", "accountId=acc-everyday"),
    AvailablePocket("Available", "POCKET", "pocketId=pocket-available"),
    RentContract("Rent", "CONTRACT", "contractId=contract-rent"),
    ;

    val isDetail: Boolean get() = this != AllFinances
}

@Composable
fun ScopedTransactionWorkspacePrototypeApp() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            background = WorkspacePaper,
            surface = WorkspaceRaised,
            onBackground = WorkspaceInk,
            onSurface = WorkspaceInk,
            primary = WorkspaceInk,
            onPrimary = WorkspaceRaised,
            outline = WorkspaceHairline,
        ),
    ) {
        var variantName by rememberSaveable { mutableStateOf(WorkspaceVariant.Boundary.name) }
        var paneName by rememberSaveable { mutableStateOf(WorkspacePane.Main.name) }
        var scopeName by rememberSaveable { mutableStateOf(TransactionScope.AllFinances.name) }
        val variant = WorkspaceVariant.valueOf(variantName)
        val pane = WorkspacePane.valueOf(paneName)
        val transactionScope = TransactionScope.valueOf(scopeName)
        val rootPagerState = rememberPagerState(initialPage = WorkspaceFeature.Finances.ordinal) {
            WorkspaceFeature.entries.size
        }
        val verticalPagerState = rememberPagerState(initialPage = WorkspacePane.Main.ordinal) {
            WorkspacePane.entries.size
        }
        val coroutineScope = rememberCoroutineScope()
        val focusRequester = remember { FocusRequester() }

        val boundaryWorkspaceOwnsGestures = variant == WorkspaceVariant.Boundary && (
            verticalPagerState.currentPage != WorkspacePane.Main.ordinal ||
                verticalPagerState.targetPage != WorkspacePane.Main.ordinal ||
                verticalPagerState.currentPageOffsetFraction.absoluteValue > 0.001f
            )
        val explicitWorkspaceOwnsGestures = variant != WorkspaceVariant.Boundary && pane == WorkspacePane.Transactions
        val workspaceOwnsGestures = boundaryWorkspaceOwnsGestures || explicitWorkspaceOwnsGestures
        val rootPagerEnabled = !transactionScope.isDetail && !workspaceOwnsGestures
        val hideFeatureNavigation = transactionScope.isDetail ||
            (variant == WorkspaceVariant.Gateway && pane == WorkspacePane.Transactions)

        fun setPane(nextPane: WorkspacePane) {
            paneName = nextPane.name
            if (variant == WorkspaceVariant.Boundary) {
                coroutineScope.launch { verticalPagerState.animateScrollToPage(nextPane.ordinal) }
            }
        }

        fun closeOneLayer(): Boolean {
            return when {
                workspaceOwnsGestures || pane == WorkspacePane.Transactions -> {
                    setPane(WorkspacePane.Main)
                    true
                }

                transactionScope.isDetail -> {
                    scopeName = TransactionScope.AllFinances.name
                    true
                }

                else -> false
            }
        }

        fun changeVariant(delta: Int) {
            val variants = WorkspaceVariant.entries
            val next = (variant.ordinal + delta + variants.size) % variants.size
            variantName = variants[next].name
            paneName = WorkspacePane.Main.name
        }

        LaunchedEffect(Unit) {
            rootPagerState.scrollToPage(WorkspaceFeature.Finances.ordinal)
            focusRequester.requestFocus()
        }

        LaunchedEffect(variant) {
            verticalPagerState.scrollToPage(WorkspacePane.Main.ordinal)
        }

        LaunchedEffect(verticalPagerState) {
            snapshotFlow { verticalPagerState.settledPage }.collect { page ->
                if (variant == WorkspaceVariant.Boundary) {
                    paneName = WorkspacePane.entries[page].name
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WorkspacePaper)
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

                        Key.Escape -> closeOneLayer()
                        else -> false
                    }
                },
        ) {
            WorkspaceRootShell(
                modifier = Modifier.fillMaxSize().padding(top = 80.dp, bottom = 70.dp),
                rootPagerState = rootPagerState,
                rootPagerEnabled = rootPagerEnabled,
                showFeatureNavigation = !hideFeatureNavigation,
                variant = variant,
                pane = pane,
                transactionScope = transactionScope,
                verticalPagerState = verticalPagerState,
                onOpenTransactions = { setPane(WorkspacePane.Transactions) },
                onCloseTransactions = { setPane(WorkspacePane.Main) },
                onSelectScope = {
                    scopeName = it.name
                    paneName = WorkspacePane.Main.name
                    coroutineScope.launch { verticalPagerState.scrollToPage(WorkspacePane.Main.ordinal) }
                },
                onCloseDetail = { scopeName = TransactionScope.AllFinances.name },
                onSelectFeature = { page ->
                    coroutineScope.launch { rootPagerState.animateScrollToPage(page) }
                },
            )

            WorkspaceStateReadout(
                modifier = Modifier.align(Alignment.TopStart).padding(start = 12.dp, top = 8.dp),
                variant = variant,
                pane = pane,
                transactionScope = transactionScope,
                rootPagerEnabled = rootPagerEnabled,
                workspaceOwnsGestures = workspaceOwnsGestures,
            )

            WorkspacePrototypeSwitcher(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 9.dp),
                variant = variant,
                onPrevious = { changeVariant(-1) },
                onNext = { changeVariant(1) },
            )
        }
    }
}

@Composable
private fun WorkspaceRootShell(
    modifier: Modifier,
    rootPagerState: PagerState,
    rootPagerEnabled: Boolean,
    showFeatureNavigation: Boolean,
    variant: WorkspaceVariant,
    pane: WorkspacePane,
    transactionScope: TransactionScope,
    verticalPagerState: PagerState,
    onOpenTransactions: () -> Unit,
    onCloseTransactions: () -> Unit,
    onSelectScope: (TransactionScope) -> Unit,
    onCloseDetail: () -> Unit,
    onSelectFeature: (Int) -> Unit,
) {
    Column(modifier) {
        HorizontalPager(
            state = rootPagerState,
            key = { WorkspaceFeature.entries[it].name },
            userScrollEnabled = rootPagerEnabled,
            beyondViewportPageCount = 0,
            modifier = Modifier.weight(1f),
        ) { page ->
            val feature = WorkspaceFeature.entries[page]
            if (feature == WorkspaceFeature.Finances) {
                WorkspaceHost(
                    variant = variant,
                    pane = pane,
                    transactionScope = transactionScope,
                    verticalPagerState = verticalPagerState,
                    onOpenTransactions = onOpenTransactions,
                    onCloseTransactions = onCloseTransactions,
                    onSelectScope = onSelectScope,
                    onCloseDetail = onCloseDetail,
                )
            } else {
                WorkspaceFeaturePlaceholder(feature)
            }
        }

        if (showFeatureNavigation) {
            WorkspaceFeatureNavigation(
                pagerState = rootPagerState,
                enabled = rootPagerEnabled,
                onSelect = onSelectFeature,
            )
        }
    }
}

@Composable
private fun WorkspaceHost(
    variant: WorkspaceVariant,
    pane: WorkspacePane,
    transactionScope: TransactionScope,
    verticalPagerState: PagerState,
    onOpenTransactions: () -> Unit,
    onCloseTransactions: () -> Unit,
    onSelectScope: (TransactionScope) -> Unit,
    onCloseDetail: () -> Unit,
) {
    when (variant) {
        WorkspaceVariant.Boundary -> {
            VerticalPager(
                state = verticalPagerState,
                key = { WorkspacePane.entries[it].name },
                beyondViewportPageCount = 1,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (WorkspacePane.entries[page]) {
                    WorkspacePane.Main -> WorkspaceMainPane(
                        transactionScope = transactionScope,
                        gatewayPlacement = GatewayPlacement.Boundary,
                        onOpenTransactions = onOpenTransactions,
                        onSelectScope = onSelectScope,
                        onCloseDetail = onCloseDetail,
                    )

                    WorkspacePane.Transactions -> TransactionFeedPane(
                        transactionScope = transactionScope,
                        transitionHint = "Swipe down at the top, or use Back, to return",
                        onBack = onCloseTransactions,
                    )
                }
            }
        }

        WorkspaceVariant.Gateway -> {
            AnimatedContent(
                targetState = pane,
                transitionSpec = { verticalReplacement(targetState) },
                label = "Scoped transaction gateway",
                modifier = Modifier.fillMaxSize(),
            ) { targetPane ->
                when (targetPane) {
                    WorkspacePane.Main -> WorkspaceMainPane(
                        transactionScope = transactionScope,
                        gatewayPlacement = GatewayPlacement.InContent,
                        onOpenTransactions = onOpenTransactions,
                        onSelectScope = onSelectScope,
                        onCloseDetail = onCloseDetail,
                    )

                    WorkspacePane.Transactions -> TransactionFeedPane(
                        transactionScope = transactionScope,
                        transitionHint = "Back is the only pane transition; list scrolling never navigates",
                        onBack = onCloseTransactions,
                    )
                }
            }
        }

        WorkspaceVariant.Dock -> {
            Box(Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = pane,
                    transitionSpec = { verticalReplacement(targetState) },
                    label = "Persistent transaction dock",
                    modifier = Modifier.fillMaxSize(),
                ) { targetPane ->
                    when (targetPane) {
                        WorkspacePane.Main -> WorkspaceMainPane(
                            transactionScope = transactionScope,
                            gatewayPlacement = GatewayPlacement.None,
                            onOpenTransactions = onOpenTransactions,
                            onSelectScope = onSelectScope,
                            onCloseDetail = onCloseDetail,
                            bottomPadding = 108,
                        )

                        WorkspacePane.Transactions -> TransactionFeedPane(
                            transactionScope = transactionScope,
                            transitionHint = "The dock captured scope before opening this workspace",
                            onBack = onCloseTransactions,
                        )
                    }
                }

                if (pane == WorkspacePane.Main) {
                    PersistentTransactionDock(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 12.dp, vertical = 10.dp),
                        transactionScope = transactionScope,
                        onOpen = onOpenTransactions,
                    )
                }
            }
        }
    }
}

private fun androidx.compose.animation.AnimatedContentTransitionScope<WorkspacePane>.verticalReplacement(
    target: WorkspacePane,
): ContentTransform = if (target == WorkspacePane.Transactions) {
    slideInVertically { it } togetherWith slideOutVertically { -it / 4 }
} else {
    slideInVertically { -it / 4 } togetherWith slideOutVertically { it }
}

private enum class GatewayPlacement {
    Boundary,
    InContent,
    None,
}

@Composable
private fun WorkspaceMainPane(
    transactionScope: TransactionScope,
    gatewayPlacement: GatewayPlacement,
    onOpenTransactions: () -> Unit,
    onSelectScope: (TransactionScope) -> Unit,
    onCloseDetail: () -> Unit,
    bottomPadding: Int = 28,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, top = 22.dp, end = 18.dp, bottom = bottomPadding.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "scope-header-${transactionScope.name}") {
            if (transactionScope.isDetail) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = onCloseDetail, contentPadding = PaddingValues(horizontal = 12.dp)) {
                        Text("← Finances", fontSize = 11.sp)
                    }
                    Column(Modifier.padding(start = 12.dp)) {
                        WorkspaceOverline(transactionScope.kind)
                        Text(transactionScope.title, fontSize = 25.sp, fontWeight = FontWeight.Black)
                    }
                }
            } else {
                Column {
                    WorkspaceOverline("FINANCES · FIXTURE CONTENT")
                    Text("Your position", fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Text("€ 12,480.20", fontSize = 18.sp, color = WorkspaceMuted, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (gatewayPlacement == GatewayPlacement.InContent) {
            item(key = "inline-gateway-${transactionScope.name}") {
                InContentTransactionGateway(transactionScope, onOpenTransactions)
            }
        }

        item(key = "scope-proof-${transactionScope.name}") {
            ScopeProofCard(transactionScope)
        }

        item(key = "metrics-${transactionScope.name}") {
            ScopeSummaryMetrics(transactionScope)
        }

        if (transactionScope == TransactionScope.AllFinances) {
            item(key = "scope-title") { WorkspaceSectionTitle("Open a scoped detail fixture") }
            item(key = "account") {
                ScopeDestinationRow(
                    title = "Everyday",
                    meta = "Account · € 4,821.60",
                    onClick = { onSelectScope(TransactionScope.EverydayAccount) },
                )
            }
            item(key = "pocket") {
                ScopeDestinationRow(
                    title = "Available",
                    meta = "Pocket · € 1,840.20",
                    onClick = { onSelectScope(TransactionScope.AvailablePocket) },
                )
            }
            item(key = "contract") {
                ScopeDestinationRow(
                    title = "Rent",
                    meta = "Contract · funded 1 month ahead",
                    onClick = { onSelectScope(TransactionScope.RentContract) },
                )
            }
        } else {
            item(key = "detail-fact-1") {
                FixtureBlock("Current balance", when (transactionScope) {
                    TransactionScope.EverydayAccount -> "€ 4,821.60 across 4 pockets"
                    TransactionScope.AvailablePocket -> "€ 1,840.20 unallocated"
                    TransactionScope.RentContract -> "€ 980 allocated · 1 month ahead"
                    TransactionScope.AllFinances -> "€ 12,480.20"
                })
            }
            item(key = "detail-fact-2") {
                FixtureBlock("Upcoming summary", "− € 244.40 · 4 entries in the next 30 days")
            }
        }

        item(key = "activity-title") { WorkspaceSectionTitle("Recent activity fixture") }
        items(
            items = listOf(
                Triple("Weekly groceries", "Today", "− € 64.20"),
                Triple("Coffee", "Today", "− € 4.60"),
                Triple("Salary", "Yesterday", "+ € 3,240"),
            ),
            key = { it.first },
        ) { entry ->
            WorkspaceEntityRow(entry.first, entry.second, entry.third)
        }

        item(key = "scroll-test-${transactionScope.name}") {
            Surface(color = Color(0xFFE7E6E1), shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("SCROLL-OWNERSHIP FIXTURE", fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Text(
                        when (gatewayPlacement) {
                            GatewayPlacement.Boundary -> "Continue upward only after this scoped surface reaches its lower boundary. The next gesture may hand off to Transactions."
                            GatewayPlacement.InContent -> "This surface never turns list overscroll into navigation. Only the Transactions gateway changes panes."
                            GatewayPlacement.None -> "The dock remains reachable without coupling navigation to this list's scroll position."
                        },
                        color = WorkspaceMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                }
            }
        }

        if (gatewayPlacement == GatewayPlacement.Boundary) {
            item(key = "boundary-gateway-${transactionScope.name}") {
                BoundaryTransactionGateway(transactionScope, onOpenTransactions)
            }
        }
    }
}

@Composable
private fun ScopeProofCard(transactionScope: TransactionScope) {
    Card(colors = CardDefaults.cardColors(containerColor = WorkspaceRaised), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            WorkspaceOverline("ACTIVE SERVER SCOPE")
            Text(transactionScope.title, fontSize = 17.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 3.dp))
            Text(
                "GET /transactions?${transactionScope.serverScope}",
                color = WorkspaceSignal,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun InContentTransactionGateway(transactionScope: TransactionScope, onOpen: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onOpen),
        color = WorkspaceInk,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(Modifier.padding(horizontal = 17.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("TRANSACTIONS", color = WorkspaceRaised, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.1.sp)
                Text(transactionScope.title, color = Color(0xFFBFC2C8), fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
            }
            Text("OPEN ↓", color = WorkspaceRaised, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun BoundaryTransactionGateway(transactionScope: TransactionScope, onOpen: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text("END OF ${transactionScope.kind}", color = WorkspaceMuted, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.1.sp)
        Text("Swipe up past this boundary for scoped transactions", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp))
        OutlinedButton(onClick = onOpen, modifier = Modifier.padding(top = 8.dp)) {
            Text("OPEN TRANSACTIONS ↓", fontSize = 10.sp)
        }
    }
}

@Composable
private fun PersistentTransactionDock(
    modifier: Modifier,
    transactionScope: TransactionScope,
    onOpen: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onOpen),
        color = WorkspaceInk,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 12.dp,
    ) {
        Row(Modifier.padding(horizontal = 17.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF33363B)), contentAlignment = Alignment.Center) {
                Text("↓", color = WorkspaceRaised, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
            Column(Modifier.weight(1f).padding(horizontal = 11.dp)) {
                Text("TRANSACTION WORKSPACE", color = WorkspaceRaised, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text(transactionScope.title, color = Color(0xFFC9CBD0), fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
            }
            Text("OPEN", color = WorkspaceRaised, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun TransactionFeedPane(
    transactionScope: TransactionScope,
    transitionHint: String,
    onBack: () -> Unit,
) {
    val feedEntries = remember(transactionScope) {
        when (transactionScope) {
            TransactionScope.AllFinances -> listOf(
                Triple("Weekly groceries", "Everyday · Today", "− € 64.20"),
                Triple("Coffee", "Available · Today", "− € 4.60"),
                Triple("Salary", "Everyday · Yesterday", "+ € 3,240"),
                Triple("Rent", "Everyday · 2 days ago", "− € 980"),
                Triple("Emergency fund", "Long term · 5 days ago", "− € 250"),
                Triple("Interest", "Long term · 8 days ago", "+ € 12.40"),
            )

            TransactionScope.EverydayAccount -> listOf(
                Triple("Weekly groceries", "Available · Today", "− € 64.20"),
                Triple("Coffee", "Available · Today", "− € 4.60"),
                Triple("Salary", "Available · Yesterday", "+ € 3,240"),
                Triple("Rent", "Rent pocket · 2 days ago", "− € 980"),
            )

            TransactionScope.AvailablePocket -> listOf(
                Triple("Weekly groceries", "Today", "− € 64.20"),
                Triple("Coffee", "Today", "− € 4.60"),
                Triple("Salary", "Yesterday", "+ € 3,240"),
            )

            TransactionScope.RentContract -> listOf(
                Triple("July rent", "2 days ago", "− € 980"),
                Triple("June rent", "1 month ago", "− € 980"),
                Triple("May rent", "2 months ago", "− € 980"),
            )
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = WorkspacePaper) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            item(key = "feed-header") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = onBack, contentPadding = PaddingValues(horizontal = 12.dp)) {
                        Text("↑ Back", fontSize = 11.sp)
                    }
                    Column(Modifier.padding(start = 12.dp)) {
                        WorkspaceOverline("SCOPED TRANSACTION FEED")
                        Text(transactionScope.title, fontSize = 23.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
            item(key = "scope-proof") {
                Surface(color = Color(0xFFE5ECF7), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.fillMaxWidth().padding(13.dp)) {
                        Text("SCOPE CAPTURED ON ENTRY", color = WorkspaceSignal, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Text(
                            "GET /transactions?${transactionScope.serverScope}",
                            color = WorkspaceInk,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Text(transitionHint, color = WorkspaceMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp))
                    }
                }
            }
            items(feedEntries, key = { it.first }) { entry ->
                WorkspaceEntityRow(entry.first, entry.second, entry.third)
            }
            item(key = "feed-end") {
                Text(
                    "FIXTURE FEED · chronology and final row content are decided elsewhere",
                    color = WorkspaceMuted,
                    fontSize = 9.sp,
                    lineHeight = 14.sp,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun ScopeDestinationRow(title: String, meta: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = WorkspaceRaised),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Color(0xFFE9E8E3), shape = RoundedCornerShape(10.dp)) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    Text(title.take(1), fontWeight = FontWeight.Black)
                }
            }
            Column(Modifier.weight(1f).padding(horizontal = 11.dp)) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(meta, color = WorkspaceMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
            }
            Text("OPEN →", fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun ScopeSummaryMetric(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = 6.dp)) {
        Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun FixtureBlock(label: String, value: String) {
    Card(colors = CardDefaults.cardColors(containerColor = WorkspaceRaised), shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                WorkspaceOverline(label.uppercase())
                Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp))
            }
        }
    }
}

@Composable
private fun WorkspaceEntityRow(label: String, meta: String, value: String) {
    Card(colors = CardDefaults.cardColors(containerColor = WorkspaceRaised), shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Color(0xFFECEBE7), shape = RoundedCornerShape(10.dp)) {
                Box(Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                    Text(label.take(1), fontWeight = FontWeight.Black)
                }
            }
            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(meta, color = WorkspaceMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
            }
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun ScopeSummaryMetrics(transactionScope: TransactionScope) {
    Card(colors = CardDefaults.cardColors(containerColor = WorkspaceRaised), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(15.dp)) {
            ScopeSummaryMetric("BALANCE", if (transactionScope == TransactionScope.RentContract) "€ 980" else "€ 4,821", WorkspacePositive, Modifier.weight(1f))
            ScopeSummaryMetric("UPCOMING", "− € 244", WorkspaceNegative, Modifier.weight(1f))
        }
    }
}

@Composable
private fun WorkspaceFeaturePlaceholder(feature: WorkspaceFeature) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(30.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("0${feature.ordinal + 1}", color = WorkspaceHairline, fontSize = 78.sp, fontWeight = FontWeight.Light)
        Spacer(Modifier.height(14.dp))
        Text(feature.label, fontSize = 34.sp, fontWeight = FontWeight.Black)
        Text(
            "A placeholder feature pane used only to test when the authoritative root pager is available.",
            color = WorkspaceMuted,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 7.dp).widthIn(max = 310.dp),
        )
    }
}

@Composable
private fun WorkspaceFeatureNavigation(
    pagerState: PagerState,
    enabled: Boolean,
    onSelect: (Int) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(WorkspacePaper)
            .semantics {
                stateDescription = if (enabled) "Feature navigation available" else "Feature navigation locked by transaction workspace"
            },
    ) {
        val iconWidth = 44.dp
        val horizontalPadding = 10.dp
        val labelSpace = (maxWidth - horizontalPadding * 2 - iconWidth * WorkspaceFeature.entries.size).coerceAtLeast(0.dp)

        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = horizontalPadding, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            WorkspaceFeature.entries.forEachIndexed { index, feature ->
                val distance = (pagerState.currentPage - index + pagerState.currentPageOffsetFraction).absoluteValue
                val emphasis = (1f - distance).coerceIn(0f, 1f)
                val foreground = lerp(WorkspaceMuted, WorkspaceInk, emphasis)

                Row(
                    modifier = Modifier
                        .height(46.dp)
                        .clip(RoundedCornerShape(23.dp))
                        .clickable(
                            enabled = enabled && index != pagerState.settledPage,
                            role = Role.Button,
                            onClickLabel = "Open ${feature.label}",
                        ) { onSelect(index) }
                        .alpha(if (enabled) 1f else 0.35f)
                        .semantics {
                            contentDescription = feature.label
                            stateDescription = when {
                                index == pagerState.settledPage -> "Selected"
                                enabled -> "Not selected"
                                else -> "Unavailable while scoped work owns gestures"
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(iconWidth), contentAlignment = Alignment.Center) {
                        Surface(
                            color = if (emphasis > 0.5f) WorkspaceRaised else Color.Transparent,
                            shape = CircleShape,
                        ) {
                            Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                                Text(feature.glyph, color = foreground, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    Box(Modifier.width(labelSpace * emphasis).height(44.dp), contentAlignment = Alignment.CenterStart) {
                        Text(
                            feature.label,
                            color = foreground,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp,
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
private fun WorkspaceStateReadout(
    modifier: Modifier,
    variant: WorkspaceVariant,
    pane: WorkspacePane,
    transactionScope: TransactionScope,
    rootPagerEnabled: Boolean,
    workspaceOwnsGestures: Boolean,
) {
    Surface(modifier = modifier.width(400.dp), color = WorkspaceRaised, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, WorkspaceHairline)) {
        Column(Modifier.padding(horizontal = 11.dp, vertical = 8.dp)) {
            Text(
                "${variant.key} · ${variant.label.uppercase()} · ${transactionScope.kind} / ${pane.name.uppercase()}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.7.sp,
            )
            Text(
                "root pager: ${if (rootPagerEnabled) "AVAILABLE" else "LOCKED"}  ·  gesture owner: ${if (workspaceOwnsGestures) "TRANSACTIONS" else if (transactionScope.isDetail) "DETAIL" else "ROOT / CONTENT"}",
                color = if (rootPagerEnabled) WorkspacePositive else WorkspaceNegative,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 3.dp),
            )
            Text("server scope: ${transactionScope.serverScope}", color = WorkspaceSignal, fontSize = 9.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun WorkspacePrototypeSwitcher(
    modifier: Modifier,
    variant: WorkspaceVariant,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Surface(modifier = modifier, color = WorkspaceInk, shape = RoundedCornerShape(28.dp), shadowElevation = 16.dp) {
        Row(Modifier.padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onPrevious,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33363B)),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(40.dp),
            ) { Text("←", fontSize = 16.sp) }
            Text(
                "${variant.key} · ${variant.label}",
                color = WorkspaceRaised,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 13.dp).widthIn(min = 132.dp),
            )
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33363B)),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(40.dp),
            ) { Text("→", fontSize = 16.sp) }
        }
    }
}

@Composable
private fun WorkspaceOverline(text: String) {
    Text(text, color = WorkspaceMuted, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
}

@Composable
private fun WorkspaceSectionTitle(text: String) {
    Text(text, fontSize = 13.sp, fontWeight = FontWeight.Black)
}
