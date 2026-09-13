package de.chennemann.plannr.di

import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun createPlatformModule(platformContext: Any?): Module {

    requireNotNull(platformContext) { "platformContext must not be null" }
    require(platformContext is Context) { "platformContext must be of type Context" }

    return module {
        single<Context> { platformContext }
    }
}