package de.chennemann.plannr.server.contracts.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import org.springframework.stereotype.Component

interface GetContract {
    suspend operator fun invoke(id: String): Contract
}

@Component
internal class GetContractUseCase(
    private val contractRepository: ContractRepository,
) : GetContract {
    override suspend fun invoke(id: String): Contract =
        contractRepository.findById(id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Contract not found",
                details = mapOf("id" to id.trim()),
            )
}
