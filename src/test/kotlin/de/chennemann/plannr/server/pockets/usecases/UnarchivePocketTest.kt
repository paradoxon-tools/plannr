package de.chennemann.plannr.server.pockets.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UnarchivePocketTest {
    @Test
    fun `unarchives pocket`() = runTest {
        val repository = InMemoryPocketRepository()
        repository.save(PocketFixtures.pocket(isArchived = true))
        val unarchivePocket = UnarchivePocketUseCase(repository)

        val result = unarchivePocket(PocketFixtures.DEFAULT_ID)

        assertEquals(false, result.isArchived)
        assertEquals(false, repository.findById(PocketFixtures.DEFAULT_ID)?.isArchived)
    }

    @Test
    fun `fails for unknown pocket`() = runTest {
        val unarchivePocket = UnarchivePocketUseCase(InMemoryPocketRepository())

        assertFailsWith<NotFoundException> {
            unarchivePocket(PocketFixtures.DEFAULT_ID)
        }
    }
}
