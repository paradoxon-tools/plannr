package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.contracts.support.RecordingContractRecurringTransactionCascade
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UnarchiveContractTest {
    @Test
    fun `unarchives contract`() = runTest {
        val repository = InMemoryContractRepository()
        repository.save(ContractFixtures.contract(isArchived = true))
        val archiveCascade = RecordingContractRecurringTransactionCascade()
        val unarchiveContract = UnarchiveContractService(repository, archiveCascade)

        val result = unarchiveContract(ContractFixtures.DEFAULT_ID)

        assertEquals(false, result.isArchived)
        assertEquals(false, repository.findById(ContractFixtures.DEFAULT_ID)?.isArchived)
        assertEquals(listOf(ContractFixtures.DEFAULT_ID), archiveCascade.unarchivedContracts)
    }

    @Test
    fun `fails for unknown contract`() = runTest {
        val unarchiveContract = UnarchiveContractService(InMemoryContractRepository(), RecordingContractRecurringTransactionCascade())

        assertFailsWith<NotFoundException> {
            unarchiveContract(ContractFixtures.DEFAULT_ID)
        }
    }
}

