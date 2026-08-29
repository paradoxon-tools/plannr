package de.chennemann.plannr.database.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class RefreshLock {
    private val mutex = Mutex()

    suspend fun <T> withRefreshLock(block: suspend () -> T): T = mutex.withLock {
        block()
    }
}

internal fun CoroutineScope.launchRefresh(
    label: String,
    block: suspend () -> Unit,
) {
    launch {
        runCatching {
            block()
        }.onFailure { error ->
            println("[$label] ${error::class.simpleName}: ${error.message}")
        }
    }
}
