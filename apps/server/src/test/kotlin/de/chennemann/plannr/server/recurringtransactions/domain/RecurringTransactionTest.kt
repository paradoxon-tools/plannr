package de.chennemann.plannr.server.recurringtransactions.domain

import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.recurringtransactions.support.RecurringTransactionFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFailsWith

class RecurringTransactionTest {
    @Test
    fun `normalizes strings and optional values`() {
        val value = RecurringTransactionFixtures.recurringTransaction(
            id = " rtx_123 ",
            contractId = "   ",
            accountId = " acc_123 ",
            sourcePocketId = " poc_123 ",
            title = " Internet Bill ",
            description = " Monthly internet ",
            currencyCode = " eur ",
            transactionType = " EXPENSE ",
            recurrenceType = " MONTHLY ",
            finalOccurrenceDate = "   ",
        )
        assertEquals("rtx_123", value.id)
        assertNull(value.contractId)
        assertEquals("acc_123", value.accountId)
        assertEquals("Internet Bill", value.title)
        assertEquals("Monthly internet", value.description)
        assertEquals("EUR", value.currencyCode)
        assertEquals("EXPENSE", value.transactionType)
        assertEquals("MONTHLY", value.recurrenceType)
        assertNull(value.finalOccurrenceDate)
    }

    @Test
    fun `rejects invalid dates and negatives`() {
        assertFailsWith<ValidationException> { RecurringTransactionFixtures.recurringTransaction(firstOccurrenceDate = "2024/01/01") }
        assertFailsWith<ValidationException> { RecurringTransactionFixtures.recurringTransaction(finalOccurrenceDate = "2024/12/31") }
        assertFailsWith<ValidationException> { RecurringTransactionFixtures.recurringTransaction(amount = -1) }
        assertFailsWith<ValidationException> { RecurringTransactionFixtures.recurringTransaction(skipCount = -1) }
    }

    @Test
    fun `accepts all valid transaction type combinations`() {
        RecurringTransactionFixtures.recurringTransaction(
            transactionType = "EXPENSE",
            sourcePocketId = "poc_123",
            destinationPocketId = null,
        )
        RecurringTransactionFixtures.recurringTransaction(
            transactionType = "INCOME",
            sourcePocketId = null,
            destinationPocketId = "poc_456",
        )
        RecurringTransactionFixtures.recurringTransaction(
            transactionType = "TRANSFER",
            sourcePocketId = "poc_123",
            destinationPocketId = "poc_456",
        )
    }

    @Test
    fun `rejects every invalid transaction type combination`() {
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(transactionType = "EXPENSE", sourcePocketId = null, destinationPocketId = null)
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(transactionType = "EXPENSE", sourcePocketId = "poc_123", destinationPocketId = "poc_456")
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(transactionType = "INCOME", sourcePocketId = null, destinationPocketId = null)
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(transactionType = "INCOME", sourcePocketId = "poc_123", destinationPocketId = "poc_456")
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(transactionType = "TRANSFER", sourcePocketId = null, destinationPocketId = "poc_456")
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(transactionType = "TRANSFER", sourcePocketId = "poc_123", destinationPocketId = null)
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(transactionType = "TRANSFER", sourcePocketId = "poc_123", destinationPocketId = "poc_123")
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(transactionType = "unknown", sourcePocketId = "poc_123", destinationPocketId = null)
        }
    }

    @Test
    fun `accepts all valid none recurrence combinations`() {
        RecurringTransactionFixtures.recurringTransaction(
            recurrenceType = "NONE",
            skipCount = 0,
            daysOfWeek = null,
            weeksOfMonth = null,
            daysOfMonth = null,
            monthsOfYear = null,
        )
    }

    @Test
    fun `rejects every invalid none recurrence combination`() {
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(recurrenceType = "NONE", skipCount = 1, daysOfWeek = null, weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null)
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(recurrenceType = "NONE", skipCount = 0, daysOfWeek = listOf("MONDAY"), weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null)
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(recurrenceType = "NONE", skipCount = 0, daysOfWeek = null, weeksOfMonth = listOf(1), daysOfMonth = null, monthsOfYear = null)
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(recurrenceType = "NONE", skipCount = 0, daysOfWeek = null, weeksOfMonth = null, daysOfMonth = listOf(1), monthsOfYear = null)
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(recurrenceType = "NONE", skipCount = 0, daysOfWeek = null, weeksOfMonth = null, daysOfMonth = null, monthsOfYear = listOf(1))
        }
    }

    @Test
    fun `accepts all valid daily recurrence combinations`() {
        RecurringTransactionFixtures.recurringTransaction(recurrenceType = "DAILY", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null, skipCount = 0)
        RecurringTransactionFixtures.recurringTransaction(recurrenceType = "DAILY", daysOfWeek = listOf("MONDAY"), weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null, skipCount = 0)
        RecurringTransactionFixtures.recurringTransaction(recurrenceType = "DAILY", daysOfWeek = listOf("MONDAY"), weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null, skipCount = 2)
    }

    @Test
    fun `rejects every invalid daily recurrence combination`() {
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(recurrenceType = "DAILY", weeksOfMonth = listOf(1))
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(recurrenceType = "DAILY", daysOfMonth = listOf(1))
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(recurrenceType = "DAILY", monthsOfYear = listOf(1))
        }
    }

    @Test
    fun `accepts all valid weekly recurrence combinations`() {
        RecurringTransactionFixtures.recurringTransaction(recurrenceType = "WEEKLY", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null, skipCount = 0)
        RecurringTransactionFixtures.recurringTransaction(recurrenceType = "WEEKLY", daysOfWeek = listOf("MONDAY"), weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null, skipCount = 1)
        RecurringTransactionFixtures.recurringTransaction(recurrenceType = "WEEKLY", daysOfWeek = null, weeksOfMonth = listOf(1, -1), daysOfMonth = null, monthsOfYear = null, skipCount = 0)
        RecurringTransactionFixtures.recurringTransaction(recurrenceType = "WEEKLY", daysOfWeek = listOf("MONDAY"), weeksOfMonth = listOf(1, -1), daysOfMonth = null, monthsOfYear = null, skipCount = 0)
    }

    @Test
    fun `rejects every invalid weekly recurrence combination`() {
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(recurrenceType = "WEEKLY", daysOfMonth = listOf(1))
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(recurrenceType = "WEEKLY", monthsOfYear = listOf(1))
        }
    }

    @Test
    fun `accepts all valid monthly recurrence combinations`() {
        RecurringTransactionFixtures.recurringTransaction(recurrenceType = "MONTHLY", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null, skipCount = 0)
        RecurringTransactionFixtures.recurringTransaction(recurrenceType = "MONTHLY", daysOfWeek = null, weeksOfMonth = listOf(1, -1), daysOfMonth = null, monthsOfYear = null, skipCount = 1)
        RecurringTransactionFixtures.recurringTransaction(recurrenceType = "MONTHLY", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = listOf(1, -1), monthsOfYear = null, skipCount = 0)
        RecurringTransactionFixtures.recurringTransaction(recurrenceType = "MONTHLY", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = null, monthsOfYear = listOf(1, 6), skipCount = 0)
        RecurringTransactionFixtures.recurringTransaction(recurrenceType = "MONTHLY", daysOfWeek = listOf("MONDAY"), weeksOfMonth = listOf(1), daysOfMonth = listOf(-1), monthsOfYear = listOf(1), skipCount = 2)
    }

    @Test
    fun `accepts yearly recurrence type`() {
        val value = RecurringTransactionFixtures.recurringTransaction(recurrenceType = "YEARLY")

        assertEquals("YEARLY", value.recurrenceType)
    }

    @Test
    fun `accepts negative selector values to count from the end`() {
        RecurringTransactionFixtures.recurringTransaction(weeksOfMonth = listOf(-1, -5))
        RecurringTransactionFixtures.recurringTransaction(daysOfMonth = listOf(-1, -31))
    }

    @Test
    fun `rejects every invalid selector value`() {
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(daysOfWeek = listOf("FUNDAY"))
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(weeksOfMonth = listOf(0))
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(weeksOfMonth = listOf(6))
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(weeksOfMonth = listOf(-6))
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(daysOfMonth = listOf(0))
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(daysOfMonth = listOf(32))
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(daysOfMonth = listOf(-32))
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(monthsOfYear = listOf(0))
        }
        assertFailsWith<ValidationException> {
            RecurringTransactionFixtures.recurringTransaction(monthsOfYear = listOf(13))
        }
    }
}
