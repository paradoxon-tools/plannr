package de.chennemann.plannr.server.accounts.domain

import de.chennemann.plannr.server.accounts.support.AccountFixtures
import kotlin.test.Test
import kotlin.test.assertEquals

class AccountTest {
    @Test
    fun `keeps currency code and weekend handling unchanged`() {
        val account = AccountFixtures.account(
            currencyCode = " eur ",
            weekendHandling = " MOVE_AFTER ",
        )

        assertEquals(" eur ", account.currencyCode)
        assertEquals(" MOVE_AFTER ", account.weekendHandling)
    }

    @Test
    fun `keeps name and institution unchanged`() {
        val account = AccountFixtures.account(
            name = " Main Account ",
            institution = " Demo Bank ",
        )

        assertEquals(" Main Account ", account.name)
        assertEquals(" Demo Bank ", account.institution)
    }
}
