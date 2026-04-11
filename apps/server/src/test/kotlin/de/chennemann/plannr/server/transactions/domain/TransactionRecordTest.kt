package de.chennemann.plannr.server.transactions.domain

import de.chennemann.plannr.server.common.error.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class TransactionRecordTest {
    @Test
    fun `normalizes canonical enum casing and optional values`() {
        val value = transaction(
            id = " txn_123 ",
            accountId = " acc_123 ",
            type = " expense ",
            status = " cleared ",
            currencyCode = " eur ",
            exchangeRate = " 1.25 ",
            partnerId = "   ",
        )

        assertEquals("txn_123", value.id)
        assertEquals("acc_123", value.accountId)
        assertEquals("EXPENSE", value.type)
        assertEquals("CLEARED", value.status)
        assertEquals("EUR", value.currencyCode)
        assertEquals("1.25", value.exchangeRate)
        assertEquals("poc_123", value.pocketId)
        assertEquals("poc_123", value.sourcePocketId)
        assertNull(value.partnerId)
    }

    @Test
    fun `rejects invalid transaction enums`() {
        assertFailsWith<ValidationException> { transaction(type = "unknown") }
        assertFailsWith<ValidationException> { transaction(status = "booked") }
    }

    @Test
    fun `rejects invalid pocket combinations`() {
        assertFailsWith<ValidationException> { transaction(type = "EXPENSE", sourcePocketId = null, pocketId = null) }
        assertFailsWith<ValidationException> { transaction(type = "INCOME", destinationPocketId = null, sourcePocketId = "poc_123", pocketId = null) }
        assertFailsWith<ValidationException> { transaction(type = "TRANSFER", destinationPocketId = null) }
        assertFailsWith<ValidationException> { transaction(type = "TRANSFER", destinationPocketId = "poc_123") }
        assertFailsWith<ValidationException> { transaction(type = "TRANSFER", pocketId = "poc_123", destinationPocketId = "poc_456", sourcePocketId = "poc_123") }
    }

    @Test
    fun `canonicalizes non transfer pocket scope to pocket id`() {
        val income = transaction(type = "INCOME", pocketId = null, sourcePocketId = null, destinationPocketId = "poc_456")

        assertEquals("poc_456", income.pocketId)
        assertNull(income.sourcePocketId)
        assertEquals("poc_456", income.destinationPocketId)
    }

    private fun transaction(
        id: String = "txn_123",
        accountId: String = "acc_123",
        type: String = "EXPENSE",
        status: String = "CLEARED",
        transactionDate: String = "2026-04-10",
        amount: Long = 100,
        currencyCode: String = "EUR",
        exchangeRate: String? = null,
        destinationAmount: Long? = null,
        description: String = "desc",
        partnerId: String? = null,
        pocketId: String? = null,
        sourcePocketId: String? = "poc_123",
        destinationPocketId: String? = null,
    ): TransactionRecord = TransactionRecord(
        id = id,
        accountId = accountId,
        type = type,
        status = status,
        transactionDate = transactionDate,
        amount = amount,
        currencyCode = currencyCode,
        exchangeRate = exchangeRate,
        destinationAmount = destinationAmount,
        description = description,
        partnerId = partnerId,
        pocketId = pocketId,
        sourcePocketId = sourcePocketId,
        destinationPocketId = destinationPocketId,
        parentTransactionId = null,
        recurringTransactionId = null,
        modifiedById = null,
        transactionOrigin = "MANUAL",
        isArchived = false,
        createdAt = 1L,
    )
}
