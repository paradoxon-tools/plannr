package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.support.ContractTestPartners
import de.chennemann.plannr.server.contracts.support.ContractTestPockets
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.FakePartnerService
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateContractTest {
    @Test
    fun `updates existing contract`() = runTest {
        val contractRepository = InMemoryContractRepository()
        contractRepository.save(ContractFixtures.contract())
        val updateContract = ContractServiceImpl(
            contractRepository = contractRepository,
            partnerService = FakePartnerService(
                listOf(
                    ContractTestPartners.partner(),
                    ContractTestPartners.partner(id = "par_456", name = "Telecom GmbH"),
                ),
            ),
            timeProvider = { ContractFixtures.DEFAULT_CREATED_AT },
        )

        val targetPocket = ContractTestPockets.pocket(id = "poc_456", accountId = 2L, name = "Rent")
        val updated = updateContract.update(
            targetPocket,
            ContractFixtures.updateContractCommand(
                partnerId = "par_456",
                name = "Updated Contract",
                startDate = "2024-02-01",
                endDate = null,
                notes = null,
            ),
        )

        assertEquals(2L, updated.accountId)
        assertEquals("poc_456", updated.pocketId)
        assertEquals("par_456", updated.partnerId)
        assertEquals("Updated Contract", updated.name)
        assertEquals(null, updated.endDate)
        assertEquals(null, updated.notes)
    }

    @Test
    fun `fails when contract does not exist`() = runTest {
        val updateContract = ContractServiceImpl(
            contractRepository = InMemoryContractRepository(),
            partnerService = FakePartnerService(emptyList()),
            timeProvider = { ContractFixtures.DEFAULT_CREATED_AT },
        )

        assertFailsWith<NotFoundException> {
            updateContract.update(ContractTestPockets.pocket(), ContractFixtures.updateContractCommand())
        }
    }

    @Test
    fun `fails when updated pocket already has another contract`() = runTest {
        val contractRepository = InMemoryContractRepository()
        contractRepository.save(ContractFixtures.contract())
        contractRepository.save(ContractFixtures.contract(id = "con_456", accountId = 2L, pocketId = "poc_456"))
        val updateContract = ContractServiceImpl(
            contractRepository = contractRepository,
            partnerService = FakePartnerService(emptyList()),
            timeProvider = { ContractFixtures.DEFAULT_CREATED_AT },
        )

        assertFailsWith<ConflictException> {
            updateContract.update(
                ContractTestPockets.pocket(id = "poc_456", accountId = 2L, name = "Rent"),
                ContractFixtures.updateContractCommand(partnerId = null),
            )
        }
    }
}

