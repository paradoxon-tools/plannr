package de.chennemann.plannr.ui.screen.finances.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.chennemann.plannr.data.Transaction
import de.chennemann.plannr.database.repository.TransactionRepository
import de.chennemann.plannr.resources.Res
import de.chennemann.plannr.ui.components.layout.twopane.Pane
import de.chennemann.plannr.ui.components.layout.twopane.TwoPanePagerState
import de.chennemann.plannr.ui.components.layout.twopane.delayedProgress
import de.chennemann.plannr.ui.theme.colors
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.StateFlow

interface TransactionOverviewComponent {
    val transactions: StateFlow<List<Transaction>>

    class Factory(
        private val transactionRepository: TransactionRepository,
    ) {
        operator fun invoke(): TransactionOverviewComponent =
            DefaultTransactionOverviewComponent(transactionRepository)
    }
}

private class DefaultTransactionOverviewComponent(
    transactionRepository: TransactionRepository,
) : TransactionOverviewComponent {
    override val transactions = transactionRepository.transactions
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionOverviewContent(
    component: TransactionOverviewComponent,
    secondaryListState: LazyListState,
    pagerState: TwoPanePagerState,
) {
    val transactions = component.transactions.collectAsState().value

    val pageTransitionInProgress by pagerState.isPageTransitionInProgress.collectAsState()
    val userScrollEnabled = !pageTransitionInProgress

    val progress by pagerState.secondaryPageTransitionProgress.collectAsState()
    val backButtonVisibilityProgress by remember {
        derivedStateOf {
            delayedProgress(progress)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Box(
                Modifier
                    .background(MaterialTheme.colors.background)
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(Res.strings.finances_transactions_section_label),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colors.onBackground,
                    )

                    Text(
                        text = "${transactions.size} upcoming",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colors.onBackgroundMuted,
                    )
                }

                Box(Modifier.padding(start = 32.dp).scale(backButtonVisibilityProgress)) {
                    IconButton(
                        onClick = {
                            pagerState.animateToPane(Pane.Main)
                        },
                        modifier = Modifier.background(MaterialTheme.colors.surfaceVariant, RoundedCornerShape(100)),
                    ) {
                        Icon(
                            painterResource(Res.images.arrow_back),
                            "Back to main view",
                            tint = MaterialTheme.colors.onSurface,
                        )
                    }
                }

                Box(
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 32.dp)
                        .scale(backButtonVisibilityProgress),
                ) {
                    IconButton(onClick = {}) {
                        Icon(
                            painterResource(Res.images.filter_1),
                            "Filter Transactions",
                            tint = MaterialTheme.colors.onSurface,
                        )
                    }
                }
            }
        },
    ) {
        LazyColumn(
            state = secondaryListState,
            userScrollEnabled = userScrollEnabled,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(
                start = 16.dp,
                top = it.calculateTopPadding(),
                end = 16.dp,
            ),
        ) {
            items(transactions) { transaction ->
                TransactionThumbnailContent(transaction = transaction)
            }
        }
    }
}
