package de.chennemann.plannr.database.di

import de.chennemann.plannr.database.DriverFactory
import org.koin.dsl.module

internal actual fun createPlatformDatabaseModule(platformContext: Any?) = module {
    single { DriverFactory() }
}