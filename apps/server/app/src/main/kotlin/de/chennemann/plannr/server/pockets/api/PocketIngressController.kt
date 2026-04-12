package de.chennemann.plannr.server.pockets.api

import de.chennemann.plannr.server.pockets.usecases.ArchivePocket
import de.chennemann.plannr.server.pockets.usecases.CreatePocket
import de.chennemann.plannr.server.pockets.usecases.UnarchivePocket
import de.chennemann.plannr.server.pockets.usecases.UpdatePocket
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pockets")
class PocketIngressController(
    private val createPocket: CreatePocket,
    private val updatePocket: UpdatePocket,
    private val archivePocket: ArchivePocket,
    private val unarchivePocket: UnarchivePocket,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreatePocketRequest): PocketResponse =
        PocketResponse.from(createPocket(request.toCommand()))


    @PutMapping("/{id}")
    suspend fun update(@PathVariable id: String, @RequestBody request: UpdatePocketRequest): PocketResponse =
        PocketResponse.from(updatePocket(request.toCommand(id)))

    @PostMapping("/{id}/archive")
    suspend fun archive(@PathVariable id: String): PocketResponse =
        PocketResponse.from(archivePocket(id))

    @PostMapping("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: String): PocketResponse =
        PocketResponse.from(unarchivePocket(id))
}
