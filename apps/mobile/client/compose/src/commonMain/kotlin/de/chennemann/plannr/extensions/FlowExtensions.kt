package de.chennemann.plannr.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

inline fun <T, R> derive(flow: Flow<T>, crossinline transform: suspend (value: T) -> R) =
    flow.map(transform = transform)