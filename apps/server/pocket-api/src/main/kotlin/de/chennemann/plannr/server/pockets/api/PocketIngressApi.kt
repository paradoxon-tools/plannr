package de.chennemann.plannr.server.pockets.api

import de.chennemann.plannr.server.pockets.api.dto.CreatePocketRequest
import de.chennemann.plannr.server.pockets.api.dto.PocketResponse
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

@HttpExchange("/pockets")
interface PocketIngressApi {
    @PostExchange
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreatePocketRequest): PocketResponse

    @PutExchange("/{id}")
    suspend fun update(@PathVariable id: String, @RequestBody request: UpdatePocketRequest): PocketResponse

    @PostExchange("/{id}/archive")
    suspend fun archive(@PathVariable id: String): PocketResponse

    @PostExchange("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: String): PocketResponse
}
