package de.chennemann.plannr.server.transactions.recurring.usecases

interface RecurringTransactionProjectionPort {
    suspend fun markAccountDirty(accountId: String)

    suspend fun markPocketDirty(pocketId: String)
}
