package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketQuery
import de.chennemann.plannr.server.pockets.service.CreatePocketCommand
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.pockets.service.UpdatePocketCommand

class FakePocketService(
    initialPockets: Iterable<Pocket> = listOf(Pocket("poc_123", "acc_123", "Bills", "Monthly fixed costs", 123456, false, false, 1_710_000_100L)),
) : PocketService {
    private val pockets = initialPockets.associateByTo(linkedMapOf()) { it.id }

    override suspend fun create(command: CreatePocketCommand): Pocket = throw UnsupportedOperationException("Not used")
    override suspend fun update(command: UpdatePocketCommand): Pocket = throw UnsupportedOperationException("Not used")
    override suspend fun archive(id: String): Pocket = throw UnsupportedOperationException("Not used")
    override suspend fun unarchive(id: String): Pocket = throw UnsupportedOperationException("Not used")
    override suspend fun list(accountId: String?, archived: Boolean?): List<Pocket> = pockets.values.toList()
    override suspend fun getById(id: String): Pocket? = pockets[id.trim()]
    override suspend fun listQueries(accountId: String?, archived: Boolean): List<PocketQuery> = emptyList()
    override suspend fun getQuery(id: String): PocketQuery =
        throw NotFoundException("not_found", "Pocket not found", mapOf("id" to id.trim()))
}
