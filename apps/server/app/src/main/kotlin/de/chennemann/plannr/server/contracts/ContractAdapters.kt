package de.chennemann.plannr.server.contracts

import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.service.ContractRecurringTransactionCascade
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.persistence.toModel
import org.springframework.stereotype.Component

@Component
internal class RepositoryContractRecurringTransactionCascade(
    private val recurringTransactionRepository: RecurringTransactionRepository,
) : ContractRecurringTransactionCascade {
    override suspend fun archiveFor(contract: Contract) {
        recurringTransactionRepository.findByContractId(contract.id)
            .forEach { recurringTransactionRepository.update(it.archive().toModel()) }
    }

    override suspend fun unarchiveFor(contract: Contract) {
        recurringTransactionRepository.findByContractId(contract.id)
            .forEach { recurringTransactionRepository.update(it.unarchive().toModel()) }
    }
}
