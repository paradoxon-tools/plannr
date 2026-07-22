package de.chennemann.plannr.server.transactions.templates.service

import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate

interface TransactionTemplateService {
    suspend fun create(command: CreateCommand): TransactionTemplate
    suspend fun createBatch(commands: List<CreateCommand>): List<TransactionTemplate> {
        val created = mutableListOf<TransactionTemplate>()
        for (command in commands) {
            created += create(command)
        }
        return created
    }
    suspend fun update(command: UpdateCommand): TransactionTemplate
    suspend fun archive(id: Long): TransactionTemplate
    suspend fun unarchive(id: Long): TransactionTemplate
    suspend fun archiveForPocket(pocketId: Long)
    suspend fun unarchiveForPocket(pocketId: Long)
    suspend fun delete(id: Long)
    suspend fun list(archived: Boolean? = null): List<TransactionTemplate>
    suspend fun getById(id: Long): TransactionTemplate?

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
