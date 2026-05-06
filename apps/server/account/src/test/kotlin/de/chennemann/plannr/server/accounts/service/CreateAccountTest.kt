package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.persistence.toDomain
import de.chennemann.plannr.server.accounts.persistence.toModel
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.common.error.ConflictException
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
    fun `creates account when same name exists for different institution`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        accountRepository.save(AccountFixtures.account(name = "Main Account", institution = "Demo Bank").toModel())
        val accountService = accountService(accountRepository = accountRepository)

        val created = accountService.create(
            AccountFixtures.createAccountCommand(
                name = "Main Account",
                institution = "Other Bank",
            ),
        )

        assertEquals("Main Account", created.name)
        assertEquals("Other Bank", created.institution)
        assertEquals(2, accountRepository.count())
    }

    @Test
    fun `fails when account with same name already exists for institution`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        accountRepository.save(AccountFixtures.account().toModel())
        val accountService = accountService(accountRepository = accountRepository)

        val exception = assertFailsWith<ConflictException> {
            accountService.create(
                AccountFixtures.createAccountCommand(
                    name = AccountFixtures.DEFAULT_NAME,
                    institution = AccountFixtures.DEFAULT_INSTITUTION,
                ),
            )
        }

        assertEquals("conflict", exception.code)
        assertEquals(
            mapOf("name" to AccountFixtures.DEFAULT_NAME, "institution" to AccountFixtures.DEFAULT_INSTITUTION),
            exception.details,
        )
        assertEquals(1, accountRepository.count())
    }

    @Test
    fun `fails when currency is unsupported`() = runTest {
        val accountService = accountService(accountRepository = InMemoryAccountRepository())

        assertFailsWith<ValidationException> {
            accountService.create(AccountFixtures.createAccountCommand(currencyCode = "xyz"))
        }
    }
}
