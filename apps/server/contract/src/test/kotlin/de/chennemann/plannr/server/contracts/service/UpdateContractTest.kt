package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateContractTest {
    @Test
    fun `updates existing contract metadata`() = runTest {
        val pockets = FakePocketService(listOf(contractPocket()))
        val repository = InMemoryContractRepository { pockets.pockets.values }
        repository.save(ContractFixtures.contractModel())
        val service = ContractServiceImpl(
            contractRepository = repository,
            partnerService = FakePartnerService(
                listOf(
                    ContractTestPartners.partner(),
                    ContractTestPartners.partner(id = 2L, name = "Telecom GmbH"),
                ),
            ),
            pocketService = pockets,
        )

        val updated = service.update(
            ContractFixtures.updateContractCommand(
                partnerId = 2L,
                signingDate = "2024-02-01",
                expirationDate = null,
            ),
        )

        assertEquals(ContractFixtures.DEFAULT_POCKET_ID, updated.id)
        assertEquals(ContractFixtures.DEFAULT_POCKET_ID, updated.pocketId)
        assertEquals(2L, updated.partnerId)
        assertEquals("2024-02-01", updated.signingDate)
        assertEquals(null, updated.expirationDate)
    }

    @Test
    fun `fails when contract does not exist`() = runTest {
        val pockets = FakePocketService(listOf(contractPocket()))
        val repository = InMemoryContractRepository { pockets.pockets.values }
        val service = ContractServiceImpl(
            contractRepository = repository,
            partnerService = FakePartnerService(emptyList()),
            pocketService = pockets,
        )

        assertFailsWith<NotFoundException> {
            service.update(ContractFixtures.updateContractCommand())
        }
    }

    private fun contractPocket(): Pocket =
        Pocket(
            id = ContractFixtures.DEFAULT_POCKET_ID,
            accountId = ContractFixtures.DEFAULT_ACCOUNT_ID,
            name = "Bills",
            description = "Monthly fixed costs",
            color = 123456,
            isDefault = false,
            isContractPocket = true,
            isArchived = false,
            createdAt = 1_710_000_100L,
        )
}
