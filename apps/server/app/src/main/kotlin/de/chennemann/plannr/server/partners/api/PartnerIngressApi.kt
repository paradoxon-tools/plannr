package de.chennemann.plannr.server.partners.api

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

@HttpExchange("/partners")
interface PartnerIngressApi {
    @PostExchange
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreatePartnerRequest): PartnerResponse

    @PutExchange("/{id}")
    suspend fun update(@PathVariable id: String, @RequestBody request: UpdatePartnerRequest): PartnerResponse

    @PostExchange("/{id}/archive")
    suspend fun archive(@PathVariable id: String): PartnerResponse

    @PostExchange("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: String): PartnerResponse
}
