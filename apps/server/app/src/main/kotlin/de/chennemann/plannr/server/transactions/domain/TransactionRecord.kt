package de.chennemann.plannr.server.transactions.domain

import de.chennemann.plannr.server.common.domain.normalizeTransactionOrigin
import de.chennemann.plannr.server.common.domain.normalizeTransactionStatus
import de.chennemann.plannr.server.common.domain.normalizeTransactionType
import de.chennemann.plannr.server.common.error.ValidationException
import java.time.LocalDate
import java.time.format.DateTimeParseException

data class TransactionRecord private constructor(
    val id: String,
    val accountId: String,
    val type: String,
    val status: String,
    val transactionDate: String,
    val amount: Long,
    val currencyCode: String,
    val exchangeRate: String?,
    val destinationAmount: Long?,
    val description: String,
    val partnerId: String?,
    val pocketId: String?,
    val sourcePocketId: String?,
    val destinationPocketId: String?,
    val parentTransactionId: String?,
    val recurringTransactionId: String?,
    val modifiedById: String?,
    val transactionOrigin: String,
    val isArchived: Boolean,
    val createdAt: Long,
) {
    fun archive(): TransactionRecord = copy(isArchived = true)

    fun unarchive(): TransactionRecord = copy(isArchived = false)

    fun persistedPocketId(): String? = if (type == "TRANSFER") null else pocketId

    fun persistedSourcePocketId(): String? = if (type == "TRANSFER") sourcePocketId else null

    fun persistedDestinationPocketId(): String? = if (type == "TRANSFER") destinationPocketId else null

    companion object {
        operator fun invoke(
            id: String,
            accountId: String,
            type: String,
            status: String,
            transactionDate: String,
            amount: Long,
            currencyCode: String,
            exchangeRate: String?,
            destinationAmount: Long?,
            description: String,
            partnerId: String?,
            pocketId: String? = null,
            sourcePocketId: String?,
            destinationPocketId: String?,
            parentTransactionId: String?,
            recurringTransactionId: String?,
            modifiedById: String?,
            transactionOrigin: String,
            isArchived: Boolean,
            createdAt: Long,
        ): TransactionRecord {
            val normalizedId = id.trim()
            val normalizedAccountId = accountId.trim()
            val normalizedType = normalizeTransactionType(type)
            val normalizedStatus = normalizeTransactionStatus(status)
            val normalizedTransactionDate = transactionDate.trim()
            val normalizedCurrencyCode = currencyCode.trim().uppercase()
            val normalizedExchangeRate = exchangeRate?.trim()?.takeIf { it.isNotBlank() }
            val normalizedDescription = description.trim()
            val normalizedPartnerId = partnerId?.trim()?.takeIf { it.isNotBlank() }
            val normalizedPocketId = pocketId?.trim()?.takeIf { it.isNotBlank() }
            val normalizedSourcePocketId = sourcePocketId?.trim()?.takeIf { it.isNotBlank() }
            val normalizedDestinationPocketId = destinationPocketId?.trim()?.takeIf { it.isNotBlank() }
            val normalizedParentTransactionId = parentTransactionId?.trim()?.takeIf { it.isNotBlank() }
            val normalizedRecurringTransactionId = recurringTransactionId?.trim()?.takeIf { it.isNotBlank() }
            val normalizedModifiedById = modifiedById?.trim()?.takeIf { it.isNotBlank() }
            val normalizedTransactionOrigin = normalizeTransactionOrigin(transactionOrigin)

            if (normalizedId.isBlank()) throw ValidationException("validation_error", "Transaction id must not be blank")
            if (normalizedAccountId.isBlank()) throw ValidationException("validation_error", "Transaction account id must not be blank")
            if (normalizedTransactionDate.isBlank()) throw ValidationException("validation_error", "Transaction date must not be blank")
            if (normalizedCurrencyCode.isBlank()) throw ValidationException("validation_error", "Transaction currency code must not be blank")
            if (normalizedDescription.isBlank()) throw ValidationException("validation_error", "Transaction description must not be blank")
            if (amount < 0) throw ValidationException("validation_error", "Transaction amount must not be negative")
            if (destinationAmount != null && destinationAmount < 0) throw ValidationException("validation_error", "Transaction destination amount must not be negative")

            parseDate(normalizedTransactionDate, "Transaction date must be a plain date")
            val canonicalPockets = canonicalizePockets(
                type = normalizedType,
                pocketId = normalizedPocketId,
                sourcePocketId = normalizedSourcePocketId,
                destinationPocketId = normalizedDestinationPocketId,
            )

            return TransactionRecord(
                id = normalizedId,
                accountId = normalizedAccountId,
                type = normalizedType,
                status = normalizedStatus,
                transactionDate = normalizedTransactionDate,
                amount = amount,
                currencyCode = normalizedCurrencyCode,
                exchangeRate = normalizedExchangeRate,
                destinationAmount = destinationAmount,
                description = normalizedDescription,
                partnerId = normalizedPartnerId,
                pocketId = canonicalPockets.pocketId,
                sourcePocketId = canonicalPockets.sourcePocketId,
                destinationPocketId = canonicalPockets.destinationPocketId,
                parentTransactionId = normalizedParentTransactionId,
                recurringTransactionId = normalizedRecurringTransactionId,
                modifiedById = normalizedModifiedById,
                transactionOrigin = normalizedTransactionOrigin,
                isArchived = isArchived,
                createdAt = createdAt,
            )
        }

        private fun parseDate(value: String, message: String): LocalDate =
            try {
                LocalDate.parse(value)
            } catch (_: DateTimeParseException) {
                throw ValidationException("validation_error", message)
            }

        private fun canonicalizePockets(
            type: String,
            pocketId: String?,
            sourcePocketId: String?,
            destinationPocketId: String?,
        ): CanonicalPockets = when (type) {
            "EXPENSE" -> {
                val resolvedPocketId = pocketId ?: sourcePocketId
                    ?: throw ValidationException("validation_error", "Expense transaction requires source pocket")
                if (destinationPocketId != null) {
                    throw ValidationException("validation_error", "Expense transaction must not define destination pocket")
                }
                CanonicalPockets(
                    pocketId = resolvedPocketId,
                    sourcePocketId = resolvedPocketId,
                    destinationPocketId = null,
                )
            }
            "INCOME" -> {
                val resolvedPocketId = pocketId ?: destinationPocketId
                    ?: throw ValidationException("validation_error", "Income transaction requires destination pocket")
                if (sourcePocketId != null) {
                    throw ValidationException("validation_error", "Income transaction must not define source pocket")
                }
                CanonicalPockets(
                    pocketId = resolvedPocketId,
                    sourcePocketId = null,
                    destinationPocketId = resolvedPocketId,
                )
            }
            "TRANSFER" -> {
                if (pocketId != null) {
                    throw ValidationException("validation_error", "Transfer transaction must not define pocketId")
                }
                if (sourcePocketId == null || destinationPocketId == null) {
                    throw ValidationException("validation_error", "Transfer transaction requires source and destination pockets")
                }
                if (sourcePocketId == destinationPocketId) {
                    throw ValidationException("validation_error", "Transfer transaction source and destination pockets must differ")
                }
                CanonicalPockets(
                    pocketId = null,
                    sourcePocketId = sourcePocketId,
                    destinationPocketId = destinationPocketId,
                )
            }
            else -> throw ValidationException("validation_error", "Transaction type is invalid")
        }

        private data class CanonicalPockets(
            val pocketId: String?,
            val sourcePocketId: String?,
            val destinationPocketId: String?,
        )
    }
}
