package de.chennemann.plannr.ui.screen.finances

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import de.chennemann.plannr.resources.Res
import de.chennemann.plannr.ui.components.button.RippleButton
import de.chennemann.plannr.ui.components.layout.twopane.Pane
import de.chennemann.plannr.ui.components.layout.twopane.TwoPaneLayout
import de.chennemann.plannr.ui.components.layout.twopane.TwoPanePagerState
import de.chennemann.plannr.ui.components.layout.twopane.rememberTwoPanePagerState
import de.chennemann.plannr.ui.screen.finances.accounts.overview.AccountOverviewContent
import de.chennemann.plannr.ui.screen.finances.contracts.ContractOverviewContent
import de.chennemann.plannr.ui.screen.finances.transactions.TransactionOverviewContent
import de.chennemann.plannr.ui.theme.colors
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.lerp as colorLerp


@Composable
fun FabNavigationMenuButton(
    onClick: () -> Unit,
    painter: Painter,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colors.surfaceVariant,
    tint: Color = LocalContentColor.current
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .shadow(2.dp, RoundedCornerShape(100))
            .background(containerColor, RoundedCornerShape(100))
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter, "", tint = tint)
    }
}


@Composable
fun FabNavigationMenu(
    hasNext: Boolean = false,
    onNext: () -> Unit = {},
    hasPrevious: Boolean = false,
    onPrevious: () -> Unit = {},
    floatingActionButton: @Composable () -> Unit,
) {

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {

        AnimatedVisibility(visible = hasPrevious) {
            FabNavigationMenuButton(
                modifier = Modifier.shadow(4.dp, RoundedCornerShape(100)),
                onClick = {
                    onPrevious()
                },
                painter = painterResource(Res.images.chevron_up),
                tint = MaterialTheme.colors.onSurface,
            )
        }

        AnimatedVisibility(visible = hasNext) {
            FabNavigationMenuButton(
                modifier = Modifier.shadow(4.dp, RoundedCornerShape(100)),
                onClick = {
                    onNext()
                },
                painter = painterResource(Res.images.chevron_down),
                tint = MaterialTheme.colors.onSurface,
            )
        }

        floatingActionButton()
    }
}

@Composable
fun FinancesHomePageContent(
    component: FinancesRootComponent
) {
    val mainScrollableState = rememberLazyListState()
    val secondaryScrollableState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var topOfTransactions by remember { mutableStateOf<Int?>(0) }

    LaunchedEffect(secondaryScrollableState) {
        snapshotFlow { secondaryScrollableState.layoutInfo }
            .collect { layoutInfo ->
                val visibleItems = layoutInfo.visibleItemsInfo

                val firstElement = visibleItems.firstOrNull() ?: return@collect

                topOfTransactions = when {
                    firstElement.index > 5 -> 0
                    else -> null
                }
            }
    }

    val pagerState = rememberTwoPanePagerState(
        mainScrollableState = mainScrollableState,
        secondaryScrollableState = secondaryScrollableState,
    )

    TwoPaneLayout(
        floatingActionButton = {
            FabNavigationMenu(
                hasPrevious = topOfTransactions != null,
                onPrevious = {
                    if (topOfTransactions != null) {
                        scope.launch {
                            secondaryScrollableState.animateScrollToItem(topOfTransactions!!)
                        }
                    }
                },
                floatingActionButton = {

                    val currentPane by pagerState.currentPane.collectAsState()

                    ExtendedFloatingActionButton(
                        onClick = {
                            when (currentPane) {
                                Pane.Main -> pagerState.animateToPane(Pane.Secondary)
                                Pane.Secondary -> pagerState.animateToPane(Pane.Main)
                            }
                        },
                        containerColor = MaterialTheme.colors.primary,
                        contentColor = MaterialTheme.colors.onPrimary,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                        text = { Text("New Transaction") },
                        icon = { Icon(painterResource(Res.images.add), "", modifier = Modifier.size(24.dp)) }
                    )
                }
            )
        },
        pagerState = pagerState,
        mainPageContent = {
            MainPage(component, mainScrollableState, pagerState)
        },
        secondaryPageContent = {
            TransactionOverviewContent(
                component = component.transactionOverview,
                secondaryListState = secondaryScrollableState,
                pagerState
            )
        }
    )
}

@Composable
private fun MainPage(
    component: FinancesRootComponent,
    mainScrollableState: LazyListState,
    pagerState: TwoPanePagerState
) {

    val pageTransitionInProgress by pagerState.isPageTransitionInProgress.collectAsState()
    val userScrollEnabled = !pageTransitionInProgress

    val transitionIndicatorProgress by pagerState.mainPageTransitionIndicatorProgress.collectAsState()

    LazyColumn(
        state = mainScrollableState,
        userScrollEnabled = userScrollEnabled,
        verticalArrangement = Arrangement.spacedBy(56.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        item {
            AccountOverviewContent(component = component.accountOverview)
        }

        item {
            ContractOverviewContent(component = component.contractOverview)
        }

        item {
            BudgetOverviewSection()
        }

        item {
            val color = colorLerp(MaterialTheme.colorScheme.onBackground, MaterialTheme.colorScheme.primary, 1f - transitionIndicatorProgress)

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(Res.images.drop_up),
                    contentDescription = "",
                    tint = color,
                    modifier = Modifier.size(24.dp).rotate(-180f * (1f - transitionIndicatorProgress))
                )
                Text("Scroll up for Transactions", color = color, fontWeight = FontWeight.Bold)
                Icon(
                    painterResource(Res.images.drop_up),
                    contentDescription = "",
                    tint = color,
                    modifier = Modifier.size(24.dp).rotate(180f * (1f - transitionIndicatorProgress))
                )
            }
        }
    }
}

@Composable
private fun BudgetOverviewSection() {
    Text(
        text = "Budgets",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colors.onBackground
    )

    Spacer(Modifier.height(16.dp))

    VerticalGrid(
        columns = SimpleGridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(4) { idx ->
            BudgetPlaceholderCard(
                title = "Budget ${idx + 1}",
                detail = if (idx % 2 == 0) "No limit set" else "Track spending"
            )
        }
    }

    Spacer(Modifier.height(24.dp))

    RippleButton(onClick = {}) {
        Text("Add Budget")
    }
}

@Composable
private fun BudgetPlaceholderCard(
    title: String,
    detail: String,
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.surfaceVariant, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colors.tertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colors.onSurface
        )
    }
}
