package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.persistence.toDomain
import de.chennemann.plannr.server.accounts.persistence.toModel
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateAccountTest {
    @Test
    fun `updates existing account when currency already exists`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        accountRepository.save(AccountFixtures.account().toModel())
        val accountService = accountService(accountRepository = accountRepository)

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
        assertEquals(updated, accountRepository.findById(AccountFixtures.DEFAULT_ID)?.toDomain())
    }

    @Test
    fun `updates account with normalized supported currency`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        accountRepository.save(AccountFixtures.account().toModel())
        val accountService = accountService(accountRepository = accountRepository)

        val updated = accountService.update(
            AccountFixtures.updateAccountCommand(currencyCode = "usd"),
        )

        assertEquals("USD", updated.currencyCode)
    }

    @Test
    fun `returns not found when account does not exist`() = runTest {
        val accountService = accountService(accountRepository = InMemoryAccountRepository())

        assertFailsWith<NotFoundException> {
            accountService.update(AccountFixtures.updateAccountCommand(id = "acc_missing"))
        }
    }

    @Test
    fun `returns validation error when updated currency is unsupported`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        accountRepository.save(AccountFixtures.account().toModel())
        val accountService = accountService(accountRepository = accountRepository)

        assertFailsWith<ValidationException> {
            accountService.update(AccountFixtures.updateAccountCommand(currencyCode = "xyz"))
        }
    }
}
