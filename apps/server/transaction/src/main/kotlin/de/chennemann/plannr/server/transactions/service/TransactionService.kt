package de.chennemann.plannr.server.transactions.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.common.events.ApplicationEventBus
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.currencies.service.CurrencyService
import de.chennemann.plannr.server.transactions.domain.TransactionRecord
import de.chennemann.plannr.server.transactions.domain.TransactionRepository
import de.chennemann.plannr.server.transactions.events.TransactionArchived
import de.chennemann.plannr.server.transactions.events.TransactionCreated
import de.chennemann.plannr.server.transactions.events.TransactionUnarchived
import de.chennemann.plannr.server.transactions.events.TransactionUpdated
import de.chennemann.plannr.server.transactions.persistence.TransactionModel
import de.chennemann.plannr.server.transactions.persistence.toModel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val currencyService: CurrencyService,
    private val contextResolver: TransactionContextResolver,
    private val timeProvider: TimeProvider,
    private val applicationEventBus: ApplicationEventBus,
) {
    @Transactional
    suspend fun create(command: CreateCommand): TransactionRecord {
        val currency = currencyService.ensureExists(command.currencyCode)
        val context = contextResolver.resolve(
            sourcePocketId = command.sourcePocketId,
            destinationPocketId = command.destinationPocketId,
            partnerId = command.partnerId,
            transactionType = command.type,
            currencyCode = currency.code,
        )
        val created = transactionRepository.save(
            TransactionModel(
                id = null,
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
                parentTransactionId = null,
                recurringTransactionId = null,
                modifiedById = null,
                transactionOrigin = "MANUAL",
                isArchived = false,
                createdAt = timeProvider(),
            ),
        )
        applicationEventBus.publish(TransactionCreated(created))
        return created
    }

    @Transactional
    suspend fun update(command: UpdateCommand): TransactionRecord {
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

    @Transactional
    suspend fun modifyRecurringOccurrence(command: ModifyRecurringOccurrenceCommand): TransactionRecord {
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
                accountId = existing.accountId,
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

    @Transactional
    suspend fun archive(id: String): TransactionRecord {
        val existing = transactionRepository.findById(id.trim())
            ?: throw NotFoundException("not_found", "Transaction not found", mapOf("id" to id.trim()))
        val updated = transactionRepository.update(existing.archive().toModel())
        applicationEventBus.publish(TransactionArchived(existing, updated))
        return updated
    }

    @Transactional
    suspend fun unarchive(id: String): TransactionRecord {
        val existing = transactionRepository.findById(id.trim())
            ?: throw NotFoundException("not_found", "Transaction not found", mapOf("id" to id.trim()))
        val updated = transactionRepository.update(existing.unarchive().toModel())
        applicationEventBus.publish(TransactionUnarchived(existing, updated))
        return updated
    }

    suspend fun list(accountId: String? = null, pocketId: String? = null, archived: Boolean = false): List<TransactionRecord> =
        transactionRepository.findAll(
            accountId = accountId?.trim()?.takeIf { it.isNotBlank() },
            pocketId = pocketId?.trim()?.takeIf { it.isNotBlank() },
            archived = archived,
        )

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

    data class CreateCommand(
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

    data class UpdateCommand(
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

    data class ModifyRecurringOccurrenceCommand(
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
