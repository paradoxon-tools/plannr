package de.chennemann.plannr.server.accounts.application

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import org.springframework.stereotype.Service

@Service
class ListAccounts(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(): List<Account> = accountRepository.findAll()
}
