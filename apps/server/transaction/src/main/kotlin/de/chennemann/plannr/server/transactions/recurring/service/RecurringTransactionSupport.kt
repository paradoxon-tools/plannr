package de.chennemann.plannr.server.transactions.recurring.service

import de.chennemann.plannr.server.common.domain.normalizeTransactionType
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.service.PocketService
import org.springframework.stereotype.Component

@Component
class RecurringTransactionContextResolver(
    private val pocketService: PocketService,
    private val partnerService: PartnerService,
) {
    suspend fun resolve(
        sourcePocketId: String?,
        destinationPocketId: String?,
        partnerId: String?,
        transactionType: String,
    ): ResolvedContext {
        val normalizedTransactionType = normalizeTransactionType(transactionType)
        val sourcePocket = sourcePocketId?.trim()?.takeIf { it.isNotBlank() }?.let {
            pocketService.getById(it)
                ?: throw NotFoundException("not_found", "Pocket not found", mapOf("id" to it))
        }
        val destinationPocket = destinationPocketId?.trim()?.takeIf { it.isNotBlank() }?.let {
            pocketService.getById(it)
                ?: throw NotFoundException("not_found", "Pocket not found", mapOf("id" to it))
        }
        val resolvedPartnerId = partnerId?.trim()?.takeIf { it.isNotBlank() }?.let {
            partnerService.getById(it)?.id
                ?: throw NotFoundException("not_found", "Partner not found", mapOf("id" to it))
        }

        when (normalizedTransactionType) {
            "EXPENSE" -> if (sourcePocket == null) throw ValidationException("validation_error", "Expense recurring transaction requires source pocket")
            "INCOME" -> if (destinationPocket == null) throw ValidationException("validation_error", "Income recurring transaction requires destination pocket")
            "TRANSFER" -> {
                if (sourcePocket == null || destinationPocket == null) {
                    throw ValidationException("validation_error", "Transfer recurring transaction requires source and destination pockets")
                }
            }
            else -> throw ValidationException("validation_error", "Recurring transaction type is invalid")
        }

        val accountId = sourcePocket?.accountId
            ?: destinationPocket?.accountId
            ?: throw ValidationException("validation_error", "Recurring transaction must reference at least one pocket")

        listOfNotNull(sourcePocket, destinationPocket).forEach { pocket: Pocket ->
            if (pocket.accountId != accountId) {
                throw ValidationException("validation_error", "Recurring transaction pockets must belong to the same account")
            }
        }

        return ResolvedContext(
            accountId = accountId,
            sourcePocketId = sourcePocket?.id,
            destinationPocketId = destinationPocket?.id,
            partnerId = resolvedPartnerId,
        )
    }

    data class ResolvedContext(
        val accountId: Long,
        val sourcePocketId: String?,
        val destinationPocketId: String?,
        val partnerId: String?,
    )
}

