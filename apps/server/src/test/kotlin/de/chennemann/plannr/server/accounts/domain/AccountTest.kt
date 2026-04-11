package de.chennemann.plannr.server.accounts.domain

import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.common.error.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AccountTest {
    @Test
    fun `normalizes currency code and weekend handling`() {
        val account = AccountFixtures.account(
            currencyCode = " eur ",
            weekendHandling = " MOVE_AFTER ",
        )

        assertEquals("EUR", account.currencyCode)
        assertEquals("MOVE_AFTER", account.weekendHandling)
    }

    @Test
    fun `trims name and institution`() {
        val account = AccountFixtures.account(
            name = " Main Account ",
            institution = " Demo Bank ",
        )

        assertEquals("Main Account", account.name)
        assertEquals("Demo Bank", account.institution)
    }

    @Test
    fun `rejects blank id`() {
        assertFailsWith<ValidationException> {
            AccountFixtures.account(id = "   ")
        }
    }

    @Test
    fun `rejects blank name`() {
        assertFailsWith<ValidationException> {
            AccountFixtures.account(name = "   ")
        }
    }

    @Test
    fun `rejects blank institution`() {
        assertFailsWith<ValidationException> {
            AccountFixtures.account(institution = "   ")
        }
    }

    @Test
    fun `rejects blank currency code`() {
        assertFailsWith<ValidationException> {
            AccountFixtures.account(currencyCode = "   ")
        }
    }

    @Test
    fun `rejects blank weekend handling`() {
        assertFailsWith<ValidationException> {
            AccountFixtures.account(weekendHandling = "   ")
        }
    }
}
