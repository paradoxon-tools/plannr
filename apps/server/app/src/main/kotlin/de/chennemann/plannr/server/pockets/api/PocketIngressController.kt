package de.chennemann.plannr.server.pockets.api

import de.chennemann.plannr.server.pockets.api.dto.CreatePocketRequest
import de.chennemann.plannr.server.pockets.api.dto.PocketResponse
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketRequest
import de.chennemann.plannr.server.pockets.usecases.ArchivePocket
import de.chennemann.plannr.server.pockets.usecases.CreatePocket
import de.chennemann.plannr.server.pockets.usecases.UnarchivePocket
import de.chennemann.plannr.server.pockets.usecases.UpdatePocket
import org.springframework.web.bind.annotation.RestController

@RestController
class PocketIngressController(
    private val createPocket: CreatePocket,
    private val updatePocket: UpdatePocket,
    private val archivePocket: ArchivePocket,
    private val unarchivePocket: UnarchivePocket,
) : PocketIngressApi {
    override suspend fun create(request: CreatePocketRequest): PocketResponse =
        PocketResponse.from(createPocket(request.toCommand()))

    override suspend fun update(id: String, request: UpdatePocketRequest): PocketResponse =
        PocketResponse.from(updatePocket(request.toCommand(id)))

    override suspend fun archive(id: String): PocketResponse =
        PocketResponse.from(archivePocket(id))

    override suspend fun unarchive(id: String): PocketResponse =
        PocketResponse.from(unarchivePocket(id))
}
