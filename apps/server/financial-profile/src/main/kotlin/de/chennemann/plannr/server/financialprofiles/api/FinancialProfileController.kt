package de.chennemann.plannr.server.financialprofiles.api

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.financialprofiles.api.dto.CreateFinancialProfileCommand
import de.chennemann.plannr.server.financialprofiles.api.dto.FinancialProfile
import de.chennemann.plannr.server.financialprofiles.api.dto.UpdateFinancialProfileCommand
import de.chennemann.plannr.server.financialprofiles.service.FinancialProfileService
import org.springframework.web.bind.annotation.RestController

@RestController
class FinancialProfileController(
    private val financialProfileService: FinancialProfileService,
) : FinancialProfileApi {
    override suspend fun create(command: CreateFinancialProfileCommand): FinancialProfile =
        financialProfileService.create(command)

    override suspend fun update(command: UpdateFinancialProfileCommand): FinancialProfile =
        financialProfileService.update(command)

    override suspend fun makeDefault(id: Long): FinancialProfile =
        financialProfileService.makeDefault(id)

    override suspend fun archive(id: Long): FinancialProfile =
        financialProfileService.archive(id)

    override suspend fun unarchive(id: Long): FinancialProfile =
        financialProfileService.unarchive(id)

    override suspend fun delete(id: Long) =
        financialProfileService.delete(id)

    override suspend fun list(query: String?, archived: Boolean): List<FinancialProfile> =
        financialProfileService.list(query, archived)

    override suspend fun getById(id: Long): FinancialProfile =
        financialProfileService.getById(id)
            ?: throw NotFoundException("not_found", "Financial profile not found", mapOf("id" to id))
}
