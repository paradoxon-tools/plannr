package de.chennemann.plannr.server.accounts.usecases

import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.support.FakeCurrencyService
import de.chennemann.plannr.server.support.TestCurrencies
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreateAccountTest {
    @Test
    fun `creates account when currency already exists`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        val createAccount = CreateAccountUseCase(
            accountRepository = accountRepository,
            currencyService = FakeCurrencyService(),
            accountIdGenerator = { AccountFixtures.DEFAULT_ID },
            timeProvider = { AccountFixtures.DEFAULT_CREATED_AT },
        )

        val created = createAccount(AccountFixtures.createAccountCommand())

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
        val createAccount = CreateAccountUseCase(
            accountRepository = accountRepository,
            currencyService = currencyService,
            accountIdGenerator = { AccountFixtures.DEFAULT_ID },
            timeProvider = { AccountFixtures.DEFAULT_CREATED_AT },
        )

        val created = createAccount(AccountFixtures.createAccountCommand(currencyCode = "eur"))

        assertEquals(AccountFixtures.DEFAULT_CURRENCY_CODE, created.currencyCode)
        assertEquals(TestCurrencies.eur(), currencyService.findByCode("EUR"))
    }

    @Test
    fun `fails when currency exists in neither database nor templates`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        val createAccount = CreateAccountUseCase(
            accountRepository = accountRepository,
            currencyService = FakeCurrencyService(initialCurrencies = emptyList()),
            accountIdGenerator = { AccountFixtures.DEFAULT_ID },
            timeProvider = { AccountFixtures.DEFAULT_CREATED_AT },
        )

        assertFailsWith<NotFoundException> {
            createAccount(AccountFixtures.createAccountCommand(currencyCode = "xyz"))
        }
    }
}
