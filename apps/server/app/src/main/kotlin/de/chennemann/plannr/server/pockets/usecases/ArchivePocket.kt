package de.chennemann.plannr.server.pockets.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.events.ApplicationEventBus
import de.chennemann.plannr.server.common.events.NoOpApplicationEventBus
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.events.PocketUpdated
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransactionRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

interface ArchivePocket {
    suspend operator fun invoke(id: String): Pocket
}

@Component
@Transactional
internal class ArchivePocketUseCase(
    private val pocketRepository: PocketRepository,
    private val contractRepository: ContractRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val applicationEventBus: ApplicationEventBus = NoOpApplicationEventBus,
) : ArchivePocket {
    override suspend fun invoke(id: String): Pocket {
        val existing = pocketRepository.findById(id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Pocket not found",
                details = mapOf("id" to id.trim()),
            )

        val updated = pocketRepository.update(existing.archive())
        contractRepository.findByPocketId(updated.id)?.let { contractRepository.update(it.archive()) }
        recurringTransactionRepository.findAll(accountId = updated.accountId, archived = false)
            .filter { it.sourcePocketId == updated.id || it.destinationPocketId == updated.id }
            .forEach { recurringTransactionRepository.update(it.archive()) }
        applicationEventBus.publish(PocketUpdated(existing, updated))
        return updated
    }
}
