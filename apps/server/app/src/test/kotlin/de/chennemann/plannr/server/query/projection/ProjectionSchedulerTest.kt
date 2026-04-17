package de.chennemann.plannr.server.query.projection

import de.chennemann.plannr.server.projection.ProjectionDirtyScope
import de.chennemann.plannr.server.projection.ProjectionDirtyScopeService
import de.chennemann.plannr.server.projection.ProjectionDirtyScopeService.ScopeType
import de.chennemann.plannr.server.projection.ProjectionRebuilder
import de.chennemann.plannr.server.projection.ProjectionScheduler
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProjectionSchedulerTest {
    @Test
    fun `failures leave dirty scopes for retry and successful retry clears them`() = runTest {
        val repository = InMemoryProjectionDirtyScopeRepository().apply {
            mark(ProjectionDirtyScopeService.ScopeType.ACCOUNT.name, "acc_123", 1)
        }
        val rebuilder = FlakyProjectionRebuilder()
        val scheduler = ProjectionScheduler(repository, rebuilder)

        assertFailsWith<IllegalStateException> {
            scheduler.processDirtyScopes()
        }
        assertEquals(
            listOf(ProjectionDirtyScope(ScopeType.ACCOUNT.name, "acc_123", 1)),
            repository.listAll(),
        )

        scheduler.processDirtyScopes()

        assertEquals(emptyList(), repository.listAll())
        assertEquals(listOf("account:acc_123", "account:acc_123"), rebuilder.calls)
    }

    private class FlakyProjectionRebuilder : ProjectionRebuilder {
        val calls = mutableListOf<String>()
        private var shouldFail = true

        override suspend fun rebuildAccountFeed(accountId: String) {
            calls += "account:$accountId"
            if (shouldFail) {
                shouldFail = false
                throw IllegalStateException("boom")
            }
        }

        override suspend fun rebuildPocketFeed(pocketId: String) {
            calls += "pocket:$pocketId"
        }

        override suspend fun rebuildAll() {
            calls += "full"
        }
    }
}
