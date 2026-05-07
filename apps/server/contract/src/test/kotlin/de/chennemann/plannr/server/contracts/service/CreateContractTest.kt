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

class CreateContractTest {
    @Test
    fun `creates contract when pocket exists and optional partner is valid`() = runTest {
        val contractRepository = InMemoryContractRepository()
        val createContract = ContractServiceImpl(
            contractRepository = contractRepository,
            partnerService = FakePartnerService(listOf(ContractTestPartners.partner())),
        )

        val created = createContract.create(ContractTestPockets.pocket(), ContractFixtures.createContractCommand())

        assertEquals(ContractFixtures.DEFAULT_ACCOUNT_ID, created.accountId)
        assertEquals(created.id, contractRepository.findById(created.id)?.pocketId)
    }

    @Test
    fun `creates contract without partner`() = runTest {
        val contractRepository = InMemoryContractRepository()
        val createContract = ContractServiceImpl(
            contractRepository = contractRepository,
            partnerService = FakePartnerService(emptyList()),
        )

        val created = createContract.create(ContractTestPockets.pocket(), ContractFixtures.createContractCommand(partnerId = null))

        assertEquals(null, created.contractInfo.partnerId)
    }

    @Test
    fun `fails when pocket already has a contract`() = runTest {
        val contractRepository = InMemoryContractRepository()
        contractRepository.save(ContractFixtures.contractModel())
        val createContract = ContractServiceImpl(
            contractRepository = contractRepository,
            partnerService = FakePartnerService(listOf(ContractTestPartners.partner())),
        )

        assertFailsWith<ConflictException> {
            createContract.create(ContractTestPockets.pocket(), ContractFixtures.createContractCommand())
        }
    }

    @Test
    fun `fails when partner does not exist`() = runTest {
        val createContract = ContractServiceImpl(
            contractRepository = InMemoryContractRepository(),
            partnerService = FakePartnerService(emptyList()),
        )

        assertFailsWith<NotFoundException> {
            createContract.create(ContractTestPockets.pocket(), ContractFixtures.createContractCommand())
        }
    }
}

