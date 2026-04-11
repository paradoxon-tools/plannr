package de.chennemann.plannr.server.query.projection

import kotlin.system.measureTimeMillis
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ProjectionScheduler(
    private val dirtyScopeRepository: ProjectionDirtyScopeRepository,
    private val projectionService: ProjectionRebuilder,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "\${plannr.projections.dirty-scope-delay-ms:30000}")
    suspend fun processDirtyScopes() {
        val scopes = dirtyScopeRepository.listAll()
        val elapsedMs = measureTimeMillis {
            scopes.forEach { scope ->
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
                        return@measureTimeMillis
                    }
                }
            }
        }
        if (scopes.isNotEmpty()) {
            logger.info("Processed {} dirty projection scope(s) in {} ms", scopes.size, elapsedMs)
        }
    }

    @Scheduled(fixedDelayString = "\${plannr.projections.full-rebuild-delay-ms:3600000}")
    suspend fun runFullRebuildSafetyJob() {
        val elapsedMs = measureTimeMillis {
            projectionService.rebuildAll()
        }
        logger.info("Completed projection full rebuild safety job in {} ms", elapsedMs)
    }
}
