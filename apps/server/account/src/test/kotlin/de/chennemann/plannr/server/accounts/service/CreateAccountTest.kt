package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.common.error.NotFoundException
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

        assertEquals(AccountFixtures.DEFAULT_ID, created.id)
        assertEquals(AccountFixtures.DEFAULT_CURRENCY_CODE, created.currencyCode)
        assertEquals(created, accountRepository.findById(AccountFixtures.DEFAULT_ID))
    }

    @Test
    fun `creates account and persists built in currency when missing`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        val currencyService = FakeCurrencyService(
            initialCurrencies = emptyList(),
            templates = mapOf("EUR" to TestCurrencies.eur()),
        )
        val accountService = accountService(
            accountRepository = accountRepository,
            currencyService = currencyService,
        )

        val created = accountService.create(AccountFixtures.createAccountCommand(currencyCode = "eur"))

        assertEquals(AccountFixtures.DEFAULT_CURRENCY_CODE, created.currencyCode)
        assertEquals(TestCurrencies.eur(), currencyService.findByCode("EUR"))
    }

    @Test
    fun `fails when currency exists in neither database nor templates`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        val accountService = accountService(
            accountRepository = accountRepository,
            currencyService = FakeCurrencyService(initialCurrencies = emptyList()),
        )

        assertFailsWith<NotFoundException> {
            accountService.create(AccountFixtures.createAccountCommand(currencyCode = "xyz"))
        }
    }
}
