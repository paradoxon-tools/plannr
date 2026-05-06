package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.persistence.toDomain
import de.chennemann.plannr.server.accounts.persistence.toModel
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.common.error.ConflictException
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
    fun `updates account when name belongs to same account`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        accountRepository.save(AccountFixtures.account().toModel())
        val accountService = accountService(accountRepository = accountRepository)

        val updated = accountService.update(AccountFixtures.updateAccountCommand(name = AccountFixtures.DEFAULT_NAME))

        assertEquals(AccountFixtures.DEFAULT_NAME, updated.name)
    }

    @Test
    fun `updates account when name already exists for different institution`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        accountRepository.save(AccountFixtures.account(id = 1L, name = "Main Account", institution = "Demo Bank").toModel())
        accountRepository.save(AccountFixtures.account(id = 2L, name = "Savings", institution = "Other Bank").toModel())
        val accountService = accountService(accountRepository = accountRepository)

        val updated = accountService.update(
            AccountFixtures.updateAccountCommand(id = 2L, name = "Main Account", institution = "Other Bank"),
        )

        assertEquals("Main Account", updated.name)
        assertEquals("Other Bank", updated.institution)
    }

    @Test
    fun `fails when updated account name already belongs to another account for institution`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        accountRepository.save(AccountFixtures.account(id = 1L, name = "Main Account", institution = "Demo Bank").toModel())
        accountRepository.save(AccountFixtures.account(id = 2L, name = "Savings", institution = "Demo Bank").toModel())
        val accountService = accountService(accountRepository = accountRepository)

        val exception = assertFailsWith<ConflictException> {
            accountService.update(
                AccountFixtures.updateAccountCommand(id = 2L, name = "Main Account", institution = "Demo Bank"),
            )
        }

        assertEquals("conflict", exception.code)
        assertEquals(mapOf("name" to "Main Account", "institution" to "Demo Bank"), exception.details)
        assertEquals("Savings", accountRepository.findById(2L)?.toDomain()?.name)
    }

    @Test
    fun `returns not found when account does not exist`() = runTest {
        val accountService = accountService(accountRepository = InMemoryAccountRepository())

        assertFailsWith<NotFoundException> {
            accountService.update(AccountFixtures.updateAccountCommand(id = 999L))
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
