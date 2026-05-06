package de.chennemann.plannr.server.transactions.recurring.support

import de.chennemann.plannr.server.transactions.recurring.api.dto.CreateRecurringTransactionRequest
import de.chennemann.plannr.server.transactions.recurring.api.dto.UpdateRecurringTransactionRequest
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransaction
import de.chennemann.plannr.server.transactions.recurring.service.RecurringTransactionService

object RecurringTransactionFixtures {
    const val DEFAULT_ID = "rtx_123"
    const val DEFAULT_ACCOUNT_ID = 1L
    const val DEFAULT_SOURCE_POCKET_ID = 1L
    const val DEFAULT_DESTINATION_POCKET_ID = 2L
    const val DEFAULT_PARTNER_ID = 1L
    const val DEFAULT_TITLE = "Internet Bill"
    const val DEFAULT_DESCRIPTION = "Monthly internet"
    const val DEFAULT_AMOUNT = 4999L
    const val DEFAULT_CURRENCY_CODE = "EUR"
    const val DEFAULT_TRANSACTION_TYPE = "EXPENSE"
    const val DEFAULT_FIRST_OCCURRENCE_DATE = "2024-01-01"
    const val DEFAULT_FINAL_OCCURRENCE_DATE = "2024-12-31"
    const val DEFAULT_RECURRENCE_TYPE = "MONTHLY"
    const val DEFAULT_SKIP_COUNT = 0
    val DEFAULT_DAYS_OF_WEEK = listOf("MONDAY")
    val DEFAULT_WEEKS_OF_MONTH = listOf(1)
    val DEFAULT_DAYS_OF_MONTH = listOf(1)
    val DEFAULT_MONTHS_OF_YEAR = listOf(1)
    const val DEFAULT_CREATED_AT = 1_710_000_400L

    fun recurringTransaction(
        id: String = DEFAULT_ID,
        accountId: Long = DEFAULT_ACCOUNT_ID,
        sourcePocketId: Long? = DEFAULT_SOURCE_POCKET_ID,
        destinationPocketId: Long? = null,
        partnerId: Long? = DEFAULT_PARTNER_ID,
        title: String = DEFAULT_TITLE,
        description: String? = DEFAULT_DESCRIPTION,
        amount: Long = DEFAULT_AMOUNT,
        currencyCode: String = DEFAULT_CURRENCY_CODE,
        transactionType: String = DEFAULT_TRANSACTION_TYPE,
        firstOccurrenceDate: String = DEFAULT_FIRST_OCCURRENCE_DATE,
        finalOccurrenceDate: String? = DEFAULT_FINAL_OCCURRENCE_DATE,
        recurrenceType: String = DEFAULT_RECURRENCE_TYPE,
        skipCount: Int = DEFAULT_SKIP_COUNT,
        daysOfWeek: List<String>? = DEFAULT_DAYS_OF_WEEK,
        weeksOfMonth: List<Int>? = DEFAULT_WEEKS_OF_MONTH,
        daysOfMonth: List<Int>? = DEFAULT_DAYS_OF_MONTH,
        monthsOfYear: List<Int>? = DEFAULT_MONTHS_OF_YEAR,
        previousVersionId: String? = null,
        isArchived: Boolean = false,
        createdAt: Long = DEFAULT_CREATED_AT,
    ) = RecurringTransaction(
        id, accountId, sourcePocketId, destinationPocketId, partnerId, title, description, amount,
        currencyCode, transactionType, firstOccurrenceDate, finalOccurrenceDate, recurrenceType, skipCount,
        daysOfWeek, weeksOfMonth, daysOfMonth, monthsOfYear, previousVersionId,
        isArchived, createdAt,
    )

    fun createCommand(
        sourcePocketId: Long? = DEFAULT_SOURCE_POCKET_ID,
        destinationPocketId: Long? = null,
        partnerId: Long? = DEFAULT_PARTNER_ID,
        title: String = DEFAULT_TITLE,
        description: String? = DEFAULT_DESCRIPTION,
        amount: Long = DEFAULT_AMOUNT,
        currencyCode: String = DEFAULT_CURRENCY_CODE,
        transactionType: String = DEFAULT_TRANSACTION_TYPE,
        firstOccurrenceDate: String = DEFAULT_FIRST_OCCURRENCE_DATE,
        finalOccurrenceDate: String? = DEFAULT_FINAL_OCCURRENCE_DATE,
        recurrenceType: String = DEFAULT_RECURRENCE_TYPE,
        skipCount: Int = DEFAULT_SKIP_COUNT,
        daysOfWeek: List<String>? = DEFAULT_DAYS_OF_WEEK,
        weeksOfMonth: List<Int>? = DEFAULT_WEEKS_OF_MONTH,
        daysOfMonth: List<Int>? = DEFAULT_DAYS_OF_MONTH,
        monthsOfYear: List<Int>? = DEFAULT_MONTHS_OF_YEAR,
        maxRecurrenceCount: Int? = null,
    ) = RecurringTransactionService.CreateCommand(sourcePocketId, destinationPocketId, partnerId, title, description, amount, currencyCode, transactionType, firstOccurrenceDate, finalOccurrenceDate, recurrenceType, skipCount, daysOfWeek, weeksOfMonth, daysOfMonth, monthsOfYear, maxRecurrenceCount)

    fun updateRequest(
        updateMode: String = "overwrite",
        sourcePocketId: Long? = DEFAULT_SOURCE_POCKET_ID,
        destinationPocketId: Long? = null,
        partnerId: Long? = DEFAULT_PARTNER_ID,
        title: String = DEFAULT_TITLE,
        description: String? = DEFAULT_DESCRIPTION,
        amount: Long = DEFAULT_AMOUNT,
        currencyCode: String = DEFAULT_CURRENCY_CODE,
        transactionType: String = DEFAULT_TRANSACTION_TYPE,
        firstOccurrenceDate: String = DEFAULT_FIRST_OCCURRENCE_DATE,
        finalOccurrenceDate: String? = DEFAULT_FINAL_OCCURRENCE_DATE,
        recurrenceType: String = DEFAULT_RECURRENCE_TYPE,
        skipCount: Int = DEFAULT_SKIP_COUNT,
        daysOfWeek: List<String>? = DEFAULT_DAYS_OF_WEEK,
        weeksOfMonth: List<Int>? = DEFAULT_WEEKS_OF_MONTH,
        daysOfMonth: List<Int>? = DEFAULT_DAYS_OF_MONTH,
        monthsOfYear: List<Int>? = DEFAULT_MONTHS_OF_YEAR,
        maxRecurrenceCount: Int? = null,
    ) = UpdateRecurringTransactionRequest(updateMode, sourcePocketId, destinationPocketId, partnerId, title, description, amount, currencyCode, transactionType, firstOccurrenceDate, finalOccurrenceDate, recurrenceType, skipCount, daysOfWeek, weeksOfMonth, daysOfMonth, monthsOfYear, maxRecurrenceCount)

    fun createRequest(
        sourcePocketId: Long? = DEFAULT_SOURCE_POCKET_ID,
        destinationPocketId: Long? = null,
        partnerId: Long? = DEFAULT_PARTNER_ID,
        title: String = DEFAULT_TITLE,
        description: String? = DEFAULT_DESCRIPTION,
        amount: Long = DEFAULT_AMOUNT,
        currencyCode: String = DEFAULT_CURRENCY_CODE,
        transactionType: String = DEFAULT_TRANSACTION_TYPE,
        firstOccurrenceDate: String = DEFAULT_FIRST_OCCURRENCE_DATE,
        finalOccurrenceDate: String? = DEFAULT_FINAL_OCCURRENCE_DATE,
        recurrenceType: String = DEFAULT_RECURRENCE_TYPE,
        skipCount: Int = DEFAULT_SKIP_COUNT,
        daysOfWeek: List<String>? = DEFAULT_DAYS_OF_WEEK,
        weeksOfMonth: List<Int>? = DEFAULT_WEEKS_OF_MONTH,
        daysOfMonth: List<Int>? = DEFAULT_DAYS_OF_MONTH,
        monthsOfYear: List<Int>? = DEFAULT_MONTHS_OF_YEAR,
        maxRecurrenceCount: Int? = null,
    ) = CreateRecurringTransactionRequest(sourcePocketId, destinationPocketId, partnerId, title, description, amount, currencyCode, transactionType, firstOccurrenceDate, finalOccurrenceDate, recurrenceType, skipCount, daysOfWeek, weeksOfMonth, daysOfMonth, monthsOfYear, maxRecurrenceCount)
}

