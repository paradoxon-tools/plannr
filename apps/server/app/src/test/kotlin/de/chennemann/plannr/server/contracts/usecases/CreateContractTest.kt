package de.chennemann.plannr.server.contracts.usecases

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.persistence.toModel
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.support.FakePartnerService
import de.chennemann.plannr.server.support.FakePocketService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreateContractTest {
    @Test
    fun `creates contract when pocket exists and optional partner is valid`() = runTest {
        val contractRepository = InMemoryContractRepository()
        val createContract = CreateContractUseCase(
            contractRepository = contractRepository,
            pocketService = FakePocketService(listOf(PocketFixtures.pocket())),
            partnerService = FakePartnerService(),
            timeProvider = { ContractFixtures.DEFAULT_CREATED_AT },
        )

        val created = createContract(ContractFixtures.createContractCommand())

        assertEquals(ContractFixtures.DEFAULT_ACCOUNT_ID, created.accountId)
        assertEquals(created, contractRepository.findById(created.id))
    }

    @Test
    fun `creates contract without partner`() = runTest {
        val contractRepository = InMemoryContractRepository()
        val createContract = CreateContractUseCase(
            contractRepository = contractRepository,
            pocketService = FakePocketService(listOf(PocketFixtures.pocket())),
            partnerService = FakePartnerService(emptyList()),
            timeProvider = { ContractFixtures.DEFAULT_CREATED_AT },
        )

        val created = createContract(ContractFixtures.createContractCommand(partnerId = null))

        assertEquals(null, created.partnerId)
    }

    @Test
    fun `fails when pocket already has a contract`() = runTest {
        val contractRepository = InMemoryContractRepository()
        contractRepository.save(ContractFixtures.contract().toModel())
        val createContract = CreateContractUseCase(
            contractRepository = contractRepository,
            pocketService = FakePocketService(listOf(PocketFixtures.pocket())),
            partnerService = FakePartnerService(),
            timeProvider = { ContractFixtures.DEFAULT_CREATED_AT },
        )

        assertFailsWith<ConflictException> {
            createContract(ContractFixtures.createContractCommand())
        }
    }

    @Test
    fun `fails when pocket does not exist`() = runTest {
        val createContract = CreateContractUseCase(
            contractRepository = InMemoryContractRepository(),
            pocketService = FakePocketService(emptyList()),
            partnerService = FakePartnerService(emptyList()),
            timeProvider = { ContractFixtures.DEFAULT_CREATED_AT },
        )

        assertFailsWith<NotFoundException> {
            createContract(ContractFixtures.createContractCommand())
        }
    }

    @Test
    fun `fails when partner does not exist`() = runTest {
        val createContract = CreateContractUseCase(
            contractRepository = InMemoryContractRepository(),
            pocketService = FakePocketService(listOf(PocketFixtures.pocket())),
            partnerService = FakePartnerService(emptyList()),
            timeProvider = { ContractFixtures.DEFAULT_CREATED_AT },
        )

        assertFailsWith<NotFoundException> {
            createContract(ContractFixtures.createContractCommand())
        }
    }
}
