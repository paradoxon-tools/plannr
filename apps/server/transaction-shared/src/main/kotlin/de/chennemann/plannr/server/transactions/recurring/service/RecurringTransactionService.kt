package de.chennemann.plannr.server.transactions.recurring.service

import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransaction

interface RecurringTransactionService {
    suspend fun create(command: CreateCommand): RecurringTransaction
    suspend fun update(command: UpdateCommand): RecurringTransaction
    suspend fun archive(id: Long): RecurringTransaction
    suspend fun unarchive(id: Long): RecurringTransaction
    suspend fun archiveForPocket(pocketId: Long)
    suspend fun unarchiveForPocket(pocketId: Long)
    suspend fun delete(id: Long)
    suspend fun list(archived: Boolean? = null): List<RecurringTransaction>
    suspend fun getById(id: Long): RecurringTransaction?

    data class CreateCommand(
        val sourcePocketId: Long?,
        val destinationPocketId: Long?,
        val partnerId: Long?,
        val title: String,
        val description: String?,
        val amount: Long,
        val currencyCode: String,
        val transactionType: String,
        val firstOccurrenceDate: String,
        val finalOccurrenceDate: String?,
        val recurrenceType: String,
        val skipCount: Int,
        val daysOfWeek: List<String>?,
        val weeksOfMonth: List<Int>?,
        val daysOfMonth: List<Int>?,
        val monthsOfYear: List<Int>?,
        val maxRecurrenceCount: Int?,
    )

    data class UpdateCommand(
        val id: Long,
        val sourcePocketId: Long?,
        val destinationPocketId: Long?,
        val partnerId: Long?,
        val title: String,
        val description: String?,
        val amount: Long,
        val currencyCode: String,
        val transactionType: String,
        val firstOccurrenceDate: String,
        val finalOccurrenceDate: String?,
        val recurrenceType: String,
        val skipCount: Int,
        val daysOfWeek: List<String>?,
        val weeksOfMonth: List<Int>?,
        val daysOfMonth: List<Int>?,
        val monthsOfYear: List<Int>?,
        val maxRecurrenceCount: Int?,
    )
}
