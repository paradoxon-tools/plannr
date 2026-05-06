package de.chennemann.plannr.server.pockets

import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.persistence.toDomain
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.service.PocketAccountLookup
import de.chennemann.plannr.server.pockets.service.PocketArchiveCascade
import org.springframework.stereotype.Component

@Component
internal class RepositoryPocketAccountLookup(
    private val accountRepository: AccountRepository,
) : PocketAccountLookup {
    override suspend fun exists(accountId: Long): Boolean =
        accountRepository.findById(accountId) != null
}

@Component
internal class RepositoryPocketArchiveCascade(
    private val contractRepository: ContractRepository,
) : PocketArchiveCascade {
    override suspend fun archiveFor(pocket: Pocket) {
        contractRepository.findByPocketId(pocket.id)?.toDomain()?.let {
            val updated = it.archive()
            contractRepository.update(
                id = updated.id,
                accountId = updated.accountId,
                pocketId = updated.pocketId,
                partnerId = updated.partnerId,
                name = updated.name,
                startDate = updated.startDate,
                endDate = updated.endDate,
                notes = updated.notes,
                isArchived = true,
            )
        }
    }

    override suspend fun unarchiveFor(pocket: Pocket) {
        contractRepository.findByPocketId(pocket.id)?.toDomain()?.let {
            val updated = it.unarchive()
            contractRepository.update(
                id = updated.id,
                accountId = updated.accountId,
                pocketId = updated.pocketId,
                partnerId = updated.partnerId,
                name = updated.name,
                startDate = updated.startDate,
                endDate = updated.endDate,
                notes = updated.notes,
                isArchived = false,
            )
        }
    }
}

