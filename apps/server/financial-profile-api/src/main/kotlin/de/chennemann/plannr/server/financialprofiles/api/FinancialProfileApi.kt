package de.chennemann.plannr.server.financialprofiles.api

import de.chennemann.plannr.server.financialprofiles.api.dto.CreateFinancialProfileCommand
import de.chennemann.plannr.server.financialprofiles.api.dto.FinancialProfile
import de.chennemann.plannr.server.financialprofiles.api.dto.UpdateFinancialProfileCommand
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

@HttpExchange("/financial-profiles")
interface FinancialProfileApi {
    @PostExchange
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody command: CreateFinancialProfileCommand): FinancialProfile

    @PutExchange
    suspend fun update(@RequestBody command: UpdateFinancialProfileCommand): FinancialProfile

    @PostExchange("/{id}/default")
    suspend fun makeDefault(@PathVariable id: Long): FinancialProfile

    @PostExchange("/{id}/archive")
    suspend fun archive(@PathVariable id: Long): FinancialProfile

    @PostExchange("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: Long): FinancialProfile

    @DeleteExchange("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable id: Long)

    @GetExchange
    suspend fun list(
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "false") archived: Boolean,
    ): List<FinancialProfile>

    @GetExchange("/{id}")
    suspend fun getById(@PathVariable id: Long): FinancialProfile
}
