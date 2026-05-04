package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.persistence.toDomain
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.common.error.ValidationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreateAccountTest {
    @Test
    fun `creates account when currency already exists`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        val accountService = accountService(accountRepository = accountRepository)

        val created = accountService.create(AccountFixtures.createAccountCommand())

        assertEquals(AccountFixtures.DEFAULT_CURRENCY_CODE, created.currencyCode)
        assertEquals(created, accountRepository.findById(created.id)?.toDomain())
    }

    @Test
    fun `creates account with normalized supported currency`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        val accountService = accountService(accountRepository = accountRepository)

        val created = accountService.create(AccountFixtures.createAccountCommand(currencyCode = "eur"))

        assertEquals(AccountFixtures.DEFAULT_CURRENCY_CODE, created.currencyCode)
    }

    @Test
    fun `fails when currency is unsupported`() = runTest {
        val accountService = accountService(accountRepository = InMemoryAccountRepository())

        assertFailsWith<ValidationException> {
            accountService.create(AccountFixtures.createAccountCommand(currencyCode = "xyz"))
        }
    }
}
