package de.chennemann.plannr.server.pockets.api

import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.PocketWithContract
import de.chennemann.plannr.server.pockets.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.common.error.NotFoundException
import org.springframework.web.bind.annotation.RestController

@RestController
class PocketController(
    private val pocketService: PocketService,
) : PocketApi {
    override suspend fun create(command: CreatePocketCommand): Pocket =
        pocketService.create(command)

    override suspend fun update(command: UpdatePocketCommand): Pocket =
        pocketService.update(command)

    override suspend fun updateContract(id: Long, command: UpdateContractCommand): PocketWithContract =
        pocketService.updateContract(id, command)

    override suspend fun archive(id: Long): Pocket =
        pocketService.archive(id)

    override suspend fun unarchive(id: Long): Pocket =
        pocketService.unarchive(id)

    override suspend fun delete(id: Long) =
        pocketService.delete(id)

    override suspend fun list(accountId: Long?, archived: Boolean): List<Pocket> =
        pocketService.list(accountId, archived)

    override suspend fun getById(id: Long): Pocket =
        pocketService.getById(id)
            ?: throw NotFoundException("not_found", "Pocket not found", mapOf("id" to id))
}
