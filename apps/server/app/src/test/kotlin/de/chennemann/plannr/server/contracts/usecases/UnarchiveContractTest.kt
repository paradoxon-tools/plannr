package de.chennemann.plannr.server.contracts.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.transactions.recurring.support.InMemoryRecurringTransactionRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UnarchiveContractTest {
    @Test
    fun `unarchives contract`() = runTest {
        val repository = InMemoryContractRepository()
        repository.save(ContractFixtures.contract(isArchived = true))
        val unarchiveContract = UnarchiveContractUseCase(repository, InMemoryRecurringTransactionRepository())

        val result = unarchiveContract(ContractFixtures.DEFAULT_ID)

        assertEquals(false, result.isArchived)
        assertEquals(false, repository.findById(ContractFixtures.DEFAULT_ID)?.isArchived)
    }

    @Test
    fun `fails for unknown contract`() = runTest {
        val unarchiveContract = UnarchiveContractUseCase(InMemoryContractRepository(), InMemoryRecurringTransactionRepository())

        assertFailsWith<NotFoundException> {
            unarchiveContract(ContractFixtures.DEFAULT_ID)
        }
    }
}
