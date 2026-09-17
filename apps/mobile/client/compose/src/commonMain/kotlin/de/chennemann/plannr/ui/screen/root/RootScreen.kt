package de.chennemann.plannr.ui.screen.root

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import de.chennemann.plannr.database.repository.PartnerRepository
import de.chennemann.plannr.ui.screen.finances.management.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.chennemann.plannr.ui.screen.finances.FinancesRootComponent
import de.chennemann.plannr.ui.screen.finances.accounts.add.AddAccountComponent
import de.chennemann.plannr.ui.screen.finances.accounts.add.AddAccountScreen
import de.chennemann.plannr.ui.screen.finances.accounts.details.AccountDetailsComponent
import de.chennemann.plannr.ui.screen.finances.accounts.details.AccountDetailsScreen
import de.chennemann.plannr.ui.screen.finances.contracts.ContractDetailsComponent
import de.chennemann.plannr.ui.screen.finances.contracts.ContractDetailsScreen
import de.chennemann.plannr.ui.screen.finances.contracts.add.AddContractComponent
import de.chennemann.plannr.ui.screen.finances.contracts.add.AddContractScreen
import de.chennemann.plannr.ui.screen.groceries.GroceriesDialog
import de.chennemann.plannr.ui.screen.groceries.GroceriesRootComponent
import de.chennemann.plannr.ui.screen.home.HomeScreen
import org.koin.compose.koinInject

private const val MANAGEMENT_ROUTE = "wallet-management"
private const val PARTNERS_ROUTE = "partner-management"
private const val PARTNER_EDITOR_ROUTE = "partner-editor"
private const val HOME_ROUTE = "home"
private const val ACCOUNT_DETAILS_ROUTE = "account-details"
private const val CONTRACT_DETAILS_ROUTE = "contract-details"
private const val GROCERIES_DIALOG_ROUTE = "groceries-dialog"

private enum class SheetDestination {
    AddAccount,
    AddContract,
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RootScreen() {
    val navController = rememberNavController()
    val rootComponentFactory = koinInject<RootComponent.Factory>()
    val partnerRepository = koinInject<PartnerRepository>()
    val partnerManagement = remember(partnerRepository) { PartnerManagementComponent(partnerRepository) }
    var selectedPartnerId by rememberSaveable { mutableLongStateOf(-1L) }
    val financesFactory = koinInject<FinancesRootComponent.Factory>()
    val groceriesFactory = koinInject<GroceriesRootComponent.Factory>()
    val accountDetailsFactory = koinInject<AccountDetailsComponent.Factory>()
    val contractDetailsFactory = koinInject<ContractDetailsComponent.Factory>()
    val addAccountFactory = koinInject<AddAccountComponent.Factory>()
    val addContractFactory = koinInject<AddContractComponent.Factory>()

    remember(rootComponentFactory) { rootComponentFactory() }

    var selectedAccountId by remember { mutableLongStateOf(-1L) }
    var selectedContractId by remember { mutableLongStateOf(-1L) }
    var sheetDestination by remember { mutableStateOf<SheetDestination?>(null) }

    val financesComponent = remember(financesFactory) {
        financesFactory(
            onAccountClicked = { account ->
                selectedAccountId = account.accountId
                navController.navigate(ACCOUNT_DETAILS_ROUTE)
            },
            onAddAccountRequested = {
                sheetDestination = SheetDestination.AddAccount
            },
            onContractClicked = { contract ->
                selectedContractId = contract.contractId.contractId
                navController.navigate(CONTRACT_DETAILS_ROUTE)
            },
            onManagementRequested = { navController.navigate(MANAGEMENT_ROUTE) },
            onAddContractRequested = {
                sheetDestination = SheetDestination.AddContract
            },
        )
    }
    val groceriesComponent = remember(groceriesFactory) {
        groceriesFactory {
            navController.navigate(GROCERIES_DIALOG_ROUTE)
        }
    }

    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE,
    ) {
        composable(HOME_ROUTE) {
            HomeScreen(
                financesComponent = financesComponent,
                groceriesComponent = groceriesComponent,
            )
        }
        composable(MANAGEMENT_ROUTE) {
            WalletManagementScreen(
                onBack = { navController.popBackStack() },
                onPartners = { navController.navigate(PARTNERS_ROUTE) },
            )
        }
        composable(PARTNERS_ROUTE) {
            PartnerManagementScreen(partnerManagement,
                onBack = { navController.popBackStack() },
                onPartner = { selectedPartnerId = it; navController.navigate(PARTNER_EDITOR_ROUTE) },
            )
        }
        composable(PARTNER_EDITOR_ROUTE) {
            PartnerEditorScreen(partnerManagement, selectedPartnerId, onBack = { navController.popBackStack() })
        }
        composable(ACCOUNT_DETAILS_ROUTE) {
            AccountDetailsScreen(
                component = remember(selectedAccountId) { accountDetailsFactory(selectedAccountId) },
            )
        }
        composable(CONTRACT_DETAILS_ROUTE) {
            ContractDetailsScreen(
                component = remember(selectedContractId) { contractDetailsFactory(selectedContractId) },
            )
        }
        composable(GROCERIES_DIALOG_ROUTE) {
            GroceriesDialog(onDismiss = { navController.popBackStack() })
        }
    }

    val activeSheet = sheetDestination
    if (activeSheet != null) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = { sheetDestination = null },
        ) {
            when (activeSheet) {
                SheetDestination.AddAccount -> {
                    AddAccountScreen(
                        component = remember(addAccountFactory) { addAccountFactory() },
                        onCreated = { sheetDestination = null },
                    )
                }

                SheetDestination.AddContract -> {
                    AddContractScreen(
                        component = remember(addContractFactory) { addContractFactory() },
                        onCreated = { sheetDestination = null },
                    )
                }
            }
        }
    }
}
