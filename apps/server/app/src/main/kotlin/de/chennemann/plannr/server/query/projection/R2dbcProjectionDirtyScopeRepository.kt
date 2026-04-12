package de.chennemann.plannr.server.query.projection

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class R2dbcProjectionDirtyScopeRepository(
    private val databaseClient: DatabaseClient,
) : ProjectionDirtyScopeRepository {
    override suspend fun mark(scopeType: String, scopeId: String, markedAt: Long): ProjectionDirtyScope {
        databaseClient.sql(
            """
            INSERT INTO projection_dirty_scope (scope_type, scope_id, marked_at)
            VALUES (:scopeType, :scopeId, :markedAt)
            ON CONFLICT (scope_type, scope_id)
            DO UPDATE SET marked_at = LEAST(projection_dirty_scope.marked_at, EXCLUDED.marked_at)
            """.trimIndent(),
        )
            .bind("scopeType", scopeType)
            .bind("scopeId", scopeId)
            .bind("markedAt", markedAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        return ProjectionDirtyScope(scopeType, scopeId, markedAt)
    }

    override suspend fun listAll(): List<ProjectionDirtyScope> =
        databaseClient.sql(
            """
            SELECT scope_type, scope_id, marked_at
            FROM projection_dirty_scope
            ORDER BY marked_at ASC, scope_type ASC, scope_id ASC
            """.trimIndent(),
        )
            .fetch()
            .all()
            .map {
                ProjectionDirtyScope(
                    scopeType = it.getValue("scope_type") as String,
                    scopeId = it.getValue("scope_id") as String,
                    markedAt = (it.getValue("marked_at") as Number).toLong(),
                )
            }
            .collectList()
            .awaitSingle()

    override suspend fun clear(scopeType: String, scopeId: String) {
        databaseClient.sql("DELETE FROM projection_dirty_scope WHERE scope_type = :scopeType AND scope_id = :scopeId")
            .bind("scopeType", scopeType)
            .bind("scopeId", scopeId)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    override suspend fun clearAll() {
        databaseClient.sql("DELETE FROM projection_dirty_scope")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
