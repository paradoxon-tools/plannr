package de.chennemann.plannr.server.accounts.usecases

import de.chennemann.plannr.server.accounts.domain.AccountQuery
import de.chennemann.plannr.server.accounts.domain.AccountQueryRepository
import org.springframework.stereotype.Component

interface ListAccountQueries {
    suspend operator fun invoke(archived: Boolean = false): List<AccountQuery>
}

@Component
internal class ListAccountQueriesUseCase(
    private val accountQueryRepository: AccountQueryRepository,
) : ListAccountQueries {
    override suspend fun invoke(archived: Boolean): List<AccountQuery> =
        accountQueryRepository.findAll(archived)
}
