package de.chennemann.plannr.server.pockets.api

import de.chennemann.plannr.server.pockets.api.dto.CreatePocketRequest
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

@HttpExchange("/pockets")
interface PocketApi {
    @PostExchange
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreatePocketRequest): Pocket

    @PutExchange("/{id}")
    suspend fun update(@PathVariable id: String, @RequestBody request: UpdatePocketRequest): Pocket

    @PostExchange("/{id}/archive")
    suspend fun archive(@PathVariable id: String): Pocket

    @PostExchange("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: String): Pocket

    @GetExchange
    suspend fun list(
        @RequestParam(required = false) accountId: String?,
        @RequestParam(defaultValue = "false") archived: Boolean,
    ): List<Pocket>

    @GetExchange("/{id}")
    suspend fun getById(@PathVariable id: String): Pocket
}
