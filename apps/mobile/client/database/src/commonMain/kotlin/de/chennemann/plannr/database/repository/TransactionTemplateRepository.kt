package de.chennemann.plannr.database.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import de.chennemann.plannr.data.Amount
import de.chennemann.plannr.data.ProcessableTransactionTemplate
import de.chennemann.plannr.data.TransactionTemplate
import de.chennemann.plannr.database.PlannrDB
import de.chennemann.plannr.database.finances.LoadProcessableTransactionTemplates
import de.chennemann.plannr.database.runGettingLastId
import de.chennemann.plannr.datetime.atEndOfDay
import de.chennemann.plannr.datetime.atMidday
import de.chennemann.plannr.datetime.now
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import migrations.TransactionTemplates

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
        operator fun invoke(plannrDB: PlannrDB, applicationScope: CoroutineScope): TransactionTemplateRepository =
            CachingTransactionTemplateRepository(plannrDB, applicationScope)
    }
}

private class CachingTransactionTemplateRepository(
    private val plannrDB: PlannrDB,
    private val applicationScope: CoroutineScope
): TransactionTemplateRepository {

    private val timeZone = TimeZone.currentSystemDefault()

    override val transactionTemplates: StateFlow<List<TransactionTemplate>> =
        plannrDB.transactionTemplatesQueries.loadTransactionTemplates()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { loadTransactionTemplates ->
                loadTransactionTemplates.map { it.asDTO(timeZone) }
            }
            .stateIn(
                scope = applicationScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )

    override val processableTransactionTemplates: StateFlow<List<ProcessableTransactionTemplate>> =
        plannrDB.transactionTemplatesQueries
            .loadProcessableTransactionTemplates(
                LocalDate.now().atEndOfDay(timeZone)
            )
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { loadTransactionTemplates ->
                loadTransactionTemplates.map { it.asDTO(timeZone) }
            }
            .stateIn(
                scope = applicationScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )

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
        return applicationScope.async(Dispatchers.IO) {
            plannrDB.runGettingLastId {
                plannrDB.transactionTemplatesQueries.addTransactionTemplate(
                    title = title,
                    description = description,
                    contractId = contractId,
                    recurrenceType = recurrencePattern.recurrenceType,
                    referenceDate = recurrencePattern.referenceDate.atMidday(),
                    finalOccurrenceDate = recurrencePattern.finalOccurrenceDate?.atMidday(),
                    maxRecurrenceCount = recurrencePattern.maxRecurrenceCount,
                    skipCount = recurrencePattern.skipCount,
                    daysOfWeek = recurrencePattern.daysOfWeek,
                    weeksOfMonth = recurrencePattern.weeksOfMonth,
                    daysOfMonth = recurrencePattern.daysOfMonth,
                    monthsOfYear = recurrencePattern.monthsOfYear,
                    amount = amount.amount,
                    currency = amount.currency,
                    sourcePocketId = sourcePocketId,
                    destinationPocketId = destinationPocketId,
                    partnerId = partnerId
                )
            }
        }.await()
    }
}


private fun TransactionTemplates.asDTO(timeZone: TimeZone) = TransactionTemplate(
    id = transactionTemplateId,
    title = title,
    description = description,
    contractId = contractId,
    recurrencePattern = TransactionTemplate.RecurrencePattern(
        recurrenceType = recurrenceType,
        referenceDate = referenceDate.toLocalDateTime(timeZone).date,
        finalOccurrenceDate = finalOccurrenceDate?.toLocalDateTime(timeZone)?.date,
        maxRecurrenceCount = maxRecurrenceCount,
        skipCount = skipCount,
        daysOfWeek = daysOfWeek,
        daysOfMonth = daysOfMonth,
        weeksOfMonth = weeksOfMonth,
        monthsOfYear = monthsOfYear
    ),
    amount = Amount(amount, currency),
    sourcePocketId = sourcePocketId,
    destinationPocketId = destinationPocketId,
    partnerId = partnerId
)

private fun LoadProcessableTransactionTemplates.asDTO(timeZone: TimeZone) = ProcessableTransactionTemplate(
    template = TransactionTemplate(
        id = transactionTemplateId,
        title = title,
        description = description,
        contractId = contractId,
        recurrencePattern = TransactionTemplate.RecurrencePattern(
            recurrenceType = recurrenceType,
            referenceDate = referenceDate.toLocalDateTime(timeZone).date,
            finalOccurrenceDate = finalOccurrenceDate?.toLocalDateTime(timeZone)?.date,
            maxRecurrenceCount = maxRecurrenceCount,
            skipCount = skipCount,
            daysOfWeek = daysOfWeek,
            weeksOfMonth = weeksOfMonth,
            daysOfMonth = daysOfMonth,
            monthsOfYear = monthsOfYear
        ),
        amount = Amount(amount = amount, currency = currency),
        sourcePocketId = sourcePocketId,
        destinationPocketId = destinationPocketId,
        partnerId = partnerId
    ),
    nextOccurrence = nextOccurrence?.toLocalDateTime(timeZone)?.date,
    lastMaterializedOccurrence = lastMaterializedOccurrence?.toLocalDateTime(timeZone)?.date
)