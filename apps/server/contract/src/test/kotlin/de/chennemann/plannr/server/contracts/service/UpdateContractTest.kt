package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateContractTest {
    @Test
    fun `updates contract metadata and refreshes attributed templates`() = runTest {
        val repository = InMemoryContractRepository()
        repository.save(ContractFixtures.contractModel())
        val templates = FakeTransactionTemplateService()
        val service = ContractServiceImpl(
            repository,
            FakeFinancialProfileService(),
            FakePartnerService(listOf(ContractTestPartners.partner(), ContractTestPartners.partner(2L))),
            FakePocketService(),
            { ContractFixtures.DEFAULT_CREATED_AT },
            templates,
        )

        val updated = service.update(
            ContractFixtures.updateContractCommand(partnerId = 2L, signingDate = "2024-02-01", expirationDate = null),
        )

        assertEquals(ContractFixtures.DEFAULT_CONTRACT_ID, updated.id)
        assertEquals(2L, updated.partnerId)
        assertEquals("2024-02-01", updated.signingDate)
        assertEquals(listOf(updated.id), templates.refreshedContractIds)
    }

    @Test
    fun `fails when contract does not exist`() = runTest {
        val service = ContractServiceImpl(
            InMemoryContractRepository(),
            FakeFinancialProfileService(),
            FakePartnerService(),
            FakePocketService(),
            { ContractFixtures.DEFAULT_CREATED_AT },
            FakeTransactionTemplateService(),
        )
        assertFailsWith<NotFoundException> { service.update(ContractFixtures.updateContractCommand()) }
    }
}
