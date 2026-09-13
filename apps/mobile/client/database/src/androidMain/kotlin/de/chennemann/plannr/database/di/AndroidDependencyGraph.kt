package de.chennemann.plannr.database.di

import org.koin.core.module.Module
import org.koin.dsl.module

internal actual fun createPlatformDatabaseModule(platformContext: Any?): Module {
    return module {}
}
