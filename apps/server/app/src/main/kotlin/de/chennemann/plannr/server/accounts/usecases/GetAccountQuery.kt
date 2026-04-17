package de.chennemann.plannr.server.accounts.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.accounts.domain.AccountQuery
import de.chennemann.plannr.server.accounts.domain.AccountQueryRepository
import org.springframework.stereotype.Component

interface GetAccountQuery {
    suspend operator fun invoke(accountId: String): AccountQuery
}

@Component
internal class GetAccountQueryUseCase(
    private val accountQueryRepository: AccountQueryRepository,
) : GetAccountQuery {
    override suspend fun invoke(accountId: String): AccountQuery =
        accountQueryRepository.findById(accountId.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Account not found",
                details = mapOf("id" to accountId.trim()),
            )
}
