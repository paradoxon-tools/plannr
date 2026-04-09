package de.chennemann.plannr.server.transactions.usecases

import de.chennemann.plannr.server.common.events.ApplicationEventBus
import de.chennemann.plannr.server.common.events.NoOpApplicationEventBus
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.currencies.usecases.EnsureCurrencyExists
import de.chennemann.plannr.server.transactions.domain.TransactionRecord
import de.chennemann.plannr.server.transactions.domain.TransactionRepository
import de.chennemann.plannr.server.transactions.events.TransactionCreated
import de.chennemann.plannr.server.transactions.support.TransactionIdGenerator
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

interface CreateTransaction {
    suspend operator fun invoke(command: Command): TransactionRecord

    data class Command(
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
internal class CreateTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    private val ensureCurrencyExists: EnsureCurrencyExists,
    private val contextResolver: TransactionContextResolver,
    private val transactionIdGenerator: TransactionIdGenerator,
    private val timeProvider: TimeProvider,
    private val applicationEventBus: ApplicationEventBus = NoOpApplicationEventBus,
) : CreateTransaction {
    override suspend fun invoke(command: CreateTransaction.Command): TransactionRecord {
        val currency = ensureCurrencyExists(command.currencyCode)
        val context = contextResolver.resolve(
            sourcePocketId = command.sourcePocketId,
            destinationPocketId = command.destinationPocketId,
            partnerId = command.partnerId,
            transactionType = command.type,
            currencyCode = currency.code,
        )
        val transaction = TransactionRecord(
            id = transactionIdGenerator(),
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
            sourcePocketId = context.sourcePocketId,
            destinationPocketId = context.destinationPocketId,
            parentTransactionId = null,
            recurringTransactionId = null,
            modifiedById = null,
            isArchived = false,
            createdAt = timeProvider(),
        )
        val created = transactionRepository.save(transaction)
        applicationEventBus.publish(TransactionCreated(created))
        return created
    }
}
