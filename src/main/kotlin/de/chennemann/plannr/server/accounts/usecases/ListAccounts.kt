package de.chennemann.plannr.server.accounts.usecases

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import org.springframework.stereotype.Component

interface ListAccounts {
    suspend operator fun invoke(): List<Account>
}

@Component
internal class ListAccountsUseCase(
    private val accountRepository: AccountRepository,
) : ListAccounts {
    override suspend fun invoke(): List<Account> = accountRepository.findAll()
}
