package de.chennemann.plannr.server.query.projection

import de.chennemann.plannr.server.projection.ProjectionDirtyScope
import de.chennemann.plannr.server.projection.ProjectionDirtyScopeRepository

class InMemoryProjectionDirtyScopeRepository : ProjectionDirtyScopeRepository {
    private val values = linkedMapOf<Pair<String, String>, ProjectionDirtyScope>()

    override suspend fun mark(scopeType: String, scopeId: String, markedAt: Long): ProjectionDirtyScope {
        val key = scopeType to scopeId
        val existing = values[key]
        val value = ProjectionDirtyScope(scopeType, scopeId, existing?.markedAt?.coerceAtMost(markedAt) ?: markedAt)
        values[key] = value
        return value
    }

    override suspend fun listAll(): List<ProjectionDirtyScope> = values.values.sortedBy { it.markedAt }

    override suspend fun clear(scopeType: String, scopeId: String) {
        values.remove(scopeType to scopeId)
    }

    override suspend fun clearAll() {
        values.clear()
    }
}
