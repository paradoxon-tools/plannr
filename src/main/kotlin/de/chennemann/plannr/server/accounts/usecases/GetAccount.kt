package de.chennemann.plannr.server.accounts.usecases

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.common.error.NotFoundException
import org.springframework.stereotype.Component

interface GetAccount {
    suspend operator fun invoke(id: String): Account
}

@Component
internal class GetAccountUseCase(
    private val accountRepository: AccountRepository,
) : GetAccount {
    override suspend fun invoke(id: String): Account =
        accountRepository.findById(id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Account not found",
                details = mapOf("id" to id.trim()),
            )
}
