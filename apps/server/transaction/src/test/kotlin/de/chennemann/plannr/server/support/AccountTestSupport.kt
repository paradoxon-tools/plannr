package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.service.AccountService
import de.chennemann.plannr.server.accounts.service.CreateAccountCommand
import de.chennemann.plannr.server.accounts.service.UpdateAccountCommand
import de.chennemann.plannr.server.common.error.NotFoundException

class FakeAccountService(
    initialAccounts: Iterable<Account> = listOf(Account("acc_123", "Main Account", "Demo Bank", "EUR", "MOVE_AFTER", false, 1_710_000_000L)),
) : AccountService {
    private val accounts = initialAccounts.associateByTo(linkedMapOf()) { it.id }

    override suspend fun create(command: CreateAccountCommand): Account = throw UnsupportedOperationException("Not used")
    override suspend fun update(command: UpdateAccountCommand): Account = throw UnsupportedOperationException("Not used")
    override suspend fun archive(id: String): Account = throw UnsupportedOperationException("Not used")
    override suspend fun unarchive(id: String): Account = throw UnsupportedOperationException("Not used")
    override suspend fun list(archived: Boolean?): List<Account> = accounts.values.toList()
    override suspend fun getById(id: String): Account? = accounts[id.trim()]
}
