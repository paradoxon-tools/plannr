package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.common.error.NotFoundException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateAccountTest {
    @Test
    fun `updates existing account when currency already exists`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        accountRepository.save(AccountFixtures.account())
        val accountService = accountService(
            accountRepository = accountRepository,
            currencyService = FakeCurrencyService(),
        )

        val updated = accountService.update(
            AccountFixtures.updateAccountCommand(
                id = AccountFixtures.DEFAULT_ID,
                name = "Updated Account",
                institution = "Updated Bank",
                currencyCode = "eur",
                weekendHandling = "NO_SHIFT",
            ),
        )

        assertEquals(AccountFixtures.DEFAULT_ID, updated.id)
        assertEquals("Updated Account", updated.name)
        assertEquals("Updated Bank", updated.institution)
        assertEquals("EUR", updated.currencyCode)
        assertEquals("NO_SHIFT", updated.weekendHandling)
        assertEquals(AccountFixtures.DEFAULT_CREATED_AT, updated.createdAt)
        assertEquals(updated, accountRepository.findById(AccountFixtures.DEFAULT_ID))
    }

    @Test
    fun `updates account and persists built in currency when missing`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        val currencyService = FakeCurrencyService(
            initialCurrencies = emptyList(),
            templates = mapOf("USD" to TestCurrencies.usd()),
        )
        accountRepository.save(AccountFixtures.account())
        val accountService = accountService(
            accountRepository = accountRepository,
            currencyService = currencyService,
        )

        val updated = accountService.update(
            AccountFixtures.updateAccountCommand(currencyCode = "usd"),
        )

        assertEquals("USD", updated.currencyCode)
        assertEquals("USD", currencyService.findByCode("USD")?.code)
    }

    @Test
    fun `returns not found when account does not exist`() = runTest {
        val accountService = accountService(
            accountRepository = InMemoryAccountRepository(),
            currencyService = FakeCurrencyService(),
        )

        assertFailsWith<NotFoundException> {
            accountService.update(AccountFixtures.updateAccountCommand(id = "acc_missing"))
        }
    }

    @Test
    fun `returns not found when updated currency is unknown everywhere`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        accountRepository.save(AccountFixtures.account())
        val accountService = accountService(
            accountRepository = accountRepository,
            currencyService = FakeCurrencyService(initialCurrencies = emptyList()),
        )

        assertFailsWith<NotFoundException> {
            accountService.update(AccountFixtures.updateAccountCommand(currencyCode = "xyz"))
        }
    }
}
