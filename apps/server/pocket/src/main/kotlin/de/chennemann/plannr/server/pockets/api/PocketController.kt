package de.chennemann.plannr.server.pockets.api

import de.chennemann.plannr.server.pockets.api.dto.CreatePocketRequest
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketRequest
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.common.error.NotFoundException
import org.springframework.web.bind.annotation.RestController

@RestController
class PocketController(
    private val pocketService: PocketService,
) : PocketApi {
    override suspend fun create(request: CreatePocketRequest): Pocket =
        pocketService.create(request.toCommand())

    override suspend fun update(id: String, request: UpdatePocketRequest): Pocket =
        pocketService.update(request.toCommand(id))

    override suspend fun archive(id: String): Pocket =
        pocketService.archive(id)

    override suspend fun unarchive(id: String): Pocket =
        pocketService.unarchive(id)

    override suspend fun list(accountId: String?, archived: Boolean): List<Pocket> =
        pocketService.list(accountId, archived)

    override suspend fun getById(id: String): Pocket =
        pocketService.getById(id.trim())
            ?: throw NotFoundException("not_found", "Pocket not found", mapOf("id" to id.trim()))
}
