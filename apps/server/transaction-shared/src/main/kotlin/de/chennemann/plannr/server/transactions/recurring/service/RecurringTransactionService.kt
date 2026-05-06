package de.chennemann.plannr.server.transactions.recurring.service

import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransaction

interface RecurringTransactionService {
    suspend fun create(command: CreateCommand): RecurringTransaction
    suspend fun update(command: UpdateCommand): RecurringTransaction
    suspend fun archive(id: String): RecurringTransaction
    suspend fun unarchive(id: String): RecurringTransaction
    suspend fun archiveForAccount(accountId: Long)
    suspend fun unarchiveForAccount(accountId: Long)
    suspend fun archiveForPocket(accountId: Long, pocketId: String)
    suspend fun unarchiveForPocket(accountId: Long, pocketId: String)
    suspend fun delete(id: String)

    data class CreateCommand(
        val sourcePocketId: String?,
        val destinationPocketId: String?,
        val partnerId: String?,
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
        val id: String,
        val updateMode: String,
        val sourcePocketId: String?,
        val destinationPocketId: String?,
        val partnerId: String?,
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
