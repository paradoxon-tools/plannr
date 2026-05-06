package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.service.AccountService
import de.chennemann.plannr.server.accounts.api.dto.CreateAccountCommand
import de.chennemann.plannr.server.accounts.api.dto.UpdateAccountCommand
import de.chennemann.plannr.server.common.error.NotFoundException

object TestAccounts {
    const val DEFAULT_ID = 1L
    const val DEFAULT_NAME = "Main Account"
    const val DEFAULT_INSTITUTION = "Demo Bank"
    const val DEFAULT_CURRENCY_CODE = "EUR"
    const val DEFAULT_WEEKEND_HANDLING = "MOVE_AFTER"
    const val DEFAULT_CREATED_AT = 1_710_000_000L

    fun account(
        id: Long = DEFAULT_ID,
        name: String = DEFAULT_NAME,
        institution: String = DEFAULT_INSTITUTION,
        currencyCode: String = DEFAULT_CURRENCY_CODE,
        weekendHandling: String = DEFAULT_WEEKEND_HANDLING,
        isArchived: Boolean = false,
        createdAt: Long = DEFAULT_CREATED_AT,
    ): Account =
        Account(id, name, institution, currencyCode, weekendHandling, isArchived, createdAt)
}

class FakeAccountService(
    initialAccounts: Iterable<Account> = listOf(TestAccounts.account()),
    private val idGenerator: () -> Long = { 999L },
    private val timeProvider: () -> Long = { TestAccounts.DEFAULT_CREATED_AT },
) : AccountService {
    private val accounts = initialAccounts.associateByTo(linkedMapOf()) { it.id }

    override suspend fun create(command: CreateAccountCommand): Account {
        val account = Account(
            id = idGenerator(),
            name = command.name,
            institution = command.institution,
            currencyCode = command.currencyCode,
            weekendHandling = command.weekendHandling,
            isArchived = false,
            createdAt = timeProvider(),
        )
        accounts[account.id] = account
        return account
    }

    override suspend fun update(command: UpdateAccountCommand): Account {
        val existing = existingAccount(command.id)
        val account = Account(
            id = existing.id,
            name = command.name,
            institution = command.institution,
            currencyCode = command.currencyCode,
            weekendHandling = command.weekendHandling,
            isArchived = existing.isArchived,
            createdAt = existing.createdAt,
        )
        accounts[account.id] = account
        return account
    }

    override suspend fun archive(id: Long): Account {
        val account = existingAccount(id).copy(isArchived = true)
        accounts[account.id] = account
        return account
    }

    override suspend fun unarchive(id: Long): Account {
        val account = existingAccount(id).copy(isArchived = false)
        accounts[account.id] = account
        return account
    }

    override suspend fun delete(id: Long) {
        accounts.remove(existingAccount(id).id)
    }

    override suspend fun list(archived: Boolean?): List<Account> =
        accounts.values
            .filter { archived == null || it.isArchived == archived }
            .sortedWith(compareBy<Account> { it.createdAt }.thenBy { it.id })

    override suspend fun getById(id: Long): Account? =
        accounts[id]

    private fun existingAccount(id: Long): Account =
        accounts[id]
            ?: throw NotFoundException("not_found", "Account not found", mapOf("id" to id))
}
