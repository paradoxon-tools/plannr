package de.chennemann.plannr.server.transactions.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.events.ApplicationEventBus
import de.chennemann.plannr.server.currencies.service.CurrencyService
import de.chennemann.plannr.server.transactions.domain.TransactionRecord
import de.chennemann.plannr.server.transactions.domain.TransactionRepository
import de.chennemann.plannr.server.transactions.events.TransactionUpdated
import de.chennemann.plannr.server.transactions.persistence.toModel
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

interface UpdateTransaction {
    suspend operator fun invoke(command: Command): TransactionRecord

    data class Command(
        val id: String,
        val type: String,
        val status: String,
        val transactionDate: String,
        val amount: Long,
        val currencyCode: String,
        val exchangeRate: String?,
        val destinationAmount: Long?,
        val description: String,
        val partnerId: String?,
        val sourcePocketId: String?,
        val destinationPocketId: String?,
    )
}

@Component
@Transactional
internal class UpdateTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    private val currencyService: CurrencyService,
    private val contextResolver: TransactionContextResolver,
    private val applicationEventBus: ApplicationEventBus,
) : UpdateTransaction {
    override suspend fun invoke(command: UpdateTransaction.Command): TransactionRecord {
        val existing = transactionRepository.findById(command.id.trim())
            ?: throw NotFoundException("not_found", "Transaction not found", mapOf("id" to command.id.trim()))
        val currency = currencyService.ensureExists(command.currencyCode)
        val context = contextResolver.resolve(
            sourcePocketId = command.sourcePocketId,
            destinationPocketId = command.destinationPocketId,
            partnerId = command.partnerId,
            transactionType = command.type,
            currencyCode = currency.code,
        )
        val updated = TransactionRecord(
            id = existing.id,
            accountId = context.accountId,
            type = command.type,
            status = command.status,
            transactionDate = command.transactionDate,
            amount = command.amount,
            currencyCode = currency.code,
            exchangeRate = command.exchangeRate,
            destinationAmount = command.destinationAmount,
            description = command.description,
            partnerId = context.partnerId,
            pocketId = context.pocketId,
            sourcePocketId = context.sourcePocketId,
            destinationPocketId = context.destinationPocketId,
            parentTransactionId = existing.parentTransactionId,
            recurringTransactionId = existing.recurringTransactionId,
            modifiedById = existing.modifiedById,
            transactionOrigin = existing.transactionOrigin,
            isArchived = existing.isArchived,
            createdAt = existing.createdAt,
        )
        val persisted = transactionRepository.update(updated.toModel())
        applicationEventBus.publish(TransactionUpdated(existing, persisted))
        return persisted
    }
}
