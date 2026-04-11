package de.chennemann.plannr.server.accounts.usecases

import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.currencies.support.InMemoryCurrencyTemplateCatalog
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.currencies.usecases.EnsureCurrencyExistsUseCase
import de.chennemann.plannr.server.currencies.support.CurrencyFixtures
import de.chennemann.plannr.server.currencies.support.InMemoryCurrencyRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateAccountTest {
    @Test
    fun `updates existing account when currency already exists`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        val currencyRepository = InMemoryCurrencyRepository()
        currencyRepository.save(CurrencyFixtures.currency())
        accountRepository.save(AccountFixtures.account())
        val updateAccount = UpdateAccountUseCase(
            accountRepository = accountRepository,
            ensureCurrencyExists = EnsureCurrencyExistsUseCase(
                currencyRepository = currencyRepository,
                currencyTemplateCatalog = InMemoryCurrencyTemplateCatalog(),
            ),
        )

        val updated = updateAccount(
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
        val currencyRepository = InMemoryCurrencyRepository()
        accountRepository.save(AccountFixtures.account())
        val updateAccount = UpdateAccountUseCase(
            accountRepository = accountRepository,
            ensureCurrencyExists = EnsureCurrencyExistsUseCase(
                currencyRepository = currencyRepository,
                currencyTemplateCatalog = InMemoryCurrencyTemplateCatalog(
                    mapOf("USD" to CurrencyFixtures.currency(code = "USD", name = "US Dollar", symbol = "$")),
                ),
            ),
        )

        val updated = updateAccount(
            AccountFixtures.updateAccountCommand(currencyCode = "usd"),
        )

        assertEquals("USD", updated.currencyCode)
        assertEquals("USD", currencyRepository.findByCode("USD")?.code)
    }

    @Test
    fun `returns not found when account does not exist`() = runTest {
        val updateAccount = UpdateAccountUseCase(
            accountRepository = InMemoryAccountRepository(),
            ensureCurrencyExists = EnsureCurrencyExistsUseCase(
                currencyRepository = InMemoryCurrencyRepository(),
                currencyTemplateCatalog = InMemoryCurrencyTemplateCatalog(),
            ),
        )

        assertFailsWith<NotFoundException> {
            updateAccount(AccountFixtures.updateAccountCommand(id = "acc_missing"))
        }
    }

    @Test
    fun `returns not found when updated currency is unknown everywhere`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        accountRepository.save(AccountFixtures.account())
        val updateAccount = UpdateAccountUseCase(
            accountRepository = accountRepository,
            ensureCurrencyExists = EnsureCurrencyExistsUseCase(
                currencyRepository = InMemoryCurrencyRepository(),
                currencyTemplateCatalog = InMemoryCurrencyTemplateCatalog(),
            ),
        )

        assertFailsWith<NotFoundException> {
            updateAccount(AccountFixtures.updateAccountCommand(currencyCode = "xyz"))
        }
    }
}
