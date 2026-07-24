package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreateContractTest {
    @Test
    fun `creates pocket before contract metadata`() = runTest {
        val pockets = FakePocketService()
        val repository = InMemoryContractRepository { pockets.pockets.values }
        val service = contractService(repository, pockets)

        val created = service.create(ContractFixtures.createContractCommand())

        assertEquals(created.id, created.pocketId)
        assertEquals(ContractFixtures.DEFAULT_PARTNER_ID, created.partnerId)
        assertEquals(created.id, repository.findById(created.id)?.pocketId)
        assertEquals(1, pockets.contractCreateCommands.size)
    }

    @Test
    fun `creates contract metadata without partner`() = runTest {
        val pockets = FakePocketService()
        val repository = InMemoryContractRepository { pockets.pockets.values }
        val service = contractService(repository, pockets, partners = emptyList())

        val created = service.create(ContractFixtures.createContractCommand(partnerId = null))

        assertEquals(null, created.partnerId)
    }

    @Test
    fun `fails before creating pocket when partner does not exist`() = runTest {
        val pockets = FakePocketService()
        val repository = InMemoryContractRepository { pockets.pockets.values }
        val service = contractService(repository, pockets, partners = emptyList())

        assertFailsWith<NotFoundException> {
            service.create(ContractFixtures.createContractCommand())
        }
        assertEquals(0, pockets.contractCreateCommands.size)
    }

    @Test
    fun `fails when selected default pocket already has a contract`() = runTest {
        val defaultPocket = contractPocket(isDefault = true)
        val pockets = FakePocketService(listOf(defaultPocket))
        val repository = InMemoryContractRepository { pockets.pockets.values }
        repository.save(ContractFixtures.contractModel())
        val service = contractService(repository, pockets)

        assertFailsWith<ConflictException> {
            service.create(ContractFixtures.createContractCommand(useDefaultPocket = true))
        }
    }

    private fun contractService(
        repository: InMemoryContractRepository,
        pockets: FakePocketService,
        partners: List<de.chennemann.plannr.server.partners.api.dto.Partner> = listOf(ContractTestPartners.partner()),
    ): ContractServiceImpl =
        ContractServiceImpl(
            contractRepository = repository,
            partnerService = FakePartnerService(partners),
            pocketService = pockets,
        )

    private fun contractPocket(isDefault: Boolean = false): Pocket =
        Pocket(
            id = ContractFixtures.DEFAULT_POCKET_ID,
            accountId = ContractFixtures.DEFAULT_ACCOUNT_ID,
            name = "Bills",
            description = null,
            color = 123456,
            isDefault = isDefault,
            isContractPocket = true,
            isArchived = false,
            createdAt = 1_710_000_100L,
        )
}
