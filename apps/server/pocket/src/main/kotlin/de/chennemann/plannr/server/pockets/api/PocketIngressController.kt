package de.chennemann.plannr.server.pockets.api

import de.chennemann.plannr.server.pockets.api.dto.CreatePocketRequest
import de.chennemann.plannr.server.pockets.api.dto.PocketResponse
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketRequest
import de.chennemann.plannr.server.pockets.service.PocketService
import org.springframework.web.bind.annotation.RestController

@RestController
class PocketIngressController(
    private val pocketService: PocketService,
) : PocketIngressApi {
    override suspend fun create(request: CreatePocketRequest): PocketResponse =
        pocketService.create(request.toCommand()).toResponse()

    override suspend fun update(id: String, request: UpdatePocketRequest): PocketResponse =
        pocketService.update(request.toCommand(id)).toResponse()

    override suspend fun archive(id: String): PocketResponse =
        pocketService.archive(id).toResponse()

    override suspend fun unarchive(id: String): PocketResponse =
        pocketService.unarchive(id).toResponse()
}
