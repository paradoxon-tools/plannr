package de.chennemann.plannr.ui.screen.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.chennemann.plannr.resources.Res
import de.chennemann.plannr.ui.components.layout.pager.NavigationItem
import de.chennemann.plannr.ui.components.layout.pager.NavigationPagerScaffold
import de.chennemann.plannr.ui.components.layout.pager.NavigationPagerTabContainer
import de.chennemann.plannr.ui.components.layout.pager.NavigationTabDefaults
import de.chennemann.plannr.ui.components.layout.pager.rememberNavigationPagerState
import de.chennemann.plannr.ui.screen.chat.ChatScreen
import de.chennemann.plannr.ui.screen.dashboard.DashboardScreen
import de.chennemann.plannr.ui.screen.finances.FinancesRootComponent
import de.chennemann.plannr.ui.screen.finances.FinancesHomePageContent
import de.chennemann.plannr.ui.screen.groceries.GroceriesRootComponent
import de.chennemann.plannr.ui.screen.groceries.GroceriesHomePageContent
import de.chennemann.plannr.ui.theme.colors
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch

private enum class HomePage {
    Dashboard,
    Chat,
    Groceries,
    Finances,
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(
    financesComponent: FinancesRootComponent,
    groceriesComponent: GroceriesRootComponent,
) {
    val pages = remember {
        listOf(
            HomePage.Dashboard,
            HomePage.Chat,
            HomePage.Groceries,
            HomePage.Finances,
        )
    }
    val initialPage = HomePage.Finances.ordinal
    val pagerState = rememberNavigationPagerState(
        initialPage = initialPage,
        pageCount = { pages.size },
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (pagerState.currentPage != initialPage) {
            pagerState.animateToPage(initialPage)
        }
    }

    NavigationPagerScaffold(
        pagerState,
        Modifier
            .fillMaxSize()
            .padding(bottom = 12.dp),
        content = { pageIndex ->
            when (pages[pageIndex]) {
                HomePage.Dashboard -> DashboardScreen()
                HomePage.Chat -> ChatScreen()
                HomePage.Groceries -> GroceriesHomePageContent(groceriesComponent)
                HomePage.Finances -> FinancesHomePageContent(financesComponent)
            }
        },
        bottomBar = {
            NavigationPagerTabContainer(
                state = pagerState,
                colors = NavigationTabDefaults.navigationItemColors(
                    tabColor = MaterialTheme.colors.onBackground,
                    tabIconColor = MaterialTheme.colors.onBackground
                ),
                onPageActivationRequest = { pageIndex ->
                    coroutineScope.launch {
                        pagerState.animateToPage(pageIndex)
                    }
                }
            ) { pageIndex ->
                when (pages[pageIndex]) {
                    HomePage.Dashboard -> NavigationItem(
                        stringResource(Res.strings.bottom_navigation_dashboard_label),
                        painterResource(Res.images.donut_small),
                    )
                    HomePage.Chat -> NavigationItem(
                        stringResource(Res.strings.bottom_navigation_chat_label),
                        painterResource(Res.images.inward_arrows_alt),
                    )
                    HomePage.Groceries -> NavigationItem(
                        stringResource(Res.strings.bottom_navigation_groceries_label),
                        painterResource(Res.images.layers),
                    )
                    HomePage.Finances -> NavigationItem(
                        stringResource(Res.strings.bottom_navigation_finances_label),
                        painterResource(Res.images.wallet),
                    )
                }
            }
        },
    )
}
