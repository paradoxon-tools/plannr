package de.chennemann.plannr.server.contracts.usecases

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.support.FakePartnerService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreateContractTest {
    @Test
    fun `creates contract when pocket exists and optional partner is valid`() = runTest {
        val contractRepository = InMemoryContractRepository()
        val pocketRepository = InMemoryPocketRepository()
        pocketRepository.save(PocketFixtures.pocket())
        val createContract = CreateContractUseCase(
            contractRepository = contractRepository,
            pocketRepository = pocketRepository,
            partnerService = FakePartnerService(),
            contractIdGenerator = { ContractFixtures.DEFAULT_ID },
            timeProvider = { ContractFixtures.DEFAULT_CREATED_AT },
        )

        val created = createContract(ContractFixtures.createContractCommand())

        assertEquals(ContractFixtures.DEFAULT_ACCOUNT_ID, created.accountId)
        assertEquals(created, contractRepository.findById(ContractFixtures.DEFAULT_ID))
    }

    @Test
    fun `creates contract without partner`() = runTest {
        val contractRepository = InMemoryContractRepository()
        val pocketRepository = InMemoryPocketRepository()
        pocketRepository.save(PocketFixtures.pocket())
        val createContract = CreateContractUseCase(
            contractRepository = contractRepository,
            pocketRepository = pocketRepository,
            partnerService = FakePartnerService(emptyList()),
            contractIdGenerator = { ContractFixtures.DEFAULT_ID },
            timeProvider = { ContractFixtures.DEFAULT_CREATED_AT },
        )

        val created = createContract(ContractFixtures.createContractCommand(partnerId = null))

        assertEquals(null, created.partnerId)
    }

    @Test
    fun `fails when pocket already has a contract`() = runTest {
        val contractRepository = InMemoryContractRepository()
        val pocketRepository = InMemoryPocketRepository()
        pocketRepository.save(PocketFixtures.pocket())
        contractRepository.save(ContractFixtures.contract())
        val createContract = CreateContractUseCase(
            contractRepository = contractRepository,
            pocketRepository = pocketRepository,
            partnerService = FakePartnerService(),
            contractIdGenerator = { "con_other" },
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
            pocketRepository = InMemoryPocketRepository(),
            partnerService = FakePartnerService(emptyList()),
            contractIdGenerator = { ContractFixtures.DEFAULT_ID },
            timeProvider = { ContractFixtures.DEFAULT_CREATED_AT },
        )

        assertFailsWith<NotFoundException> {
            createContract(ContractFixtures.createContractCommand())
        }
    }

    @Test
    fun `fails when partner does not exist`() = runTest {
        val pocketRepository = InMemoryPocketRepository()
        pocketRepository.save(PocketFixtures.pocket())
        val createContract = CreateContractUseCase(
            contractRepository = InMemoryContractRepository(),
            pocketRepository = pocketRepository,
            partnerService = FakePartnerService(emptyList()),
            contractIdGenerator = { ContractFixtures.DEFAULT_ID },
            timeProvider = { ContractFixtures.DEFAULT_CREATED_AT },
        )

        assertFailsWith<NotFoundException> {
            createContract(ContractFixtures.createContractCommand())
        }
    }
}
