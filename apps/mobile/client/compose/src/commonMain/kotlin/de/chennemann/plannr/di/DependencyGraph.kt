package de.chennemann.plannr.di

import de.chennemann.plannr.database.di.createDatabaseModule
import de.chennemann.plannr.ui.screen.finances.FinancesRootComponent
import de.chennemann.plannr.ui.screen.finances.accounts.details.AccountDetailsComponent
import de.chennemann.plannr.ui.screen.finances.accounts.overview.AccountOverviewComponent
import de.chennemann.plannr.ui.screen.finances.accounts.add.AddAccountComponent
import de.chennemann.plannr.ui.screen.finances.contracts.ContractDetailsComponent
import de.chennemann.plannr.ui.screen.finances.contracts.ContractOverviewComponent
import de.chennemann.plannr.ui.screen.finances.contracts.add.AddContractComponent
import de.chennemann.plannr.ui.screen.finances.transactions.TransactionOverviewComponent
import de.chennemann.plannr.ui.screen.groceries.GroceriesRootComponent
import de.chennemann.plannr.ui.screen.root.RootComponent
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module


expect fun createPlatformModule(platformContext: Any?): Module

val financesModule = module {
    singleOf(FinancesRootComponent::Factory)
    singleOf(AccountOverviewComponent::Factory)
    singleOf(AccountDetailsComponent::Factory)
    singleOf(AddAccountComponent::Factory)
    singleOf(ContractOverviewComponent::Factory)
    singleOf(ContractDetailsComponent::Factory)
    singleOf(AddContractComponent::Factory)
    singleOf(TransactionOverviewComponent::Factory)
}

val groceriesModule = module {
    singleOf(GroceriesRootComponent::Factory)
}

val applicationModule = module {
    singleOf(RootComponent::Factory)

    includes(financesModule, groceriesModule)
}

fun dependencyGraph(
    platformContext: Any?,
): KoinAppDeclaration {
    return {
        modules(createPlatformModule(platformContext), createDatabaseModule(platformContext), applicationModule)
    }
}
