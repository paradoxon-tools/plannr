package de.chennemann.plannr.server.contracts.domain

import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ContractTest {
    @Test
    fun `trims fields and normalizes blank optional values to null`() {
        val contract = ContractFixtures.contract(
            accountId = 1L,
            pocketId = 1L,
            partnerId = null,
            name = " Internet Contract ",
            startDate = "2024-01-01",
            endDate = "   ",
            notes = " 12 month term ",
        )

        assertEquals(1L, contract.id)
        assertEquals(1L, contract.accountId)
        assertEquals(1L, contract.pocketId)
        assertNull(contract.partnerId)
        assertEquals("Internet Contract", contract.name)
        assertNull(contract.endDate)
        assertEquals("12 month term", contract.notes)
    }

    @Test
    fun `rejects invalid dates`() {
        assertFailsWith<ValidationException> {
            ContractFixtures.contract(startDate = "2024/01/01")
        }
        assertFailsWith<ValidationException> {
            ContractFixtures.contract(endDate = "2024/12/31")
        }
    }

    @Test
    fun `rejects end date before start date`() {
        assertFailsWith<ValidationException> {
            ContractFixtures.contract(startDate = "2024-01-02", endDate = "2024-01-01")
        }
    }

    @Test
    fun `rejects blank required fields`() {
        assertFailsWith<ValidationException> { ContractFixtures.contract(name = "   ") }
        assertFailsWith<ValidationException> { ContractFixtures.contract(startDate = "   ") }
    }
}
