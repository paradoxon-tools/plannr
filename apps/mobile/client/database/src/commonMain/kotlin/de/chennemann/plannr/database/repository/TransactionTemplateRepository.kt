package de.chennemann.plannr.database.repository

import de.chennemann.plannr.data.Amount
import de.chennemann.plannr.data.ProcessableTransactionTemplate
import de.chennemann.plannr.data.TransactionTemplate
import de.chennemann.plannr.database.remote.PlannrApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface TransactionTemplateRepository {

    val transactionTemplates: StateFlow<List<TransactionTemplate>>
    val processableTransactionTemplates: StateFlow<List<ProcessableTransactionTemplate>>

    suspend fun addTransactionTemplate(
        title: String,
        description: String? = null,
        contractId: Long? = null,
        recurrencePattern: TransactionTemplate.RecurrencePattern,
        amount: Amount,
        sourcePocketId: Long? = null,
        destinationPocketId: Long? = null,
        partnerId: Long? = null
    ): Long


    companion object {
        internal operator fun invoke(apiClient: PlannrApiClient, applicationScope: CoroutineScope): TransactionTemplateRepository =
            RemoteTransactionTemplateRepository()
    }
}

private class RemoteTransactionTemplateRepository : TransactionTemplateRepository {
    override val transactionTemplates: StateFlow<List<TransactionTemplate>> =
        MutableStateFlow(emptyList())
    override val processableTransactionTemplates: StateFlow<List<ProcessableTransactionTemplate>> =
        MutableStateFlow(emptyList())

    override suspend fun addTransactionTemplate(
        title: String,
        description: String?,
        contractId: Long?,
        recurrencePattern: TransactionTemplate.RecurrencePattern,
        amount: Amount,
        sourcePocketId: Long?,
        destinationPocketId: Long?,
        partnerId: Long?,
    ): Long {
        throw UnsupportedOperationException("Transaction template creation is not wired to the server yet.")
    }
}
