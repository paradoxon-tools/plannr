package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.events.NoOpApplicationEventBus
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreatePocketTest {
    @Test
    fun `creates pocket when account exists`() = runTest {
        val pocketRepository = InMemoryPocketRepository()
        val pocketService = PocketServiceImpl(
            pocketRepository = pocketRepository,
            accountLookup = PocketAccountLookup { true },
            archiveCascade = NoOpPocketArchiveCascade,
            balanceProvider = PocketBalanceProvider { 0 },
            timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
            applicationEventBus = NoOpApplicationEventBus,
        )

        val created = pocketService.create(PocketFixtures.createPocketCommand())

        assertEquals(PocketFixtures.DEFAULT_ACCOUNT_ID, created.accountId)
        assertEquals(created, pocketRepository.findById(created.id))
    }

    @Test
    fun `fails when account does not exist`() = runTest {
        val pocketService = PocketServiceImpl(
            pocketRepository = InMemoryPocketRepository(),
            accountLookup = PocketAccountLookup { false },
            archiveCascade = NoOpPocketArchiveCascade,
            balanceProvider = PocketBalanceProvider { 0 },
            timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
            applicationEventBus = NoOpApplicationEventBus,
        )

        assertFailsWith<NotFoundException> {
            pocketService.create(PocketFixtures.createPocketCommand())
        }
    }
}
