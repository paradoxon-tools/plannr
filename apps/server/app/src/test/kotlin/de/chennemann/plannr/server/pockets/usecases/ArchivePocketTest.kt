package de.chennemann.plannr.server.pockets.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.transactions.recurring.support.InMemoryRecurringTransactionRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ArchivePocketTest {
    @Test
    fun `archives pocket and its contract`() = runTest {
        val repository = InMemoryPocketRepository()
        val contractRepository = InMemoryContractRepository()
        repository.save(PocketFixtures.pocket())
        contractRepository.save(ContractFixtures.contract(pocketId = PocketFixtures.DEFAULT_ID))
        val archivePocket = ArchivePocketUseCase(repository, contractRepository, InMemoryRecurringTransactionRepository())

        val result = archivePocket(PocketFixtures.DEFAULT_ID)

        assertEquals(true, result.isArchived)
        assertEquals(true, repository.findById(PocketFixtures.DEFAULT_ID)?.isArchived)
        assertEquals(true, contractRepository.findByPocketId(PocketFixtures.DEFAULT_ID)?.isArchived)
    }

    @Test
    fun `fails for unknown pocket`() = runTest {
        val archivePocket = ArchivePocketUseCase(InMemoryPocketRepository(), InMemoryContractRepository(), InMemoryRecurringTransactionRepository())

        assertFailsWith<NotFoundException> {
            archivePocket(PocketFixtures.DEFAULT_ID)
        }
    }
}
