package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.pockets.persistence.toModel
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ArchivePocketTest {
    @Test
    fun `archives pocket and recurring transactions`() = runTest {
        val repository = InMemoryPocketRepository()
        repository.save(PocketFixtures.pocket(isContractPocket = true).toModel())
        val pocketService = pocketService(repository)

        val result = pocketService.archive(PocketFixtures.DEFAULT_ID)

        assertEquals(true, result.isArchived)
        assertEquals(true, repository.findById(PocketFixtures.DEFAULT_ID)?.isArchived)
    }

    @Test
    fun `fails for unknown pocket`() = runTest {
        val pocketService = pocketService(InMemoryPocketRepository(), RecordingContractService())

        assertFailsWith<NotFoundException> {
            pocketService.archive(PocketFixtures.DEFAULT_ID)
        }
    }
}
