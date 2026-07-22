package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.common.error.NotFoundException
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
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
internal class PocketServiceImpl(
    private val pocketRepository: PocketRepository,
    private val accountLookup: PocketAccountLookup,
    private val transactionTemplateService: TransactionTemplateService,
    private val timeProvider: TimeProvider,
    private val projectionEventQueue: TransactionProjectionEventQueue? = null,
) : PocketService {
    override suspend fun create(command: CreatePocketCommand): Pocket {
        val accountId = existingAccountId(command.accountId)
        val pocket = createPocket(
            accountId = accountId,
            name = command.name,
            description = command.description,
            color = command.color,
            isDefault = command.isDefault,
            isContractPocket = false,
        )
        enqueueProjectionChange(pocket.id)
        return pocket
    }

    override suspend fun createForContract(command: CreatePocketForContractCommand): Pocket {
        val accountId = existingAccountId(command.accountId)
        return if (command.useDefaultPocket) {
            defaultPocket(accountId).let { defaultPocket ->
                if (defaultPocket.isContractPocket) defaultPocket
                else pocketRepository.save(defaultPocket.copy(isContractPocket = true))
            }
        } else {
            createPocket(
                accountId = accountId,
                name = command.name,
                description = command.description,
                color = command.color,
                isDefault = false,
                isContractPocket = true,
            )
        }
    }

    override suspend fun update(command: UpdatePocketCommand): Pocket {
        val existing = existingPocket(command.id)
        val accountId = existingAccountId(command.accountId)
        val persisted = pocketRepository.save(
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

    override suspend fun delete(id: Long) {
        val normalizedId = existingPocket(id).id
        pocketRepository.deleteById(normalizedId)
        enqueueProjectionChange(normalizedId)
    }

    override suspend fun list(accountId: Long?, archived: Boolean?): List<Pocket> =
        pocketRepository.findAllByAccountIdAndArchived(
            accountId = accountId,
            archived = archived,
        )
            .toList()
            .map { it.toDTO() }

    override suspend fun getById(id: Long): Pocket? =
        pocketRepository.findById(id)?.toDTO()

    private suspend fun createPocket(
        accountId: Long,
        name: String,
        description: String?,
        color: Int,
        isDefault: Boolean,
        isContractPocket: Boolean,
    ): Pocket =
        pocketRepository.save(
            PocketModel(
                id = null,
                accountId = accountId,
                name = name,
                description = description,
                color = color,
                isDefault = isDefault,
                isContractPocket = isContractPocket,
                isArchived = false,
                createdAt = timeProvider(),
            ),
        ).toDTO()

    private suspend fun existingAccountId(accountId: Long): Long {
        if (!accountLookup.exists(accountId)) {
            throw NotFoundException(
                code = "not_found",
                message = "Account not found",
                details = mapOf("id" to accountId),
            )
        }
        return accountId
    }

    private suspend fun existingPocket(id: Long): Pocket =
        pocketRepository.findById(id)?.toDTO()
            ?: throw NotFoundException(
                code = "not_found",
                message = "Pocket not found",
                details = mapOf("id" to id),
            )

    private suspend fun defaultPocket(accountId: Long): Pocket =
        pocketRepository.findDefaultByAccountId(accountId)?.toDTO()
            ?: throw NotFoundException(
                code = "not_found",
                message = "Default pocket not found",
                details = mapOf("accountId" to accountId),
            )

    private suspend fun enqueueProjectionChange(id: Long) {
        projectionEventQueue?.enqueue(
            TransactionProjectionChangeEvent.PocketChanged(id),
        )
    }
}
