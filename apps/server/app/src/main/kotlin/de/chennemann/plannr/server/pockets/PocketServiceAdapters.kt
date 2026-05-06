package de.chennemann.plannr.server.pockets

import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.pockets.service.PocketAccountLookup
import org.springframework.stereotype.Component

@Component
internal class RepositoryPocketAccountLookup(
    private val accountRepository: AccountRepository,
) : PocketAccountLookup {
    override suspend fun exists(accountId: Long): Boolean =
        accountRepository.findById(accountId) != null
}

