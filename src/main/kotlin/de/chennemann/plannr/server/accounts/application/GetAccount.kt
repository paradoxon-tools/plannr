package de.chennemann.plannr.server.accounts.application

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.common.error.NotFoundException
import org.springframework.stereotype.Service

@Service
class GetAccount(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(id: String): Account =
        accountRepository.findById(id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Account not found",
                details = mapOf("id" to id.trim()),
            )
}
