package de.chennemann.plannr.server.transactions.recurring.support

import de.chennemann.plannr.server.transactions.recurring.api.dto.CreateRecurringTransactionRequest
import de.chennemann.plannr.server.transactions.recurring.api.dto.UpdateRecurringTransactionRequest
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransaction
import de.chennemann.plannr.server.transactions.recurring.usecases.CreateRecurringTransaction
import de.chennemann.plannr.server.transactions.recurring.usecases.UpdateRecurringTransaction

object RecurringTransactionFixtures {
    const val DEFAULT_ID = "rtx_123"
    const val DEFAULT_CONTRACT_ID = "con_123"
    const val DEFAULT_ACCOUNT_ID = "acc_123"
    const val DEFAULT_SOURCE_POCKET_ID = "poc_123"
    const val DEFAULT_DESTINATION_POCKET_ID = "poc_456"
    const val DEFAULT_PARTNER_ID = "par_123"
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
        contractId: String? = DEFAULT_CONTRACT_ID,
        accountId: String = DEFAULT_ACCOUNT_ID,
        sourcePocketId: String? = DEFAULT_SOURCE_POCKET_ID,
        destinationPocketId: String? = null,
        partnerId: String? = DEFAULT_PARTNER_ID,
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
        lastMaterializedDate: String? = null,
        previousVersionId: String? = null,
        isArchived: Boolean = false,
        createdAt: Long = DEFAULT_CREATED_AT,
    ) = RecurringTransaction(
        id, contractId, accountId, sourcePocketId, destinationPocketId, partnerId, title, description, amount,
        currencyCode, transactionType, firstOccurrenceDate, finalOccurrenceDate, recurrenceType, skipCount,
        daysOfWeek, weeksOfMonth, daysOfMonth, monthsOfYear, lastMaterializedDate, previousVersionId,
        isArchived, createdAt,
    )

    fun createCommand(
        contractId: String? = DEFAULT_CONTRACT_ID,
        sourcePocketId: String? = DEFAULT_SOURCE_POCKET_ID,
        destinationPocketId: String? = null,
        partnerId: String? = DEFAULT_PARTNER_ID,
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
    ) = CreateRecurringTransaction.Command(contractId, sourcePocketId, destinationPocketId, partnerId, title, description, amount, currencyCode, transactionType, firstOccurrenceDate, finalOccurrenceDate, recurrenceType, skipCount, daysOfWeek, weeksOfMonth, daysOfMonth, monthsOfYear, maxRecurrenceCount)

    fun updateRequest(
        updateMode: String = "overwrite",
        contractId: String? = DEFAULT_CONTRACT_ID,
        sourcePocketId: String? = DEFAULT_SOURCE_POCKET_ID,
        destinationPocketId: String? = null,
        partnerId: String? = DEFAULT_PARTNER_ID,
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
    ) = UpdateRecurringTransactionRequest(updateMode, contractId, sourcePocketId, destinationPocketId, partnerId, title, description, amount, currencyCode, transactionType, firstOccurrenceDate, finalOccurrenceDate, recurrenceType, skipCount, daysOfWeek, weeksOfMonth, daysOfMonth, monthsOfYear, maxRecurrenceCount)

    fun createRequest(
        contractId: String? = DEFAULT_CONTRACT_ID,
        sourcePocketId: String? = DEFAULT_SOURCE_POCKET_ID,
        destinationPocketId: String? = null,
        partnerId: String? = DEFAULT_PARTNER_ID,
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
    ) = CreateRecurringTransactionRequest(contractId, sourcePocketId, destinationPocketId, partnerId, title, description, amount, currencyCode, transactionType, firstOccurrenceDate, finalOccurrenceDate, recurrenceType, skipCount, daysOfWeek, weeksOfMonth, daysOfMonth, monthsOfYear, maxRecurrenceCount)
}
