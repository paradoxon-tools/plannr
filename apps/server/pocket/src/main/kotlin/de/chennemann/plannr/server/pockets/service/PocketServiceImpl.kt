package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.accounts.service.AccountService
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.domain.save
import de.chennemann.plannr.server.pockets.persistence.PocketModel
import de.chennemann.plannr.server.pockets.persistence.toDTO
import de.chennemann.plannr.server.transactions.projection.service.TransactionProjectionChangeEvent
import de.chennemann.plannr.server.transactions.projection.service.TransactionProjectionEventQueue
import de.chennemann.plannr.server.transactions.templates.service.TransactionTemplateService
import kotlinx.coroutines.flow.toList
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
internal class PocketServiceImpl(
    private val pocketRepository: PocketRepository,
    @param:Lazy
    private val accountService: AccountService,
    @param:Lazy
    private val contractPresentationService: ContractPresentationService,
    @param:Lazy
    private val transactionTemplateService: TransactionTemplateService,
    private val timeProvider: TimeProvider,
    private val projectionEventQueue: TransactionProjectionEventQueue? = null,
) : PocketService {
    override suspend fun create(command: CreatePocketCommand): Pocket {
        val accountId = existingAccountId(command.accountId)
        val pocket =
            createPocket(
                accountId = accountId,
                name = command.name,
                description = command.description,
                color = command.color,
                isDefault = command.isDefault,
                contractId = null,
                savingGoalId = null,
            )
        enqueueProjectionChange(pocket.id)
        return pocket
    }

    override suspend fun createForContract(command: CreatePocketForContractCommand): Pocket {
        val accountId = existingAccountId(command.accountId)
        return createPocket(
            accountId = accountId,
            name = null,
            description = null,
            color = null,
            isDefault = false,
            contractId = command.contractId,
            savingGoalId = null,
        )
    }

    override suspend fun createForSavingGoal(command: CreatePocketForSavingGoalCommand): Pocket {
        val accountId = existingAccountId(command.accountId)
        val pocket = createPocket(
            accountId = accountId,
            name = command.name,
            description = command.description,
            color = command.color,
            isDefault = false,
            contractId = null,
            savingGoalId = command.savingGoalId,
        )
        enqueueProjectionChange(pocket.id)
        return pocket
    }

    override suspend fun updateForSavingGoal(command: UpdatePocketsForSavingGoalCommand) {
        listForSavingGoal(command.savingGoalId).forEach { pocket ->
            pocketRepository.save(
                pocket.copy(
                    name = command.name,
                    description = command.description,
                    color = command.color,
                ),
            )
            enqueueProjectionChange(pocket.id)
        }
    }

    override suspend fun update(command: UpdatePocketCommand): Pocket {
        val existing = existingPocket(command.id)
        val contractId = existing.contractId
        if (contractId != null) {
            if (command.accountId != existing.accountId || command.isDefault) {
                throw ValidationException(
                    "validation_error",
                    "Dedicated contract pockets cannot change account or become default",
                    mapOf("id" to existing.id),
                )
            }
            contractPresentationService.updatePresentation(
                contractId,
                command.name,
                command.description,
                command.color,
            )
            enqueueProjectionChange(existing.id)
            return existingPocket(existing.id)
        }
        if (existing.savingGoalId != null) {
            throw ValidationException(
                "validation_error",
                "Dedicated saving goal pockets must be updated through their saving goal",
                mapOf("id" to existing.id),
            )
        }
        val accountId = existingAccountId(command.accountId)
        val persisted =
            pocketRepository.save(
                existing.copy(
                    accountId = accountId,
                    name = command.name,
                    description = command.description,
                    color = command.color,
                    isDefault = command.isDefault,
                ),
            )
        enqueueProjectionChange(persisted.id)
        return persisted
    }

    override suspend fun archive(id: Long): Pocket {
        val existing = existingPocket(id)
        val updated = pocketRepository.save(existing.copy(isArchived = true))
        transactionTemplateService.archiveForPocket(updated.id)
        enqueueProjectionChange(updated.id)
        return updated
    }

    override suspend fun unarchive(id: Long): Pocket {
        val existing = existingPocket(id)
        val updated = pocketRepository.save(existing.copy(isArchived = false))
        transactionTemplateService.unarchiveForPocket(updated.id)
        enqueueProjectionChange(updated.id)
        return updated
    }

    override suspend fun archiveForAccount(accountId: Long) {
        list(accountId = accountId).forEach { archive(it.id) }
    }

    override suspend fun unarchiveForAccount(accountId: Long) {
        list(accountId = accountId).forEach { unarchive(it.id) }
    }

    override suspend fun archiveForSavingGoal(savingGoalId: Long) {
        listForSavingGoal(savingGoalId).forEach { archive(it.id) }
    }

    override suspend fun unarchiveForSavingGoal(savingGoalId: Long) {
        listForSavingGoal(savingGoalId).forEach { unarchive(it.id) }
    }

    override suspend fun delete(id: Long) {
        val existing = existingPocket(id)
        if (existing.contractId != null || existing.savingGoalId != null) {
            throw ValidationException(
                "validation_error",
                "Managed pockets cannot be deleted directly",
                mapOf("id" to id),
            )
        }
        val normalizedId = existing.id
        pocketRepository.deleteById(normalizedId)
        enqueueProjectionChange(normalizedId)
    }

    override suspend fun list(
        accountId: Long?,
        archived: Boolean?,
    ): List<Pocket> =
        pocketRepository
            .findAllByAccountIdAndArchived(
                accountId = accountId,
                archived = archived,
            ).toList()
            .map { it.toDTO() }

    override suspend fun listForSavingGoal(savingGoalId: Long): List<Pocket> =
        pocketRepository.findAllBySavingGoalId(savingGoalId).toList().map { it.toDTO() }

    override suspend fun getById(id: Long): Pocket? = pocketRepository.findResolvedById(id)?.toDTO()

    private suspend fun createPocket(
        accountId: Long,
        name: String?,
        description: String?,
        color: Int?,
        isDefault: Boolean,
        contractId: Long?,
        savingGoalId: Long?,
    ): Pocket =
        pocketRepository
            .save(
                PocketModel(
                    id = null,
                    accountId = accountId,
                    contractId = contractId,
                    savingGoalId = savingGoalId,
                    name = name,
                    description = description,
                    color = color,
                    isDefault = isDefault,
                    isArchived = false,
                    createdAt = timeProvider(),
                ),
            ).let { pocketRepository.findResolvedById(requireNotNull(it.id))!!.toDTO() }

    private suspend fun existingAccountId(accountId: Long): Long {
        if (accountService.getById(accountId) == null) {
            throw NotFoundException(
                code = "not_found",
                message = "Account not found",
                details = mapOf("id" to accountId),
            )
        }
        return accountId
    }

    private suspend fun existingPocket(id: Long): Pocket =
        pocketRepository.findResolvedById(id)?.toDTO()
            ?: throw NotFoundException(
                code = "not_found",
                message = "Pocket not found",
                details = mapOf("id" to id),
            )

    private suspend fun enqueueProjectionChange(id: Long) {
        projectionEventQueue?.enqueue(
            TransactionProjectionChangeEvent.PocketChanged(id),
        )
    }
}
