package de.chennemann.plannr.server.pockets

import de.chennemann.plannr.server.accounts.service.AccountService
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.persistence.toModel
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
    private val accountService: AccountService,
) : PocketAccountLookup {
    override suspend fun exists(accountId: String): Boolean =
        accountService.getById(accountId) != null
}

@Component
internal class RepositoryPocketArchiveCascade(
    private val contractRepository: ContractRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
) : PocketArchiveCascade {
    override suspend fun archiveFor(pocket: Pocket) {
        contractRepository.findByPocketId(pocket.id)?.let { contractRepository.update(it.archive().toModel()) }
        recurringTransactionRepository.findAll(accountId = pocket.accountId, archived = false)
            .filter { it.sourcePocketId == pocket.id || it.destinationPocketId == pocket.id }
            .forEach { recurringTransactionRepository.update(it.archive().toModel()) }
    }

    override suspend fun unarchiveFor(pocket: Pocket) {
        contractRepository.findByPocketId(pocket.id)?.let { contractRepository.update(it.unarchive().toModel()) }
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
