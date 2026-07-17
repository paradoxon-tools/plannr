package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.pockets.api.dto.CreateContractCommand
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
    fun `creates pocket when account exists`() = runTest {
        val pocketRepository = InMemoryPocketRepository()
        val pocketService = PocketServiceImpl(
            pocketRepository = pocketRepository,
            accountLookup = PocketAccountLookup { true },
            contractService = NoOpContractService,
            transactionTemplateService = NoOpTransactionTemplateService,
            timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
        )

        val created = pocketService.create(PocketFixtures.createPocketCommand())

        assertEquals(PocketFixtures.DEFAULT_ACCOUNT_ID, created.accountId)
        assertEquals(false, created.isContractPocket)
        assertEquals(created, pocketRepository.findById(created.id)?.toDTO())
    }

    @Test
    fun `creates contract pocket with nested contract metadata`() = runTest {
        val pocketRepository = InMemoryPocketRepository()
        val contractService = RecordingContractService()
        val pocketService = PocketServiceImpl(
            pocketRepository = pocketRepository,
            accountLookup = PocketAccountLookup { true },
            contractService = contractService,
            transactionTemplateService = NoOpTransactionTemplateService,
            timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
        )
        val contract = CreateContractCommand(
            partnerId = null,
            signingDate = "2026-01-01",
            expirationDate = null,
            lastCancellationDate = null,
        )

        val created = pocketService.create(PocketFixtures.createPocketCommand(contract = contract))

        assertEquals(true, created.isContractPocket)
        assertEquals(listOf(created to contract), contractService.createdContracts)
    }

    @Test
    fun `creates contract on account default pocket when requested`() = runTest {
        val pocketRepository = InMemoryPocketRepository()
        val defaultPocket = pocketRepository.save(PocketFixtures.pocket(isDefault = true))
        val contractService = RecordingContractService()
        val pocketService = PocketServiceImpl(
            pocketRepository = pocketRepository,
            accountLookup = PocketAccountLookup { true },
            contractService = contractService,
            transactionTemplateService = NoOpTransactionTemplateService,
            timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
        )
        val contract = CreateContractCommand(
            partnerId = null,
            signingDate = "2026-01-01",
            expirationDate = null,
            lastCancellationDate = null,
            useDefaultPocket = true,
        )

        val created = pocketService.create(PocketFixtures.createPocketCommand(name = "Ignored contract pocket", contract = contract))

        assertEquals(defaultPocket.id, created.id)
        assertEquals(defaultPocket.name, created.name)
        assertTrue(created.isDefault)
        assertTrue(created.isContractPocket)
        assertEquals(1, pocketRepository.count())
        assertEquals(listOf(created to contract), contractService.createdContracts)
    }

    @Test
    fun `fails when default pocket is requested but does not exist`() = runTest {
        val pocketRepository = InMemoryPocketRepository()
        val contractService = RecordingContractService()
        val pocketService = PocketServiceImpl(
            pocketRepository = pocketRepository,
            accountLookup = PocketAccountLookup { true },
            contractService = contractService,
            transactionTemplateService = NoOpTransactionTemplateService,
            timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
        )
        val contract = CreateContractCommand(
            partnerId = null,
            signingDate = null,
            expirationDate = null,
            lastCancellationDate = null,
            useDefaultPocket = true,
        )

        val error = assertFailsWith<NotFoundException> {
            pocketService.create(PocketFixtures.createPocketCommand(contract = contract))
        }

        assertEquals("Default pocket not found", error.message)
        assertEquals(0, pocketRepository.count())
        assertTrue(contractService.createdContracts.isEmpty())
    }

    @Test
    fun `fails when account does not exist`() = runTest {
        val pocketService = PocketServiceImpl(
            pocketRepository = InMemoryPocketRepository(),
            accountLookup = PocketAccountLookup { false },
            contractService = NoOpContractService,
            transactionTemplateService = NoOpTransactionTemplateService,
            timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
        )

        assertFailsWith<NotFoundException> {
            pocketService.create(PocketFixtures.createPocketCommand())
        }
    }
}
