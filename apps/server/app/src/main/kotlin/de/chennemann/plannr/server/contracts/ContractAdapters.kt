package de.chennemann.plannr.server.contracts

import de.chennemann.plannr.server.contracts.api.ContractFutureTransactionFeedQuery
import de.chennemann.plannr.server.contracts.api.ContractHistoricalTransactionFeedQuery
import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.usecases.ContractRecurringTransactionCascade
import de.chennemann.plannr.server.transactions.api.dto.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.PocketTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.toResponse
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.persistence.toModel
import de.chennemann.plannr.server.transactions.usecases.ListContractFutureTransactionFeed
import de.chennemann.plannr.server.transactions.usecases.ListContractHistoricalTransactionFeed
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

@Component
internal class UseCaseContractHistoricalTransactionFeedQuery(
    private val listContractHistoricalTransactionFeed: ListContractHistoricalTransactionFeed,
) : ContractHistoricalTransactionFeedQuery {
    override suspend fun list(
        contractId: String,
        limit: Int,
        before: Long?,
    ): PocketTransactionFeedPageResponse =
        listContractHistoricalTransactionFeed(contractId, before, limit).toResponse()
}

@Component
internal class UseCaseContractFutureTransactionFeedQuery(
    private val listContractFutureTransactionFeed: ListContractFutureTransactionFeed,
) : ContractFutureTransactionFeedQuery {
    override suspend fun list(
        contractId: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): PocketFutureTransactionFeedPageResponse =
        listContractFutureTransactionFeed(contractId, fromDate, toDate, after, limit).toResponse()
}
