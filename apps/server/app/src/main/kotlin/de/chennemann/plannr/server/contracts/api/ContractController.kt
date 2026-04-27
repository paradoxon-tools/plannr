package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.contracts.api.dto.ContractResponse
import de.chennemann.plannr.server.contracts.api.dto.CreateContractRequest
import de.chennemann.plannr.server.contracts.api.dto.UpdateContractRequest
import de.chennemann.plannr.server.contracts.usecases.ArchiveContract
import de.chennemann.plannr.server.contracts.usecases.CreateContract
import de.chennemann.plannr.server.contracts.usecases.ListContractsQuery
import de.chennemann.plannr.server.contracts.usecases.UnarchiveContract
import de.chennemann.plannr.server.contracts.usecases.UpdateContract
import de.chennemann.plannr.server.transactions.api.dto.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.PocketTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.toResponse
import de.chennemann.plannr.server.transactions.usecases.ListContractFutureTransactionFeed
import de.chennemann.plannr.server.transactions.usecases.ListContractHistoricalTransactionFeed
import org.springframework.web.bind.annotation.RestController

@RestController
class ContractController(
    private val createContract: CreateContract,
    private val updateContract: UpdateContract,
    private val archiveContract: ArchiveContract,
    private val unarchiveContract: UnarchiveContract,
    private val listContractsQuery: ListContractsQuery,
    private val listContractHistoricalTransactionFeed: ListContractHistoricalTransactionFeed,
    private val listContractFutureTransactionFeed: ListContractFutureTransactionFeed,
) : ContractApi {
    override suspend fun create(request: CreateContractRequest): ContractResponse =
        createContract(request.toCommand()).toResponse()

    override suspend fun update(id: String, request: UpdateContractRequest): ContractResponse =
        updateContract(request.toCommand(id)).toResponse()

    override suspend fun archive(id: String): ContractResponse =
        archiveContract(id).toResponse()

    override suspend fun unarchive(id: String): ContractResponse =
        unarchiveContract(id).toResponse()

    override suspend fun list(accountId: String?, archived: Boolean): List<ContractResponse> =
        listContractsQuery(accountId, archived).map { it.toResponse() }

    override suspend fun listTransactions(
        id: String,
        limit: Int,
        before: Long?,
    ): PocketTransactionFeedPageResponse =
        listContractHistoricalTransactionFeed(id, before, limit).toResponse()

    override suspend fun listFutureTransactions(
        id: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): PocketFutureTransactionFeedPageResponse =
        listContractFutureTransactionFeed(id, fromDate, toDate, after, limit).toResponse()
}
