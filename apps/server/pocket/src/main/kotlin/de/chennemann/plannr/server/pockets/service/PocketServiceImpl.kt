package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.persistence.toDomain
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
internal class PocketServiceImpl(
    private val pocketRepository: PocketRepository,
    private val accountLookup: PocketAccountLookup,
    private val archiveCascade: PocketArchiveCascade,
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
        archiveCascade.archiveFor(updated)
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
        archiveCascade.unarchiveFor(updated)
        return updated
    }

    override suspend fun list(accountId: String?, archived: Boolean?): List<Pocket> =
        pocketRepository.findAllByAccountIdAndArchived(
            accountId = accountId?.trim()?.takeIf { it.isNotBlank() },
            archived = archived,
        )
            .toList()
            .map { it.toDomain() }

    override suspend fun getById(id: String): Pocket? =
        pocketRepository.findById(id.trim())?.toDomain()

    private suspend fun existingAccountId(accountId: String): String {
        val normalizedAccountId = accountId.trim()
        if (!accountLookup.exists(normalizedAccountId)) {
            throw NotFoundException(
                code = "not_found",
                message = "Account not found",
                details = mapOf("id" to normalizedAccountId),
            )
        }
        return normalizedAccountId
    }

    private suspend fun existingPocket(id: String): Pocket =
        pocketRepository.findById(id.trim())?.toDomain()
            ?: throw NotFoundException(
                code = "not_found",
                message = "Pocket not found",
                details = mapOf("id" to id.trim()),
            )
}
