package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.pockets.persistence.toModel
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UnarchivePocketTest {
    @Test
    fun `unarchives pocket and its contract`() = runTest {
        val repository = InMemoryPocketRepository()
        val contractService = RecordingContractService()
        repository.save(PocketFixtures.pocket(isContractPocket = true, isArchived = true).toModel())
        val pocketService = pocketService(repository, contractService)

        val result = pocketService.unarchive(PocketFixtures.DEFAULT_ID)

        assertEquals(false, result.isArchived)
        assertEquals(false, repository.findById(PocketFixtures.DEFAULT_ID)?.isArchived)
        assertEquals(listOf(PocketFixtures.DEFAULT_ID), contractService.unarchivedPocketIds)
    }

    @Test
    fun `fails for unknown pocket`() = runTest {
        val pocketService = pocketService(InMemoryPocketRepository(), RecordingContractService())

        assertFailsWith<NotFoundException> {
            pocketService.unarchive(PocketFixtures.DEFAULT_ID)
        }
    }
}
