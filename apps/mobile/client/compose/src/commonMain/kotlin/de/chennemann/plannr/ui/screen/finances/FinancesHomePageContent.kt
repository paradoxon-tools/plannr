package de.chennemann.plannr.ui.screen.finances

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import de.chennemann.plannr.data.Pocket
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.chennemann.plannr.data.Partner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalDensity
import de.chennemann.plannr.ui.screen.finances.partners.PartnerLogo
import de.chennemann.plannr.data.Contract
import de.chennemann.plannr.data.Transaction
import de.chennemann.plannr.money.MoneyFormatter
import de.chennemann.plannr.resources.Res
import de.chennemann.plannr.ui.components.layout.twopane.*
import de.chennemann.plannr.ui.screen.finances.contracts.contractColor
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.launch

@Composable
fun FinancesHomePageContent(component: FinancesRootComponent) {
    val partners by component.partners.collectAsState()
    val contracts by component.contractOverview.contracts.collectAsState()
    val accounts by component.accountOverview.accounts.collectAsState()
    val history by component.transactionOverview.history.collectAsState()
    val loading by component.transactionOverview.loading.collectAsState()
    val error by component.transactionOverview.error.collectAsState()
    val mainScroll = rememberLazyListState()
    val transactionScroll = rememberLazyListState()
    val pager = rememberTwoPanePagerState(mainScrollableState = mainScroll, secondaryScrollableState = transactionScroll)
    val transitioning by pager.isPageTransitionInProgress.collectAsState()
    val scope = rememberCoroutineScope()
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var sort by rememberSaveable { mutableStateOf("Name A–Z") }
    var sortOpen by remember { mutableStateOf(false) }
    val visible = accounts.sortedBy { it.accountName.lowercase() }
    Surface(color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
    TwoPaneLayout(
        pagerState = pager,
        mainPageContent = {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                LazyColumn(
                    state = mainScroll, userScrollEnabled = !transitioning,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Wallet", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { scope.launch { component.contractOverview.addContract() } }) {
                                Icon(painterResource(Res.images.add), "Add contract")
                            }
                            Box {
                                IconButton(onClick = { sortOpen = true }) {
                                    Icon(painterResource(Res.images.filter_1), "Sort pockets")
                                }
                                DropdownMenu(sortOpen, { sortOpen = false }) {
                                    listOf("Name A–Z", "Balance: highest", "Balance: lowest").forEach { option ->
                                        DropdownMenuItem(text = { Text(if (sort == option) "✓ $option" else option) },
                                            onClick = { sort = option; sortOpen = false })
                                    }
                                }
                            }
                            IconButton(onClick = { searchOpen = !searchOpen; if (!searchOpen) query = "" }) {
                                Icon(painterResource(if (searchOpen) Res.images.close else Res.images.search), "Search pockets")
                            }
                            IconButton(onClick = component::openManagement) {
                                Icon(painterResource(Res.images.settings), "Wallet management")
                            }
                        }
                        AnimatedVisibility(searchOpen) {
                            OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(),
                                label = { Text("Search accounts or pockets") }, singleLine = true)
                        }
                    }
                    visible.forEach { account ->
                        val pockets = account.pockets.filter {
                            !it.isDefault && (account.accountName.contains(query, true) || it.pocketName.contains(query, true))
                        }.let { list ->
                            when (sort) {
                                "Balance: highest" -> list.sortedByDescending { it.balance }
                                "Balance: lowest" -> list.sortedBy { it.balance }
                                else -> list.sortedBy { it.pocketName.lowercase() }
                            }
                        }
                        if (pockets.isNotEmpty() || query.isBlank() || account.accountName.contains(query, true)) {
                            item(key = "account:${account.accountId}") {
                                Row(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 6.dp)
                                    .clickable { component.accountOverview.onAccountClicked(account) },
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text(account.accountName, Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground,
                                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Column(Modifier.padding(start = 12.dp), horizontalAlignment = Alignment.End) {
                                        Text(MoneyFormatter.format(account.freeBalance, "EUR"),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                                        Text("Total ${MoneyFormatter.format(account.totalBalance, "EUR")}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            items(pockets.chunked(2)) { row ->
                                Row(Modifier.fillMaxWidth().animateContentSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    row.forEach { pocket ->
                                        val contract = contracts.firstOrNull { it.contractId.contractId == pocket.contractId }
                                        WalletPocketCard(pocket, contract?.color ?: pocket.color,
                                            partners.firstOrNull { it.partnerId == contract?.partner?.partnerId }, Modifier.weight(1f)) {
                                            if (contract != null) component.contractOverview.onContractClicked(contract)
                                            else component.accountOverview.onAccountClicked(account)
                                        }
                                    }
                                    if (row.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    if (visible.isEmpty()) item { Text("No accounts yet.", color = MaterialTheme.colorScheme.onBackground) }
                    item {
                        val progress by pager.mainPageTransitionIndicatorProgress.collectAsState()
                        ScrollHint(true, progress)
                    }
                }
            }
        },
        secondaryPageContent = {
            Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                LazyColumn(
                    state = transactionScroll, userScrollEnabled = !transitioning,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { pager.animateToPane(Pane.Main) }) {
                                Icon(painterResource(Res.images.arrow_back), "Back to wallet")
                            }
                            Text("Transactions", Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleLarge)
                            TextButton(onClick = { scope.launch { component.transactionOverview.refresh() } }) { Text("Refresh") }
                        }
                    }
                    item {
                        AnimatedVisibility(loading) { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                        AnimatedVisibility(error != null) {
                            Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                    history.groupBy { it.date }.forEach { (date, entries) ->
                        item(key = "date:$date") {
                            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(date.day.toString(), Modifier.padding(end = 10.dp), style = MaterialTheme.typography.headlineSmall)
                                Column(Modifier.weight(1f)) {
                                    Text(date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelLarge)
                                    Text(date.month.name.lowercase().replaceFirstChar { it.uppercase() } + " " + date.year,
                                        style = MaterialTheme.typography.labelMedium)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    entries.groupBy { it.amount.currency }.forEach { (currency, sameCurrency) ->
                                        Text(MoneyFormatter.format(sameCurrency.sumOf { it.signedAmount }, currency),
                                            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        items(entries, key = { "history:${it.id}:${it.amount.currency}" }) {
                            WalletTransactionRow(it)
                        }
                    }
                    if (history.isEmpty() && !loading && error == null) item {
                        Text("No transaction history yet", Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
    )
}

}

@Composable
private fun ScrollHint(up: Boolean, progress: Float) {
    val tint = androidx.compose.ui.graphics.lerp(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onBackground, progress)
    val rotation = (if (up) 0f else 180f) + 180f * (1f - progress)
    Row(Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(Res.images.drop_up), null, Modifier.size(24.dp).rotate(rotation), tint = tint)
        Text(if (up) "Scroll up for Transactions" else "Scroll down for Wallet", color = tint, fontWeight = FontWeight.Bold)
        Icon(painterResource(Res.images.drop_up), null, Modifier.size(24.dp).rotate(-rotation), tint = tint)
    }
}

@Composable
private fun WalletPocketCard(pocket: Pocket, color: Int, partner: Partner?, modifier: Modifier, onClick: () -> Unit) {
    val baseBackground = if (color == 0) MaterialTheme.colorScheme.surfaceContainerHigh else contractColor(color)
    // Use white even on amber cards; reserve dark text for very bright colors
    // such as the yellow electricity pocket.
    val preferWhite = baseBackground.luminance() < 0.8f
    val background = remember(baseBackground, preferWhite) {
        var adjusted = baseBackground
        if (preferWhite && baseBackground.luminance() < 0.45f) {
            while (adjusted.luminance() > 0.183f) {
                adjusted = androidx.compose.ui.graphics.lerp(adjusted, Color.Black, 0.04f)
            }
        }
        adjusted
    }
    val foreground = if (preferWhite) Color.White else Color.Black
    val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
    Surface(onClick = onClick, modifier = modifier.height((92 * fontScale).dp), shape = RoundedCornerShape(18.dp),
        color = background, contentColor = foreground) {
        Column(Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)) {
            Text(pocket.pocketName, Modifier.fillMaxWidth(), color = foreground,
                style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 12.sp, stepSize = 0.5.sp),
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PartnerLogo(partner, Modifier.size(32.dp))
                Text(MoneyFormatter.format(pocket.balance, "EUR"), Modifier.weight(1f), color = foreground,
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    autoSize = TextAutoSize.StepBased(minFontSize = 14.sp, maxFontSize = 22.sp, stepSize = 0.5.sp),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun WalletTransactionRow(transaction: Transaction, showDate: Boolean = false) {
    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(transaction.title, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(MoneyFormatter.format(transaction.amount), Modifier.padding(start = 8.dp), color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PocketLabel(transaction.sourceName ?: "External", Modifier.weight(1f))
                Text("→", color = MaterialTheme.colorScheme.onSurfaceVariant)
                PocketLabel(transaction.destinationName ?: "External", Modifier.weight(1f))
            }
            if (showDate) Text(transaction.date.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PocketLabel(name: String, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(50), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Text(name, Modifier.padding(horizontal = 8.dp, vertical = 3.dp), maxLines = 1, overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall)
    }
}
