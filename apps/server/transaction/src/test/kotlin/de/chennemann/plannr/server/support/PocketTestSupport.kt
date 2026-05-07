package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.PocketWithContract
import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.pockets.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand

class FakePocketService(
    initialPockets: Iterable<Pocket> = listOf(Pocket(1L, 1L, "Bills", "Monthly fixed costs", 123456, false, false, false, 1_710_000_100L)),
) : PocketService {
    private val pockets = initialPockets.associateByTo(linkedMapOf()) { it.id }

    override suspend fun create(command: CreatePocketCommand): Pocket = throw UnsupportedOperationException("Not used")
    override suspend fun update(command: UpdatePocketCommand): Pocket = throw UnsupportedOperationException("Not used")
    override suspend fun updateContract(pocketId: Long, command: UpdateContractCommand): PocketWithContract = throw UnsupportedOperationException("Not used")
    override suspend fun archive(id: Long): Pocket = throw UnsupportedOperationException("Not used")
    override suspend fun unarchive(id: Long): Pocket = throw UnsupportedOperationException("Not used")
    override suspend fun archiveForAccount(accountId: Long) = throw UnsupportedOperationException("Not used")
    override suspend fun unarchiveForAccount(accountId: Long) = throw UnsupportedOperationException("Not used")
    override suspend fun delete(id: Long) = throw UnsupportedOperationException("Not used")
    override suspend fun list(accountId: Long?, archived: Boolean?): List<Pocket> = pockets.values.toList()
    override suspend fun getById(id: Long): Pocket? = pockets[id]
}
