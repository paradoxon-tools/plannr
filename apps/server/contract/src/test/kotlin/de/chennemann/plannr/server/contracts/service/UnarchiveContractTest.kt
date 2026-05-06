package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.FakePartnerService
import de.chennemann.plannr.server.contracts.support.FakePocketService
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
        val contractService = ContractServiceImpl(
            contractRepository = repository,
            pocketService = FakePocketService(),
            partnerService = FakePartnerService(),
            recurringTransactionCascade = archiveCascade,
            timeProvider = { 0L },
        )

        val result = contractService.unarchive(ContractFixtures.DEFAULT_ID)

        assertEquals(false, result.isArchived)
        assertEquals(false, repository.findById(ContractFixtures.DEFAULT_ID)?.isArchived)
        assertEquals(listOf(ContractFixtures.DEFAULT_ID), archiveCascade.unarchivedContracts)
    }

    @Test
    fun `fails for unknown contract`() = runTest {
        val contractService = ContractServiceImpl(
            contractRepository = InMemoryContractRepository(),
            pocketService = FakePocketService(),
            partnerService = FakePartnerService(),
            recurringTransactionCascade = RecordingContractRecurringTransactionCascade(),
            timeProvider = { 0L },
        )

        assertFailsWith<NotFoundException> {
            contractService.unarchive(ContractFixtures.DEFAULT_ID)
        }
    }
}

