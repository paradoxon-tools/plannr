package de.chennemann.plannr.server.accounts.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.accounts.domain.AccountQuery
import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.transactions.usecases.CurrentBalanceCalculator
import org.springframework.stereotype.Component

interface GetAccountQuery {
    suspend operator fun invoke(accountId: String): AccountQuery
}

@Component
internal class GetAccountQueryUseCase(
    private val accountRepository: AccountRepository,
    private val currentBalanceCalculator: CurrentBalanceCalculator,
) : GetAccountQuery {
    override suspend fun invoke(accountId: String): AccountQuery =
        (accountRepository.findById(accountId.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Account not found",
                details = mapOf("id" to accountId.trim()),
            )).toQuery(currentBalanceCalculator.accountBalance(accountId.trim()))
}

private fun Account.toQuery(currentBalance: Long): AccountQuery =
    AccountQuery(
        accountId = id,
        name = name,
        institution = institution,
        currencyCode = currencyCode,
        weekendHandling = weekendHandling,
        isArchived = isArchived,
        createdAt = createdAt,
        currentBalance = currentBalance,
    )
