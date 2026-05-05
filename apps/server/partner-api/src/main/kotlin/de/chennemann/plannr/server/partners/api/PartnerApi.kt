package de.chennemann.plannr.server.partners.api

import de.chennemann.plannr.server.partners.api.dto.CreatePartnerCommand
import de.chennemann.plannr.server.partners.api.dto.Partner
import de.chennemann.plannr.server.partners.api.dto.UpdatePartnerCommand
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

@HttpExchange("/partners")
interface PartnerApi {
    @PostExchange
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody command: CreatePartnerCommand): Partner

    @PutExchange
    suspend fun update(@RequestBody command: UpdatePartnerCommand): Partner

    @PostExchange("/{id}/archive")
    suspend fun archive(@PathVariable id: String): Partner

    @PostExchange("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: String): Partner

    @GetExchange
    suspend fun list(
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "false") archived: Boolean,
    ): List<Partner>
}
