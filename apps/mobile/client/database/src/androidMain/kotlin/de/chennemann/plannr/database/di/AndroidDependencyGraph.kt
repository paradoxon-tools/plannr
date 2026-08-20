package de.chennemann.plannr.database.di

import android.content.Context
import de.chennemann.plannr.database.DriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual fun createPlatformDatabaseModule(platformContext: Any?): Module {

    requireNotNull(platformContext) { "platformContext must not be null" }
    require(platformContext is Context) { "platformContext must be of type Context" }

    return module {
        single {
            DriverFactory(platformContext)
        }
    }
}