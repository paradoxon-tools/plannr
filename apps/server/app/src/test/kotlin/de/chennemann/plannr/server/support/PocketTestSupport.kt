package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketQuery
import de.chennemann.plannr.server.pockets.service.CreatePocketCommand
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.pockets.service.UpdatePocketCommand

object TestPockets {
    const val DEFAULT_ID = "poc_123"
    const val DEFAULT_ACCOUNT_ID = "acc_123"
    const val DEFAULT_NAME = "Bills"
    const val DEFAULT_DESCRIPTION = "Monthly fixed costs"
    const val DEFAULT_COLOR = 123456
    const val DEFAULT_CREATED_AT = 1_710_000_100L

    fun pocket(
        id: String = DEFAULT_ID,
        accountId: String = DEFAULT_ACCOUNT_ID,
        name: String = DEFAULT_NAME,
        description: String? = DEFAULT_DESCRIPTION,
        color: Int = DEFAULT_COLOR,
        isDefault: Boolean = false,
        isArchived: Boolean = false,
        createdAt: Long = DEFAULT_CREATED_AT,
    ): Pocket =
        Pocket(
            id = id,
            accountId = accountId,
            name = name,
            description = description,
            color = color,
            isDefault = isDefault,
            isArchived = isArchived,
            createdAt = createdAt,
        )
}

class FakePocketService(
    initialPockets: Iterable<Pocket> = listOf(TestPockets.pocket()),
    private val idGenerator: () -> String = { "poc_new" },
    private val timeProvider: () -> Long = { TestPockets.DEFAULT_CREATED_AT },
    private val balanceProvider: (String) -> Long = { 0 },
    private val onArchive: suspend (Pocket) -> Unit = {},
    private val onUnarchive: suspend (Pocket) -> Unit = {},
) : PocketService {
    private val pockets = initialPockets.associateByTo(linkedMapOf()) { it.id }

    override suspend fun create(command: CreatePocketCommand): Pocket {
        val pocket = Pocket(
            id = idGenerator(),
            accountId = command.accountId,
            name = command.name,
            description = command.description,
            color = command.color,
            isDefault = command.isDefault,
            isArchived = false,
            createdAt = timeProvider(),
        )
        pockets[pocket.id] = pocket
        return pocket
    }

    override suspend fun update(command: UpdatePocketCommand): Pocket {
        val existing = existingPocket(command.id)
        val pocket = Pocket(
            id = existing.id,
            accountId = command.accountId,
            name = command.name,
            description = command.description,
            color = command.color,
            isDefault = command.isDefault,
            isArchived = existing.isArchived,
            createdAt = existing.createdAt,
        )
        pockets[pocket.id] = pocket
        return pocket
    }

    override suspend fun archive(id: String): Pocket {
        val pocket = existingPocket(id).archive()
        pockets[pocket.id] = pocket
        onArchive(pocket)
        return pocket
    }

    override suspend fun unarchive(id: String): Pocket {
        val pocket = existingPocket(id).unarchive()
        pockets[pocket.id] = pocket
        onUnarchive(pocket)
        return pocket
    }

    override suspend fun list(accountId: String?, archived: Boolean?): List<Pocket> {
        val normalizedAccountId = accountId?.trim()?.takeIf { it.isNotBlank() }
        return pockets.values
            .filter { normalizedAccountId == null || it.accountId == normalizedAccountId }
            .filter { archived == null || it.isArchived == archived }
            .sortedWith(compareBy<Pocket> { it.createdAt }.thenBy { it.id })
    }

    override suspend fun getById(id: String): Pocket? =
        pockets[id.trim()]

    override suspend fun listQueries(accountId: String?, archived: Boolean): List<PocketQuery> =
        list(accountId, archived).map { it.toQuery() }

    override suspend fun getQuery(id: String): PocketQuery =
        existingPocket(id).toQuery()

    fun findByIdNow(id: String): Pocket? = pockets[id.trim()]

    private fun existingPocket(id: String): Pocket =
        pockets[id.trim()]
            ?: throw NotFoundException("not_found", "Pocket not found", mapOf("id" to id.trim()))

    private fun Pocket.toQuery(): PocketQuery =
        PocketQuery(
            pocketId = id,
            accountId = accountId,
            name = name,
            description = description,
            color = color,
            isDefault = isDefault,
            isArchived = isArchived,
            createdAt = createdAt,
            currentBalance = balanceProvider(id),
        )
}
