package de.chennemann.plannr.server.pockets.api

import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.service.annotation.DeleteExchange
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

@HttpExchange("/pockets")
interface PocketApi {
    @PostExchange
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody command: CreatePocketCommand): Pocket

    @PutExchange
    suspend fun update(@RequestBody command: UpdatePocketCommand): Pocket

    @PostExchange("/{id}/archive")
    suspend fun archive(@PathVariable id: String): Pocket

    @PostExchange("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: String): Pocket

    @DeleteExchange("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable id: String)

    @GetExchange
    suspend fun list(
        @RequestParam(required = false) accountId: Long?,
        @RequestParam(defaultValue = "false") archived: Boolean,
    ): List<Pocket>

    @GetExchange("/{id}")
    suspend fun getById(@PathVariable id: String): Pocket
}
