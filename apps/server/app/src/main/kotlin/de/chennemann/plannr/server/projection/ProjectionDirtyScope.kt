package de.chennemann.plannr.server.projection

data class ProjectionDirtyScope(
    val scopeType: String,
    val scopeId: String,
    val markedAt: Long,
)

interface ProjectionDirtyScopeRepository {
    suspend fun mark(scopeType: String, scopeId: String, markedAt: Long): ProjectionDirtyScope
    suspend fun listAll(): List<ProjectionDirtyScope>
    suspend fun clear(scopeType: String, scopeId: String)
    suspend fun clearAll()
}
