package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.events.ApplicationEventBus
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketQuery
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.events.PocketCreated
import de.chennemann.plannr.server.pockets.events.PocketUpdated
import de.chennemann.plannr.server.pockets.persistence.PocketModel
import de.chennemann.plannr.server.pockets.persistence.toModel
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
internal class PocketServiceImpl(
    private val pocketRepository: PocketRepository,
    private val accountLookup: PocketAccountLookup,
    private val archiveCascade: PocketArchiveCascade,
    private val balanceProvider: PocketBalanceProvider,
    private val timeProvider: TimeProvider,
    private val applicationEventBus: ApplicationEventBus,
) : PocketService {
    override suspend fun create(command: CreatePocketCommand): Pocket {
        val accountId = existingAccountId(command.accountId)
        val created = pocketRepository.save(
            PocketModel(
                id = null,
                accountId = accountId,
                name = command.name,
                description = command.description,
                color = command.color,
                isDefault = command.isDefault,
                isArchived = false,
                createdAt = timeProvider(),
            ),
        )
        applicationEventBus.publish(PocketCreated(created))
        return created
    }

    override suspend fun update(command: UpdatePocketCommand): Pocket {
        val existing = existingPocket(command.id)
        val accountId = existingAccountId(command.accountId)
        val persisted = pocketRepository.update(
            Pocket(
                id = existing.id,
                accountId = accountId,
                name = command.name,
                description = command.description,
                color = command.color,
                isDefault = command.isDefault,
                isArchived = existing.isArchived,
                createdAt = existing.createdAt,
            ).toModel(),
        )
        applicationEventBus.publish(PocketUpdated(existing, persisted))
        return persisted
    }

    override suspend fun archive(id: String): Pocket {
        val existing = existingPocket(id)
        val updated = pocketRepository.update(existing.archive().toModel())
        archiveCascade.archiveFor(updated)
        applicationEventBus.publish(PocketUpdated(existing, updated))
        return updated
    }

    override suspend fun unarchive(id: String): Pocket {
        val existing = existingPocket(id)
        val updated = pocketRepository.update(existing.unarchive().toModel())
        archiveCascade.unarchiveFor(updated)
        applicationEventBus.publish(PocketUpdated(existing, updated))
        return updated
    }

    override suspend fun list(accountId: String?, archived: Boolean?): List<Pocket> =
        pocketRepository.findAll(
            accountId = accountId?.trim()?.takeIf { it.isNotBlank() },
            archived = archived,
        )

    override suspend fun getById(id: String): Pocket? =
        pocketRepository.findById(id.trim())

    override suspend fun listQueries(accountId: String?, archived: Boolean): List<PocketQuery> =
        list(accountId = accountId, archived = archived)
            .map { it.toQuery(balanceProvider.currentBalance(it.id)) }

    override suspend fun getQuery(id: String): PocketQuery =
        existingPocket(id).toQuery(balanceProvider.currentBalance(id.trim()))

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
        pocketRepository.findById(id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Pocket not found",
                details = mapOf("id" to id.trim()),
            )

    private fun Pocket.toQuery(currentBalance: Long): PocketQuery =
        PocketQuery(
            pocketId = id,
            accountId = accountId,
            name = name,
            description = description,
            color = color,
            isDefault = isDefault,
            isArchived = isArchived,
            createdAt = createdAt,
            currentBalance = currentBalance,
        )
}
