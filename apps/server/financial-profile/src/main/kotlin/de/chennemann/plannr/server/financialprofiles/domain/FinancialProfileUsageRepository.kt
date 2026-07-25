package de.chennemann.plannr.server.financialprofiles.domain

interface FinancialProfileUsageRepository {
    suspend fun reassignReferences(
        sourceProfileId: Long,
        fallbackProfileId: Long,
        fallbackProfileName: String,
    )
}
