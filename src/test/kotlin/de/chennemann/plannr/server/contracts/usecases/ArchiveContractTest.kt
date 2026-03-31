package de.chennemann.plannr.server.contracts.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ArchiveContractTest {
    @Test
    fun `archives contract`() = runTest {
        val repository = InMemoryContractRepository()
        repository.save(ContractFixtures.contract())
        val archiveContract = ArchiveContractUseCase(repository)

        val result = archiveContract(ContractFixtures.DEFAULT_ID)

        assertEquals(true, result.isArchived)
        assertEquals(true, repository.findById(ContractFixtures.DEFAULT_ID)?.isArchived)
    }

    @Test
    fun `fails for unknown contract`() = runTest {
        val archiveContract = ArchiveContractUseCase(InMemoryContractRepository())

        assertFailsWith<NotFoundException> {
            archiveContract(ContractFixtures.DEFAULT_ID)
        }
    }
}
