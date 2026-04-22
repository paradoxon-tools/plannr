package de.chennemann.plannr.server.transactions.usecases

import de.chennemann.plannr.server.accounts.service.AccountService
import de.chennemann.plannr.server.common.domain.normalizeTransactionType
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.service.PocketService
import org.springframework.stereotype.Component

@Component
internal class TransactionContextResolver(
    private val accountService: AccountService,
    private val pocketService: PocketService,
    private val partnerService: PartnerService,
) {
    suspend fun resolve(
        sourcePocketId: String?,
        destinationPocketId: String?,
        partnerId: String?,
        transactionType: String,
        currencyCode: String,
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
            "EXPENSE" -> if (sourcePocket == null) throw ValidationException("validation_error", "Expense transaction requires source pocket")
            "INCOME" -> if (destinationPocket == null) throw ValidationException("validation_error", "Income transaction requires destination pocket")
            "TRANSFER" -> {
                if (sourcePocket == null || destinationPocket == null) {
                    throw ValidationException("validation_error", "Transfer transaction requires source and destination pockets")
                }
                if (sourcePocket.id == destinationPocket.id) {
                    throw ValidationException("validation_error", "Transfer transaction source and destination pockets must differ")
                }
            }
            else -> throw ValidationException("validation_error", "Transaction type is invalid")
        }

        val accountId = sourcePocket?.accountId
            ?: destinationPocket?.accountId
            ?: throw ValidationException("validation_error", "Transaction must reference at least one pocket")

        listOfNotNull(sourcePocket, destinationPocket).forEach { pocket ->
            if (pocket.accountId != accountId) {
                throw ValidationException("validation_error", "Transaction pockets must belong to the same account")
            }
        }

        val account = accountService.getById(accountId)
            ?: throw NotFoundException("not_found", "Account not found", mapOf("id" to accountId))
        if (account.currencyCode != currencyCode.trim().uppercase()) {
            throw ValidationException("validation_error", "Transaction currency must match account currency")
        }

        return ResolvedContext(
            accountId = account.id,
            pocketId = when (normalizedTransactionType) {
                "EXPENSE" -> sourcePocket?.id
                "INCOME" -> destinationPocket?.id
                else -> null
            },
            sourcePocketId = sourcePocket?.id,
            destinationPocketId = destinationPocket?.id,
            partnerId = resolvedPartnerId,
        )
    }

    data class ResolvedContext(
        val accountId: String,
        val pocketId: String?,
        val sourcePocketId: String?,
        val destinationPocketId: String?,
        val partnerId: String?,
    )
}
