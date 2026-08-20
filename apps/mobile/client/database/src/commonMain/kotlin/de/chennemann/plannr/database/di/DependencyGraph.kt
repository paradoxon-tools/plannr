package de.chennemann.plannr.database.di

import de.chennemann.plannr.database.repository.AccountRepository
import de.chennemann.plannr.database.DriverFactory
import de.chennemann.plannr.database.PlannrDB
import de.chennemann.plannr.database.createDatabase
import de.chennemann.plannr.database.repository.ContractRepository
import de.chennemann.plannr.database.repository.PartnerRepository
import de.chennemann.plannr.database.repository.TransactionRepository
import de.chennemann.plannr.database.repository.TransactionTemplateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module


internal expect fun createPlatformDatabaseModule(platformContext: Any?): Module


object ApplicationScope

fun createDatabaseModule(platformContext: Any?) = module {

    includes(createPlatformDatabaseModule(platformContext))

    single<CoroutineScope>(qualifier<ApplicationScope>()) {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
    single<PlannrDB> {
        createDatabase(get<DriverFactory>().createDriver())
    }
    single {
        AccountRepository(get(), get(qualifier<ApplicationScope>()))
    }.withOptions {
        createdAtStart()
    }
    single {
        ContractRepository(get(), get(qualifier<ApplicationScope>()))
    }.withOptions {
        createdAtStart()
    }
    single {
        PartnerRepository(get(), get(qualifier<ApplicationScope>()))
    }.withOptions {
        createdAtStart()
    }
    single {
        TransactionRepository(get(), get(qualifier<ApplicationScope>()))
    }.withOptions {
        createdAtStart()
    }
    single {
        TransactionTemplateRepository(get(), get(qualifier<ApplicationScope>()))
    }.withOptions {
        createdAtStart()
    }
}