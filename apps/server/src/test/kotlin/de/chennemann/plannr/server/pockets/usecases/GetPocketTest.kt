package de.chennemann.plannr.server.pockets.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetPocketTest {
    @Test
    fun `returns pocket by id`() = runTest {
        val repository = InMemoryPocketRepository()
        val pocket = PocketFixtures.pocket()
        repository.save(pocket)
        val getPocket = GetPocketUseCase(repository)

        val result = getPocket(PocketFixtures.DEFAULT_ID)

        assertEquals(pocket, result)
    }

    @Test
    fun `fails for unknown pocket`() = runTest {
        val getPocket = GetPocketUseCase(InMemoryPocketRepository())

        assertFailsWith<NotFoundException> {
            getPocket(PocketFixtures.DEFAULT_ID)
        }
    }
}
