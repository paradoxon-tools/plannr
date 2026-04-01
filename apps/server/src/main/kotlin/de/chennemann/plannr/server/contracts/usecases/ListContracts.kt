package de.chennemann.plannr.server.contracts.usecases

import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import org.springframework.stereotype.Component

interface ListContracts {
    suspend operator fun invoke(accountId: String? = null, archived: Boolean = false): List<Contract>
}

@Component
internal class ListContractsUseCase(
    private val contractRepository: ContractRepository,
) : ListContracts {
    override suspend fun invoke(accountId: String?, archived: Boolean): List<Contract> =
        contractRepository.findAll(accountId = accountId?.trim(), archived = archived)
}
