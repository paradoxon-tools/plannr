package de.chennemann.plannr.server.transactions.projection.service

interface TransactionProjectionEventQueue {
    suspend fun enqueue(event: TransactionProjectionChangeEvent)
}
