package de.chennemann.plannr.server.accounts.usecases

import de.chennemann.plannr.server.accounts.domain.AccountQuery
import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.transactions.usecases.CurrentBalanceCalculator
import org.springframework.stereotype.Component

interface ListAccountQueries {
    suspend operator fun invoke(archived: Boolean = false): List<AccountQuery>
}

@Component
internal class ListAccountQueriesUseCase(
    private val accountRepository: AccountRepository,
    private val currentBalanceCalculator: CurrentBalanceCalculator,
) : ListAccountQueries {
    override suspend fun invoke(archived: Boolean): List<AccountQuery> =
        accountRepository.findAll()
            .filter { it.isArchived == archived }
            .map { it.toQuery(currentBalanceCalculator.accountBalance(it.id)) }
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
