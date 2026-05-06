package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.pockets.persistence.toModel
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdatePocketTest {
    @Test
    fun `updates existing pocket`() = runTest {
        val pocketRepository = InMemoryPocketRepository()
        pocketRepository.save(PocketFixtures.pocket(isContractPocket = true).toModel())
        val pocketService = PocketServiceImpl(
            pocketRepository = pocketRepository,
            accountLookup = PocketAccountLookup { it in setOf(PocketFixtures.DEFAULT_ACCOUNT_ID, 2L) },
            contractService = NoOpContractService,
            recurringTransactionService = NoOpRecurringTransactionService,
            timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
        )

        val updated = pocketService.update(
            PocketFixtures.updatePocketCommand(
                accountId = 2L,
                name = "Updated",
                description = "Updated description",
                color = 99,
                isDefault = true,
            ),
        )

        assertEquals(2L, updated.accountId)
        assertEquals("Updated", updated.name)
        assertEquals("Updated description", updated.description)
        assertEquals(99, updated.color)
        assertEquals(true, updated.isDefault)
        assertEquals(true, updated.isContractPocket)
        assertEquals(PocketFixtures.DEFAULT_CREATED_AT, updated.createdAt)
    }

    @Test
    fun `fails when pocket does not exist`() = runTest {
        val pocketService = PocketServiceImpl(
            pocketRepository = InMemoryPocketRepository(),
            accountLookup = PocketAccountLookup { true },
            contractService = NoOpContractService,
            recurringTransactionService = NoOpRecurringTransactionService,
            timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
        )

        assertFailsWith<NotFoundException> {
            pocketService.update(PocketFixtures.updatePocketCommand())
        }
    }

    @Test
    fun `fails when target account does not exist`() = runTest {
        val pocketRepository = InMemoryPocketRepository()
        pocketRepository.save(PocketFixtures.pocket().toModel())
        val pocketService = PocketServiceImpl(
            pocketRepository = pocketRepository,
            accountLookup = PocketAccountLookup { false },
            contractService = NoOpContractService,
            recurringTransactionService = NoOpRecurringTransactionService,
            timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
        )

        assertFailsWith<NotFoundException> {
            pocketService.update(PocketFixtures.updatePocketCommand(accountId = 999L))
        }
    }
}
