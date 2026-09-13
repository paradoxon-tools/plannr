package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.pockets.persistence.toDTO
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreatePocketTest {
    @Test
    fun `creates regular pocket when account exists`() = runTest {
        val pocketRepository = InMemoryPocketRepository()
        val pocketService = PocketServiceImpl(
            pocketRepository = pocketRepository,
            accountService = StubAccountService(),
            contractPresentationService = NoOpContractPresentationService,
            transactionTemplateService = NoOpTransactionTemplateService,
            timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
        )

        val created = pocketService.create(PocketFixtures.createPocketCommand())

        assertEquals(PocketFixtures.DEFAULT_ACCOUNT_ID, created.accountId)
        assertEquals(null, created.contractId)
        assertEquals(created, pocketRepository.findResolvedById(created.id)?.toDTO())
    }

    @Test
    fun `creates dedicated pocket for contract`() = runTest {
        val pocketRepository = InMemoryPocketRepository()
        val pocketService = pocketService(repository = pocketRepository)

        val created = pocketService.createForContract(createPocketForContractCommand())

        assertEquals(PocketFixtures.DEFAULT_ACCOUNT_ID, created.accountId)
        assertEquals(42L, created.contractId)
        assertEquals(false, created.isDefault)
        assertEquals(created, pocketRepository.findResolvedById(created.id)?.toDTO())
    }

    @Test
    fun `creates dedicated pocket for saving goal`() = runTest {
        val pocketRepository = InMemoryPocketRepository()
        val pocketService = pocketService(repository = pocketRepository)

        val created = pocketService.createForSavingGoal(
            CreatePocketForSavingGoalCommand(
                accountId = PocketFixtures.DEFAULT_ACCOUNT_ID,
                savingGoalId = 24L,
                name = "Emergency fund",
                description = "Six months of expenses",
                color = 42,
            ),
        )

        assertEquals(24L, created.savingGoalId)
        assertEquals("Emergency fund", created.name)
        assertEquals(false, created.isDefault)
        assertEquals(created, pocketRepository.findResolvedById(created.id)?.toDTO())
    }

    @Test
    fun `fails when account does not exist`() = runTest {
        val pocketService = PocketServiceImpl(
            pocketRepository = InMemoryPocketRepository(),
            accountService = StubAccountService { false },
            contractPresentationService = NoOpContractPresentationService,
            transactionTemplateService = NoOpTransactionTemplateService,
            timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
        )

        assertFailsWith<NotFoundException> {
            pocketService.create(PocketFixtures.createPocketCommand())
        }
    }

    private fun createPocketForContractCommand(
        accountId: Long = PocketFixtures.DEFAULT_ACCOUNT_ID,
        contractId: Long = 42L,
    ): CreatePocketForContractCommand =
        CreatePocketForContractCommand(
            accountId = accountId,
            contractId = contractId,
        )
}
