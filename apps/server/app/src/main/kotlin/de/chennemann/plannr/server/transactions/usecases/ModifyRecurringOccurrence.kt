package de.chennemann.plannr.server.transactions.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.common.events.ApplicationEventBus
import de.chennemann.plannr.server.common.events.NoOpApplicationEventBus
import de.chennemann.plannr.server.currencies.service.CurrencyService
import de.chennemann.plannr.server.transactions.domain.TransactionRecord
import de.chennemann.plannr.server.transactions.domain.TransactionRepository
import de.chennemann.plannr.server.transactions.events.TransactionCreated
import de.chennemann.plannr.server.transactions.events.TransactionUpdated
import de.chennemann.plannr.server.transactions.persistence.TransactionModel
import de.chennemann.plannr.server.transactions.persistence.toModel
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

interface ModifyRecurringOccurrence {
    suspend operator fun invoke(command: Command): TransactionRecord

    data class Command(
        val transactionId: String,
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
internal class ModifyRecurringOccurrenceUseCase(
    private val transactionRepository: TransactionRepository,
    private val currencyService: CurrencyService,
    private val contextResolver: TransactionContextResolver,
    private val applicationEventBus: ApplicationEventBus = NoOpApplicationEventBus,
) : ModifyRecurringOccurrence {
    override suspend fun invoke(command: ModifyRecurringOccurrence.Command): TransactionRecord {
        val existing = transactionRepository.findById(command.transactionId.trim())
            ?: throw NotFoundException("not_found", "Transaction not found", mapOf("id" to command.transactionId.trim()))
        validateModifiableOccurrence(existing)

        val currency = currencyService.ensureExists(command.currencyCode)
        val context = contextResolver.resolve(
            sourcePocketId = command.sourcePocketId,
            destinationPocketId = command.destinationPocketId,
            partnerId = command.partnerId,
            transactionType = command.type,
            currencyCode = currency.code,
        )
        if (context.accountId != existing.accountId) {
            throw ValidationException("validation_error", "Modified recurring occurrence must remain in the same account")
        }

        val persistedChild = transactionRepository.save(
            TransactionModel(
                id = null,
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
                parentTransactionId = existing.id,
                recurringTransactionId = existing.recurringTransactionId,
                modifiedById = null,
                transactionOrigin = "RECURRING_MODIFICATION",
                isArchived = false,
                createdAt = existing.createdAt + 1,
            ),
        )
        val hiddenOriginal = TransactionRecord(
            id = existing.id,
            accountId = existing.accountId,
            type = existing.type,
            status = existing.status,
            transactionDate = existing.transactionDate,
            amount = existing.amount,
            currencyCode = existing.currencyCode,
            exchangeRate = existing.exchangeRate,
            destinationAmount = existing.destinationAmount,
            description = existing.description,
            partnerId = existing.partnerId,
            pocketId = existing.pocketId,
            sourcePocketId = existing.sourcePocketId,
            destinationPocketId = existing.destinationPocketId,
            parentTransactionId = existing.parentTransactionId,
            recurringTransactionId = existing.recurringTransactionId,
            modifiedById = persistedChild.id,
            transactionOrigin = existing.transactionOrigin,
            isArchived = existing.isArchived,
            createdAt = existing.createdAt,
        )

        transactionRepository.update(hiddenOriginal.toModel())
        applicationEventBus.publish(TransactionUpdated(existing, hiddenOriginal))
        applicationEventBus.publish(TransactionCreated(persistedChild))
        return persistedChild
    }

    private fun validateModifiableOccurrence(existing: TransactionRecord) {
        if (existing.transactionOrigin != "RECURRING_MATERIALIZED") {
            throw ValidationException("validation_error", "Only recurring-materialized root occurrences can be modified")
        }
        if (existing.parentTransactionId != null) {
            throw ValidationException("validation_error", "Only recurring-materialized root occurrences can be modified")
        }
        if (existing.recurringTransactionId == null) {
            throw ValidationException("validation_error", "Only recurring-materialized root occurrences can be modified")
        }
        if (existing.modifiedById != null) {
            throw ValidationException("validation_error", "Recurring occurrence has already been modified")
        }
    }
}
