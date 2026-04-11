package de.chennemann.plannr.server.query.projection

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ProjectionScheduler(
    private val dirtyScopeRepository: ProjectionDirtyScopeRepository,
    private val projectionService: TransactionQueryProjectionService,
) {
    @Scheduled(fixedDelayString = "\${plannr.projections.dirty-scope-delay-ms:30000}")
    suspend fun processDirtyScopes() {
        dirtyScopeRepository.listAll().forEach { scope ->
            when (scope.scopeType) {
                ProjectionDirtyScopeService.ScopeType.ACCOUNT.name -> {
                    projectionService.rebuildAccountFeed(scope.scopeId)
                    dirtyScopeRepository.clear(scope.scopeType, scope.scopeId)
                }
                ProjectionDirtyScopeService.ScopeType.POCKET.name -> {
                    projectionService.rebuildPocketFeed(scope.scopeId)
                    dirtyScopeRepository.clear(scope.scopeType, scope.scopeId)
                }
                ProjectionDirtyScopeService.ScopeType.FULL.name -> {
                    projectionService.rebuildAll()
                    dirtyScopeRepository.clearAll()
                    return
                }
            }
        }
    }

    @Scheduled(fixedDelayString = "\${plannr.projections.full-rebuild-delay-ms:3600000}")
    suspend fun runFullRebuildSafetyJob() {
        projectionService.rebuildAll()
    }
}
