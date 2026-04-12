package de.chennemann.plannr.server.query.projection

import de.chennemann.plannr.server.common.time.TimeProvider
import org.springframework.stereotype.Component

@Component
class ProjectionDirtyScopeService(
    private val repository: ProjectionDirtyScopeRepository,
    private val timeProvider: TimeProvider,
) {
    suspend fun markAccountDirty(accountId: String) {
        repository.mark(ScopeType.ACCOUNT.name, accountId, timeProvider())
    }

    suspend fun markPocketDirty(pocketId: String) {
        repository.mark(ScopeType.POCKET.name, pocketId, timeProvider())
    }

    suspend fun markFullRebuildDirty() {
        repository.mark(ScopeType.FULL.name, "ALL", timeProvider())
    }

    enum class ScopeType {
        ACCOUNT,
        POCKET,
        FULL,
    }
}
