package de.chennemann.plannr.server.transactions.recurring.usecases

import de.chennemann.plannr.server.common.domain.normalizeTransactionType
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.partners.domain.PartnerRepository
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import org.springframework.stereotype.Component

@Component
internal class RecurringTransactionContextResolver(
    private val contractRepository: ContractRepository,
    private val pocketRepository: PocketRepository,
    private val partnerRepository: PartnerRepository,
) {
    suspend fun resolve(
        contractId: String?,
        sourcePocketId: String?,
        destinationPocketId: String?,
        partnerId: String?,
        transactionType: String,
    ): ResolvedContext {
        val normalizedTransactionType = normalizeTransactionType(transactionType)
        val contract = contractId?.trim()?.takeIf { it.isNotBlank() }?.let {
            contractRepository.findById(it)
                ?: throw NotFoundException("not_found", "Contract not found", mapOf("id" to it))
        }
        val sourcePocket = sourcePocketId?.trim()?.takeIf { it.isNotBlank() }?.let {
            pocketRepository.findById(it)
                ?: throw NotFoundException("not_found", "Pocket not found", mapOf("id" to it))
        }
        val destinationPocket = destinationPocketId?.trim()?.takeIf { it.isNotBlank() }?.let {
            pocketRepository.findById(it)
                ?: throw NotFoundException("not_found", "Pocket not found", mapOf("id" to it))
        }
        val resolvedPartnerId = partnerId?.trim()?.takeIf { it.isNotBlank() }?.let {
            partnerRepository.findById(it)?.id
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

        val accountId = contract?.accountId
            ?: sourcePocket?.accountId
            ?: destinationPocket?.accountId
            ?: throw ValidationException("validation_error", "Recurring transaction must reference at least one pocket or a contract")

        listOfNotNull(sourcePocket, destinationPocket).forEach { pocket: de.chennemann.plannr.server.pockets.domain.Pocket ->
            if (pocket.accountId != accountId) {
                throw ValidationException("validation_error", "Recurring transaction pockets must belong to the same account")
            }
        }

        if (contract != null) {
            if (sourcePocket != null && sourcePocket.accountId != contract.accountId) {
                throw ValidationException("validation_error", "Recurring transaction source pocket must belong to the contract account")
            }
            if (destinationPocket != null && destinationPocket.accountId != contract.accountId) {
                throw ValidationException("validation_error", "Recurring transaction destination pocket must belong to the contract account")
            }
            if (sourcePocket?.id != contract.pocketId && destinationPocket?.id != contract.pocketId) {
                throw ValidationException("validation_error", "Recurring transaction must reference the contract pocket as source or destination")
            }
        }

        return ResolvedContext(
            contractId = contract?.id,
            accountId = accountId,
            sourcePocketId = sourcePocket?.id,
            destinationPocketId = destinationPocket?.id,
            partnerId = resolvedPartnerId,
        )
    }

    data class ResolvedContext(
        val contractId: String?,
        val accountId: String,
        val sourcePocketId: String?,
        val destinationPocketId: String?,
        val partnerId: String?,
    )
}
