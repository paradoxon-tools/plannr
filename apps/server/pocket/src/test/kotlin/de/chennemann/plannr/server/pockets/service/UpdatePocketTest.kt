package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.common.error.NotFoundException
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
        pocketRepository.save(PocketFixtures.pocket())
        val pocketService = PocketServiceImpl(
            pocketRepository = pocketRepository,
            accountLookup = PocketAccountLookup { it in setOf(PocketFixtures.DEFAULT_ACCOUNT_ID, "acc_456") },
            archiveCascade = NoOpPocketArchiveCascade,
            balanceProvider = PocketBalanceProvider { 0 },
            pocketIdGenerator = { PocketFixtures.DEFAULT_ID },
            timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
        )

        val updated = pocketService.update(
            PocketFixtures.updatePocketCommand(
                accountId = "acc_456",
                name = "Updated",
                description = "Updated description",
                color = 99,
                isDefault = true,
            ),
        )

        assertEquals("acc_456", updated.accountId)
        assertEquals("Updated", updated.name)
        assertEquals("Updated description", updated.description)
        assertEquals(99, updated.color)
        assertEquals(true, updated.isDefault)
        assertEquals(PocketFixtures.DEFAULT_CREATED_AT, updated.createdAt)
    }

    @Test
    fun `fails when pocket does not exist`() = runTest {
        val pocketService = PocketServiceImpl(
            pocketRepository = InMemoryPocketRepository(),
            accountLookup = PocketAccountLookup { true },
            archiveCascade = NoOpPocketArchiveCascade,
            balanceProvider = PocketBalanceProvider { 0 },
            pocketIdGenerator = { PocketFixtures.DEFAULT_ID },
            timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
        )

        assertFailsWith<NotFoundException> {
            pocketService.update(PocketFixtures.updatePocketCommand())
        }
    }

    @Test
    fun `fails when target account does not exist`() = runTest {
        val pocketRepository = InMemoryPocketRepository()
        pocketRepository.save(PocketFixtures.pocket())
        val pocketService = PocketServiceImpl(
            pocketRepository = pocketRepository,
            accountLookup = PocketAccountLookup { false },
            archiveCascade = NoOpPocketArchiveCascade,
            balanceProvider = PocketBalanceProvider { 0 },
            pocketIdGenerator = { PocketFixtures.DEFAULT_ID },
            timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
        )

        assertFailsWith<NotFoundException> {
            pocketService.update(PocketFixtures.updatePocketCommand(accountId = "acc_missing"))
        }
    }
}
