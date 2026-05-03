package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.contracts.domain.Contract
import org.springframework.stereotype.Component

@Component
class CreateContract(
    private val contractService: ContractService,
) {
    suspend operator fun invoke(command: Command): Contract =
        contractService.create(command.toServiceCommand())

    data class Command(
        val pocketId: String,
        val partnerId: String?,
        val name: String,
        val startDate: String,
        val endDate: String?,
        val notes: String?,
    ) {
        fun toServiceCommand() = ContractService.CreateCommand(
            pocketId = pocketId,
            partnerId = partnerId,
            name = name,
            startDate = startDate,
            endDate = endDate,
            notes = notes,
        )
    }
}
