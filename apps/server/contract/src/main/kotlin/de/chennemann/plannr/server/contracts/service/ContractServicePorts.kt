package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.contracts.domain.Contract

interface ContractRecurringTransactionCascade {
    suspend fun archiveFor(contract: Contract)
    suspend fun unarchiveFor(contract: Contract)
}

