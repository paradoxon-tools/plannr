package de.chennemann.plannr.server.projection

import de.chennemann.plannr.server.transactions.recurring.service.RecurringTransactionProjectionPort
import org.springframework.stereotype.Component

@Component
class RecurringTransactionProjectionAdapter(
    private val dirtyScopeService: ProjectionDirtyScopeService,
) : RecurringTransactionProjectionPort {
    override suspend fun markAccountDirty(accountId: String) {
        dirtyScopeService.markAccountDirty(accountId)
    }

    override suspend fun markPocketDirty(pocketId: String) {
        dirtyScopeService.markPocketDirty(pocketId)
    }
}

