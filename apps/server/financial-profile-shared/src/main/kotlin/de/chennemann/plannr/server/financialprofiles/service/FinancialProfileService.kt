package de.chennemann.plannr.server.financialprofiles.service

import de.chennemann.plannr.server.financialprofiles.api.dto.CreateFinancialProfileCommand
import de.chennemann.plannr.server.financialprofiles.api.dto.FinancialProfile
import de.chennemann.plannr.server.financialprofiles.api.dto.UpdateFinancialProfileCommand

interface FinancialProfileService {
    suspend fun create(command: CreateFinancialProfileCommand): FinancialProfile
    suspend fun update(command: UpdateFinancialProfileCommand): FinancialProfile
    suspend fun makeDefault(id: Long): FinancialProfile
    suspend fun archive(id: Long): FinancialProfile
    suspend fun unarchive(id: Long): FinancialProfile
    suspend fun delete(id: Long)
    suspend fun list(query: String? = null, archived: Boolean = false): List<FinancialProfile>
    suspend fun getById(id: Long): FinancialProfile?
    suspend fun resolveForAssignment(id: Long?): FinancialProfile
}
