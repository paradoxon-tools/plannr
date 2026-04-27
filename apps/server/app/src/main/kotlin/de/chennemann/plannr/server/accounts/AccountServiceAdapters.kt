package de.chennemann.plannr.server.accounts

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.service.AccountArchiveCascade
import de.chennemann.plannr.server.accounts.service.AccountBalanceProvider
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.persistence.toModel
import de.chennemann.plannr.server.transactions.usecases.CurrentBalanceCalculator
import org.springframework.stereotype.Component

@Component
internal class RepositoryAccountArchiveCascade(
    private val pocketService: PocketService,
    private val recurringTransactionRepository: RecurringTransactionRepository,
) : AccountArchiveCascade {
    override suspend fun archiveFor(account: Account) {
        pocketService.list(accountId = account.id).forEach { pocketService.archive(it.id) }
        recurringTransactionRepository.findAll(accountId = account.id, archived = false)
            .forEach { recurringTransactionRepository.update(it.archive().toModel()) }
    }

    override suspend fun unarchiveFor(account: Account) {
        pocketService.list(accountId = account.id).forEach { pocketService.unarchive(it.id) }
        recurringTransactionRepository.findAll(accountId = account.id, archived = true)
            .forEach { recurringTransactionRepository.update(it.unarchive().toModel()) }
    }
}

@Component
internal class CalculatorAccountBalanceProvider(
    private val currentBalanceCalculator: CurrentBalanceCalculator,
) : AccountBalanceProvider {
    override suspend fun currentBalance(accountId: String): Long =
        currentBalanceCalculator.accountBalance(accountId)
}
