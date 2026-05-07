package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.pockets.contracts.support.ContractFixtures
import de.chennemann.plannr.server.pockets.contracts.support.ContractTestPartners
import de.chennemann.plannr.server.pockets.contracts.support.ContractTestPockets
import de.chennemann.plannr.server.pockets.contracts.support.FakePartnerService
import de.chennemann.plannr.server.pockets.contracts.support.InMemoryContractRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateContractTest {
    @Test
    fun `updates existing contract metadata`() = runTest {
        val contractRepository = InMemoryContractRepository()
        contractRepository.save(ContractFixtures.contractModel())
        val service = ContractServiceImpl(
            contractRepository = contractRepository,
            partnerService = FakePartnerService(
                listOf(
                    ContractTestPartners.partner(),
                    ContractTestPartners.partner(id = 2L, name = "Telecom GmbH"),
                ),
            ),
        )

        val updated = service.update(
            ContractTestPockets.pocket(),
            ContractFixtures.updateContractCommand(
                partnerId = 2L,
                signingDate = "2024-02-01",
                expirationDate = null,
            ),
        )

        assertEquals(1L, updated.accountId)
        assertEquals(1L, updated.id)
        assertEquals(2L, updated.contractInfo.partnerId)
        assertEquals("Bills", updated.name)
        assertEquals(null, updated.contractInfo.expirationDate)
    }

    @Test
    fun `fails when contract metadata does not exist`() = runTest {
        val service = ContractServiceImpl(
            contractRepository = InMemoryContractRepository(),
            partnerService = FakePartnerService(emptyList()),
        )

        assertFailsWith<NotFoundException> {
            service.update(ContractTestPockets.pocket(), ContractFixtures.updateContractCommand())
        }
    }
}
