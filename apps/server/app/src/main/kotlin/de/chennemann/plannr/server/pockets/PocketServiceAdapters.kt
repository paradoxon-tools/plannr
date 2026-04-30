package de.chennemann.plannr.server.pockets

import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.persistence.toDomain
import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.service.PocketAccountLookup
import de.chennemann.plannr.server.pockets.service.PocketArchiveCascade
import de.chennemann.plannr.server.pockets.service.PocketBalanceProvider
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.persistence.toModel
import de.chennemann.plannr.server.transactions.usecases.CurrentBalanceCalculator
import org.springframework.stereotype.Component

@Component
internal class RepositoryPocketAccountLookup(
    private val accountRepository: AccountRepository,
) : PocketAccountLookup {
    override suspend fun exists(accountId: String): Boolean =
        accountRepository.existsById(accountId)
}

@Component
internal class RepositoryPocketArchiveCascade(
    private val contractRepository: ContractRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
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
        recurringTransactionRepository.findAll(accountId = pocket.accountId, archived = false)
            .filter { it.sourcePocketId == pocket.id || it.destinationPocketId == pocket.id }
            .forEach { recurringTransactionRepository.update(it.archive().toModel()) }
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
        recurringTransactionRepository.findAll(accountId = pocket.accountId, archived = true)
            .filter { it.sourcePocketId == pocket.id || it.destinationPocketId == pocket.id }
            .forEach { recurringTransactionRepository.update(it.unarchive().toModel()) }
    }
}

@Component
internal class CalculatorPocketBalanceProvider(
    private val currentBalanceCalculator: CurrentBalanceCalculator,
) : PocketBalanceProvider {
    override suspend fun currentBalance(pocketId: String): Long =
        currentBalanceCalculator.pocketBalance(pocketId)
}
