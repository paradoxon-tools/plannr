package de.chennemann.plannr.server.pockets.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.recurringtransactions.support.InMemoryRecurringTransactionRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UnarchivePocketTest {
    @Test
    fun `unarchives pocket and its contract`() = runTest {
        val repository = InMemoryPocketRepository()
        val contractRepository = InMemoryContractRepository()
        repository.save(PocketFixtures.pocket(isArchived = true))
        contractRepository.save(ContractFixtures.contract(pocketId = PocketFixtures.DEFAULT_ID, isArchived = true))
        val unarchivePocket = UnarchivePocketUseCase(repository, contractRepository, InMemoryRecurringTransactionRepository())

        val result = unarchivePocket(PocketFixtures.DEFAULT_ID)

        assertEquals(false, result.isArchived)
        assertEquals(false, repository.findById(PocketFixtures.DEFAULT_ID)?.isArchived)
        assertEquals(false, contractRepository.findByPocketId(PocketFixtures.DEFAULT_ID)?.isArchived)
    }

    @Test
    fun `fails for unknown pocket`() = runTest {
        val unarchivePocket = UnarchivePocketUseCase(InMemoryPocketRepository(), InMemoryContractRepository(), InMemoryRecurringTransactionRepository())

        assertFailsWith<NotFoundException> {
            unarchivePocket(PocketFixtures.DEFAULT_ID)
        }
    }
}
