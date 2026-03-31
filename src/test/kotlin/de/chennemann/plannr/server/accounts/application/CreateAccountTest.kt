package de.chennemann.plannr.server.accounts.application

import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.accounts.support.InMemoryCurrencyTemplateCatalog
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.currencies.support.CurrencyFixtures
import de.chennemann.plannr.server.currencies.support.InMemoryCurrencyRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreateAccountTest {
    @Test
    fun `creates account when currency already exists`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        val currencyRepository = InMemoryCurrencyRepository()
        currencyRepository.save(CurrencyFixtures.currency())
        val createAccount = CreateAccount(
            accountRepository = accountRepository,
            ensureCurrencyExists = EnsureCurrencyExists(
                currencyRepository = currencyRepository,
                currencyTemplateCatalog = InMemoryCurrencyTemplateCatalog(),
            ),
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
        val currencyRepository = InMemoryCurrencyRepository()
        val createAccount = CreateAccount(
            accountRepository = accountRepository,
            ensureCurrencyExists = EnsureCurrencyExists(
                currencyRepository = currencyRepository,
                currencyTemplateCatalog = InMemoryCurrencyTemplateCatalog(
                    mapOf("EUR" to CurrencyFixtures.currency()),
                ),
            ),
            accountIdGenerator = { AccountFixtures.DEFAULT_ID },
            timeProvider = { AccountFixtures.DEFAULT_CREATED_AT },
        )

        val created = createAccount(AccountFixtures.createAccountCommand(currencyCode = "eur"))

        assertEquals(AccountFixtures.DEFAULT_CURRENCY_CODE, created.currencyCode)
        assertEquals(CurrencyFixtures.currency(), currencyRepository.findByCode("EUR"))
    }

    @Test
    fun `fails when currency exists in neither database nor templates`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        val currencyRepository = InMemoryCurrencyRepository()
        val createAccount = CreateAccount(
            accountRepository = accountRepository,
            ensureCurrencyExists = EnsureCurrencyExists(
                currencyRepository = currencyRepository,
                currencyTemplateCatalog = InMemoryCurrencyTemplateCatalog(),
            ),
            accountIdGenerator = { AccountFixtures.DEFAULT_ID },
            timeProvider = { AccountFixtures.DEFAULT_CREATED_AT },
        )

        assertFailsWith<NotFoundException> {
            createAccount(AccountFixtures.createAccountCommand(currencyCode = "xyz"))
        }
    }
}
