package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.common.error.ConflictException
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

class CreateContractTest {
    @Test
    fun `creates contract metadata for pocket when optional partner is valid`() = runTest {
        val contractRepository = InMemoryContractRepository()
        val service = ContractServiceImpl(
            contractRepository = contractRepository,
            partnerService = FakePartnerService(listOf(ContractTestPartners.partner())),
        )

        val created = service.create(ContractTestPockets.pocket(), ContractFixtures.createContractCommand())

        assertEquals(ContractFixtures.DEFAULT_ACCOUNT_ID, created.accountId)
        assertEquals(created.id, contractRepository.findById(created.id)?.pocketId)
    }

    @Test
    fun `creates contract metadata without partner`() = runTest {
        val contractRepository = InMemoryContractRepository()
        val service = ContractServiceImpl(
            contractRepository = contractRepository,
            partnerService = FakePartnerService(emptyList()),
        )

        val created = service.create(ContractTestPockets.pocket(), ContractFixtures.createContractCommand(partnerId = null))

        assertEquals(null, created.contractInfo.partnerId)
    }

    @Test
    fun `fails when pocket already has contract metadata`() = runTest {
        val contractRepository = InMemoryContractRepository()
        contractRepository.save(ContractFixtures.contractModel())
        val service = ContractServiceImpl(
            contractRepository = contractRepository,
            partnerService = FakePartnerService(listOf(ContractTestPartners.partner())),
        )

        assertFailsWith<ConflictException> {
            service.create(ContractTestPockets.pocket(), ContractFixtures.createContractCommand())
        }
    }

    @Test
    fun `fails when partner does not exist`() = runTest {
        val service = ContractServiceImpl(
            contractRepository = InMemoryContractRepository(),
            partnerService = FakePartnerService(emptyList()),
        )

        assertFailsWith<NotFoundException> {
            service.create(ContractTestPockets.pocket(), ContractFixtures.createContractCommand())
        }
    }
}
