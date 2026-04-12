package de.chennemann.plannr.server.pockets.usecases

import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.common.error.NotFoundException
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
        val accountRepository = InMemoryAccountRepository()
        accountRepository.save(AccountFixtures.account())
        val createPocket = CreatePocketUseCase(
            pocketRepository = pocketRepository,
            accountRepository = accountRepository,
            pocketIdGenerator = { PocketFixtures.DEFAULT_ID },
            timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
        )

        val created = createPocket(PocketFixtures.createPocketCommand())

        assertEquals(PocketFixtures.DEFAULT_ID, created.id)
        assertEquals(PocketFixtures.DEFAULT_ACCOUNT_ID, created.accountId)
        assertEquals(created, pocketRepository.findById(PocketFixtures.DEFAULT_ID))
    }

    @Test
    fun `fails when account does not exist`() = runTest {
        val createPocket = CreatePocketUseCase(
            pocketRepository = InMemoryPocketRepository(),
            accountRepository = InMemoryAccountRepository(),
            pocketIdGenerator = { PocketFixtures.DEFAULT_ID },
            timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
        )

        assertFailsWith<NotFoundException> {
            createPocket(PocketFixtures.createPocketCommand())
        }
    }
}
