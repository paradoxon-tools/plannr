package de.chennemann.plannr.server.transactions.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.events.ApplicationEventBus
import de.chennemann.plannr.server.common.events.NoOpApplicationEventBus
import de.chennemann.plannr.server.transactions.domain.TransactionRecord
import de.chennemann.plannr.server.transactions.domain.TransactionRepository
import de.chennemann.plannr.server.transactions.events.TransactionUnarchived
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

interface UnarchiveTransaction {
    suspend operator fun invoke(id: String): TransactionRecord
}

@Component
@Transactional
internal class UnarchiveTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    private val applicationEventBus: ApplicationEventBus = NoOpApplicationEventBus,
) : UnarchiveTransaction {
    override suspend fun invoke(id: String): TransactionRecord {
        val existing = transactionRepository.findById(id.trim())
            ?: throw NotFoundException("not_found", "Transaction not found", mapOf("id" to id.trim()))
        val updated = transactionRepository.update(existing.unarchive())
        applicationEventBus.publish(TransactionUnarchived(existing, updated))
        return updated
    }
}
