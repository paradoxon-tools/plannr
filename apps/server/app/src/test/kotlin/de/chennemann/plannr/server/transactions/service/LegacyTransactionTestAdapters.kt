package de.chennemann.plannr.server.transactions.service

import de.chennemann.plannr.server.transactions.domain.TransactionRecord
import org.springframework.stereotype.Component

@Component
class CreateTransaction(
    private val transactionService: TransactionService,
) {
    suspend operator fun invoke(command: Command): TransactionRecord =
        transactionService.create(command.toServiceCommand())

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
    ) {
        fun toServiceCommand() = TransactionService.CreateCommand(
            type = type,
            status = status,
            transactionDate = transactionDate,
            amount = amount,
            currencyCode = currencyCode,
            exchangeRate = exchangeRate,
            destinationAmount = destinationAmount,
            description = description,
            partnerId = partnerId,
            sourcePocketId = sourcePocketId,
            destinationPocketId = destinationPocketId,
        )
    }
}

@Component
class UpdateTransaction(
    private val transactionService: TransactionService,
) {
    suspend operator fun invoke(command: Command): TransactionRecord =
        transactionService.update(command.toServiceCommand())

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
    ) {
        fun toServiceCommand() = TransactionService.UpdateCommand(
            id = id,
            type = type,
            status = status,
            transactionDate = transactionDate,
            amount = amount,
            currencyCode = currencyCode,
            exchangeRate = exchangeRate,
            destinationAmount = destinationAmount,
            description = description,
            partnerId = partnerId,
            sourcePocketId = sourcePocketId,
            destinationPocketId = destinationPocketId,
        )
    }
}

@Component
class ArchiveTransaction(
    private val transactionService: TransactionService,
) {
    suspend operator fun invoke(id: String): TransactionRecord =
        transactionService.archive(id)
}

@Component
class UnarchiveTransaction(
    private val transactionService: TransactionService,
) {
    suspend operator fun invoke(id: String): TransactionRecord =
        transactionService.unarchive(id)
}
