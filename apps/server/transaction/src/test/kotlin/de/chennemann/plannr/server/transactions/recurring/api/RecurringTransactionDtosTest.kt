package de.chennemann.plannr.server.transactions.recurring.api

import de.chennemann.plannr.server.transactions.recurring.api.dto.CreateRecurringTransactionRequest
import de.chennemann.plannr.server.transactions.recurring.api.dto.UpdateRecurringTransactionRequest
import kotlin.test.Test
import kotlin.test.assertEquals

class RecurringTransactionDtosTest {
    @Test
    fun `create request maps max recurrence count to command`() {
        val command = CreateRecurringTransactionRequest(
            sourcePocketId = 1L,
            destinationPocketId = null,
            partnerId = null,
            title = "Rent",
            description = null,
            amount = 1000,
            currencyCode = "EUR",
            transactionType = "EXPENSE",
            firstOccurrenceDate = "2024-01-15",
            finalOccurrenceDate = null,
            recurrenceType = "MONTHLY",
            skipCount = 0,
            daysOfWeek = null,
            weeksOfMonth = null,
            daysOfMonth = listOf(15),
            monthsOfYear = null,
            maxRecurrenceCount = 3,
        ).toCreateCommand()

        assertEquals(3, command.maxRecurrenceCount)
    }

    @Test
    fun `update request maps without effective from date`() {
        val command = UpdateRecurringTransactionRequest(
            updateMode = "new_version",
            sourcePocketId = 1L,
            destinationPocketId = null,
            partnerId = null,
            title = "Rent",
            description = null,
            amount = 1000,
            currencyCode = "EUR",
            transactionType = "EXPENSE",
            firstOccurrenceDate = "2024-06-15",
            finalOccurrenceDate = null,
            recurrenceType = "MONTHLY",
            skipCount = 0,
            daysOfWeek = null,
            weeksOfMonth = null,
            daysOfMonth = listOf(15),
            monthsOfYear = null,
            maxRecurrenceCount = null,
        ).toUpdateCommand("rtx_123")

        assertEquals("new_version", command.updateMode)
        assertEquals("2024-06-15", command.firstOccurrenceDate)
    }
}
