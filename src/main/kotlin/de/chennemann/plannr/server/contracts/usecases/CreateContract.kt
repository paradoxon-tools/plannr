package de.chennemann.plannr.server.contracts.usecases

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.support.ContractIdGenerator
import de.chennemann.plannr.server.partners.usecases.GetPartner
import de.chennemann.plannr.server.pockets.usecases.GetPocket
import org.springframework.stereotype.Component

interface CreateContract {
    suspend operator fun invoke(command: Command): Contract

    data class Command(
        val pocketId: String,
        val partnerId: String?,
        val name: String,
        val startDate: String,
        val endDate: String?,
        val notes: String?,
    )
}

@Component
internal class CreateContractUseCase(
    private val contractRepository: ContractRepository,
    private val getPocket: GetPocket,
    private val getPartner: GetPartner,
    private val contractIdGenerator: ContractIdGenerator,
    private val timeProvider: TimeProvider,
) : CreateContract {
    override suspend fun invoke(command: CreateContract.Command): Contract {
        val pocket = getPocket(command.pocketId)
        if (contractRepository.findByPocketId(pocket.id) != null) {
            throw ConflictException(
                code = "conflict",
                message = "Contract already exists for pocket",
                details = mapOf("pocketId" to pocket.id),
            )
        }

        val partnerId = command.partnerId?.trim()?.takeIf { it.isNotBlank() }?.let { getPartner(it).id }
        val contract = Contract(
            id = contractIdGenerator(),
            accountId = pocket.accountId,
            pocketId = pocket.id,
            partnerId = partnerId,
            name = command.name,
            startDate = command.startDate,
            endDate = command.endDate,
            notes = command.notes,
            isArchived = false,
            createdAt = timeProvider(),
        )

        return contractRepository.save(contract)
    }
}
