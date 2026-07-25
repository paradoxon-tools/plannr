package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.contracts.api.dto.Contract
import de.chennemann.plannr.server.contracts.api.dto.ContractType
import de.chennemann.plannr.server.contracts.api.dto.CreateContractCommand
import de.chennemann.plannr.server.contracts.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.persistence.ContractModel
import de.chennemann.plannr.server.contracts.persistence.toDTO
import de.chennemann.plannr.server.financialprofiles.service.FinancialProfileService
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.service.CreatePocketForContractCommand
import de.chennemann.plannr.server.pockets.service.ContractPresentationService
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.transactions.projection.service.TransactionProjectionChangeEvent
import de.chennemann.plannr.server.transactions.projection.service.TransactionProjectionEventQueue
import de.chennemann.plannr.server.transactions.templates.service.TransactionTemplateService
import kotlinx.coroutines.flow.toList
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
internal class ContractServiceImpl(
    private val contractRepository: ContractRepository,
    private val financialProfileService: FinancialProfileService,
    private val partnerService: PartnerService,
    private val pocketService: PocketService,
    private val timeProvider: TimeProvider,
    @param:Lazy
    private val transactionTemplateService: TransactionTemplateService,
    private val projectionEventQueue: TransactionProjectionEventQueue? = null,
) : ContractService, ContractPresentationService {
    override suspend fun create(command: CreateContractCommand): Contract {
        val type = command.type
        validateAccounts(type, command.accountIds)
        val persisted = contractRepository.save(
            ContractModel(
                id = null,
                financialProfileId = financialProfileService.resolveForAssignment(command.financialProfileId).id,
                partnerId = resolvePartnerId(command.partnerId),
                name = command.name,
                description = command.description,
                color = command.color,
                type = type.name,
                signingDate = command.signingDate,
                expirationDate = command.expirationDate,
                lastCancellationDate = command.lastCancellationDate,
                isArchived = false,
                createdAt = timeProvider(),
            ),
        ).toDTO()
        if (type == ContractType.ACCUMULATING) {
            command.accountIds.forEach { accountId ->
                pocketService.createForContract(CreatePocketForContractCommand(accountId, persisted.id))
            }
        }
        enqueueProjectionChange(persisted.id)
        return persisted
    }

    override suspend fun update(command: UpdateContractCommand): Contract {
        val existing = existingContract(command.id)
        val type = command.type
        if (type != existing.type) {
            throw ValidationException(
                "validation_error",
                "Contract type cannot be changed after creation",
                mapOf("field" to "type"),
            )
        }
        val updated = contractRepository.save(
            ContractModel(
                id = existing.id,
                financialProfileId = financialProfileService.resolveForAssignment(command.financialProfileId).id,
                partnerId = resolvePartnerId(command.partnerId),
                name = command.name,
                description = command.description,
                color = command.color,
                type = type.name,
                signingDate = command.signingDate,
                expirationDate = command.expirationDate,
                lastCancellationDate = command.lastCancellationDate,
                isArchived = existing.isArchived,
                createdAt = existing.createdAt,
            ),
        ).toDTO()
        transactionTemplateService.refreshFinancialProfilesForContract(updated.id)
        enqueueProjectionChange(updated.id)
        return updated
    }

    override suspend fun updatePresentation(contractId: Long, name: String, description: String?, color: Int) {
        val existing = existingContract(contractId)
        val updated = contractRepository.save(
            ContractModel(
                id = existing.id,
                financialProfileId = existing.financialProfileId,
                partnerId = existing.partnerId,
                name = name,
                description = description,
                color = color,
                type = existing.type.name,
                signingDate = existing.signingDate,
                expirationDate = existing.expirationDate,
                lastCancellationDate = existing.lastCancellationDate,
                isArchived = existing.isArchived,
                createdAt = existing.createdAt,
            ),
        ).toDTO()
        enqueueProjectionChange(updated.id)
    }

    override suspend fun list(accountId: Long?, archived: Boolean): List<Contract> =
        contractRepository.findAllByAccountIdAndArchived(accountId, archived).toList().map { it.toDTO() }

    override suspend fun getById(id: Long): Contract? = contractRepository.findById(id)?.toDTO()

    private suspend fun existingContract(id: Long): Contract =
        getById(id) ?: throw NotFoundException("not_found", "Contract not found", mapOf("id" to id))

    private suspend fun resolvePartnerId(partnerId: Long?): Long? =
        partnerId?.let {
            partnerService.getById(it)?.id
                ?: throw NotFoundException("not_found", "Partner not found", mapOf("id" to it))
        }

    private fun validateAccounts(type: ContractType, accountIds: Set<Long>) {
        if (type == ContractType.ACCUMULATING && accountIds.isEmpty()) {
            throw ValidationException(
                "validation_error",
                "Accumulating contracts require at least one account",
                mapOf("field" to "accountIds"),
            )
        }
        if (type == ContractType.NON_ACCUMULATING && accountIds.isNotEmpty()) {
            throw ValidationException(
                "validation_error",
                "Non-accumulating contracts cannot have dedicated pockets",
                mapOf("field" to "accountIds"),
            )
        }
    }

    private suspend fun enqueueProjectionChange(id: Long) {
        projectionEventQueue?.enqueue(TransactionProjectionChangeEvent.ContractChanged(id))
    }
}
