package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.pockets.persistence.toDTO
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CreatePocketTest {
    @Test
    fun `creates regular pocket when account exists`() = runTest {
        val pocketRepository = InMemoryPocketRepository()
        val pocketService = PocketServiceImpl(
            pocketRepository = pocketRepository,
            accountLookup = PocketAccountLookup { true },
            transactionTemplateService = NoOpTransactionTemplateService,
            timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
        )

        val created = pocketService.create(PocketFixtures.createPocketCommand())

        assertEquals(PocketFixtures.DEFAULT_ACCOUNT_ID, created.accountId)
        assertEquals(false, created.isContractPocket)
        assertEquals(created, pocketRepository.findById(created.id)?.toDTO())
    }

    @Test
    fun `creates dedicated pocket for contract`() = runTest {
        val pocketRepository = InMemoryPocketRepository()
        val pocketService = pocketService(repository = pocketRepository)

        val created = pocketService.createForContract(createPocketForContractCommand())

        assertEquals(PocketFixtures.DEFAULT_ACCOUNT_ID, created.accountId)
        assertEquals(PocketFixtures.DEFAULT_NAME, created.name)
        assertEquals(false, created.isDefault)
        assertEquals(true, created.isContractPocket)
        assertEquals(created, pocketRepository.findById(created.id)?.toDTO())
    }

    @Test
    fun `uses account default pocket for contract when requested`() = runTest {
        val pocketRepository = InMemoryPocketRepository()
        val defaultPocket = pocketRepository.save(PocketFixtures.pocket(isDefault = true))
        val pocketService = pocketService(repository = pocketRepository)

        val created = pocketService.createForContract(
            createPocketForContractCommand(
                name = "Ignored contract pocket",
                useDefaultPocket = true,
            ),
        )

        assertEquals(defaultPocket.id, created.id)
        assertEquals(defaultPocket.name, created.name)
        assertTrue(created.isDefault)
        assertTrue(created.isContractPocket)
        assertEquals(1, pocketRepository.count())
    }

    @Test
    fun `fails when default pocket is requested but does not exist`() = runTest {
        val pocketRepository = InMemoryPocketRepository()
        val pocketService = pocketService(repository = pocketRepository)

        val error = assertFailsWith<NotFoundException> {
            pocketService.createForContract(createPocketForContractCommand(useDefaultPocket = true))
        }

        assertEquals("Default pocket not found", error.message)
        assertEquals(0, pocketRepository.count())
    }

    @Test
    fun `fails when account does not exist`() = runTest {
        val pocketService = PocketServiceImpl(
            pocketRepository = InMemoryPocketRepository(),
            accountLookup = PocketAccountLookup { false },
            transactionTemplateService = NoOpTransactionTemplateService,
            timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
        )

        assertFailsWith<NotFoundException> {
            pocketService.create(PocketFixtures.createPocketCommand())
        }
    }

    private fun createPocketForContractCommand(
        accountId: Long = PocketFixtures.DEFAULT_ACCOUNT_ID,
        name: String = PocketFixtures.DEFAULT_NAME,
        description: String? = PocketFixtures.DEFAULT_DESCRIPTION,
        color: Int = PocketFixtures.DEFAULT_COLOR,
        useDefaultPocket: Boolean = false,
    ): CreatePocketForContractCommand =
        CreatePocketForContractCommand(
            accountId = accountId,
            name = name,
            description = description,
            color = color,
            useDefaultPocket = useDefaultPocket,
        )
}
