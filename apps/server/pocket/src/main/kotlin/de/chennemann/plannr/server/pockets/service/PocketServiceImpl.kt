package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.contracts.api.dto.Contract
import de.chennemann.plannr.server.contracts.service.ContractService
import de.chennemann.plannr.server.pockets.api.dto.CreateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.persistence.toDomain
import de.chennemann.plannr.server.transactions.recurring.service.RecurringTransactionService
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
internal class PocketServiceImpl(
    private val pocketRepository: PocketRepository,
    private val accountLookup: PocketAccountLookup,
    private val contractService: ContractService,
    private val recurringTransactionService: RecurringTransactionService,
    private val timeProvider: TimeProvider,
) : PocketService {
    override suspend fun create(command: CreatePocketCommand): Pocket {
        val accountId = existingAccountId(command.accountId)
        val created = pocketRepository.insert(
            id = null,
            accountId = accountId,
            name = command.name,
            description = command.description,
            color = command.color,
            isDefault = command.isDefault,
            isContractPocket = command.isContractPocket,
            isArchived = false,
            createdAt = timeProvider(),
        ).toDomain()
        return created
    }

    override suspend fun update(command: UpdatePocketCommand): Pocket {
        val existing = existingPocket(command.id)
        val accountId = existingAccountId(command.accountId)
        val persisted = pocketRepository.update(
            id = existing.id,
            accountId = accountId,
            name = command.name,
            description = command.description,
            color = command.color,
            isDefault = command.isDefault,
            isArchived = existing.isArchived,
        ).toDomain()
        return persisted
    }

    override suspend fun createContract(pocketId: String, command: CreateContractCommand): Contract =
        contractService.create(existingPocket(pocketId), command)

    override suspend fun updateContract(pocketId: String, command: UpdateContractCommand): Contract =
        contractService.update(existingPocket(pocketId), command)

    override suspend fun archive(id: String): Pocket {
        val existing = existingPocket(id)
        val updated = pocketRepository.update(
            id = existing.id,
            accountId = existing.accountId,
            name = existing.name,
            description = existing.description,
            color = existing.color,
            isDefault = existing.isDefault,
            isArchived = true,
        ).toDomain()
        if (updated.isContractPocket) {
            contractService.archiveForPocket(updated.id)
        }
        recurringTransactionService.archiveForPocket(updated.accountId, updated.id)
        return updated
    }

    override suspend fun unarchive(id: String): Pocket {
        val existing = existingPocket(id)
        val updated = pocketRepository.update(
            id = existing.id,
            accountId = existing.accountId,
            name = existing.name,
            description = existing.description,
            color = existing.color,
            isDefault = existing.isDefault,
            isArchived = false,
        ).toDomain()
        if (updated.isContractPocket) {
            contractService.unarchiveForPocket(updated.id)
        }
        recurringTransactionService.unarchiveForPocket(updated.accountId, updated.id)
        return updated
    }

    override suspend fun archiveForAccount(accountId: Long) {
        list(accountId = accountId).forEach { archive(it.id) }
        recurringTransactionService.archiveForAccount(accountId)
    }

    override suspend fun unarchiveForAccount(accountId: Long) {
        list(accountId = accountId).forEach { unarchive(it.id) }
        recurringTransactionService.unarchiveForAccount(accountId)
    }

    override suspend fun delete(id: String) {
        val normalizedId = existingPocket(id).id
        pocketRepository.deleteById(normalizedId)
    }

    override suspend fun list(accountId: Long?, archived: Boolean?): List<Pocket> =
        pocketRepository.findAllByAccountIdAndArchived(
            accountId = accountId,
            archived = archived,
        )
            .toList()
            .map { it.toDomain() }

    override suspend fun getById(id: String): Pocket? =
        pocketRepository.findById(id.trim())?.toDomain()

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

    private suspend fun existingPocket(id: String): Pocket =
        pocketRepository.findById(id.trim())?.toDomain()
            ?: throw NotFoundException(
                code = "not_found",
                message = "Pocket not found",
                details = mapOf("id" to id.trim()),
            )
}
