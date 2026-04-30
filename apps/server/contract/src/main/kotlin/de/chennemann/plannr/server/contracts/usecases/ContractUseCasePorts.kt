package de.chennemann.plannr.server.contracts.usecases

import de.chennemann.plannr.server.contracts.domain.Contract

interface ContractRecurringTransactionCascade {
    suspend fun archiveFor(contract: Contract)
    suspend fun unarchiveFor(contract: Contract)
}
