package de.chennemann.plannr.server.contracts.usecases

import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.persistence.ContractModel
import de.chennemann.plannr.server.contracts.persistence.toDomain
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

interface ListContractsQuery {
    suspend operator fun invoke(accountId: String? = null, archived: Boolean = false): List<Contract>
}

@Component
internal class ListContractsQueryUseCase(
    private val contractRepository: ContractRepository,
) : ListContractsQuery {
    override suspend fun invoke(accountId: String?, archived: Boolean): List<Contract> =
        contractRepository.findAllByAccountIdAndArchived(accountId?.trim()?.takeIf { it.isNotBlank() }, archived)
            .toList()
            .map(ContractModel::toDomain)
}
