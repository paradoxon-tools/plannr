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

class UpdatePocketTest {
    @Test
    fun `updates existing pocket`() = runTest {
        val pocketRepository = InMemoryPocketRepository()
        val accountRepository = InMemoryAccountRepository()
        accountRepository.save(AccountFixtures.account())
        val otherAccount = AccountFixtures.account(id = "acc_456", name = "Savings")
        accountRepository.save(otherAccount)
        pocketRepository.save(PocketFixtures.pocket())
        val updatePocket = UpdatePocketUseCase(
            pocketRepository = pocketRepository,
            accountRepository = accountRepository,
        )

        val updated = updatePocket(
            PocketFixtures.updatePocketCommand(
                accountId = otherAccount.id,
                name = "Updated",
                description = "Updated description",
                color = 99,
                isDefault = true,
            ),
        )

        assertEquals(otherAccount.id, updated.accountId)
        assertEquals("Updated", updated.name)
        assertEquals("Updated description", updated.description)
        assertEquals(99, updated.color)
        assertEquals(true, updated.isDefault)
        assertEquals(PocketFixtures.DEFAULT_CREATED_AT, updated.createdAt)
    }

    @Test
    fun `fails when pocket does not exist`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        accountRepository.save(AccountFixtures.account())
        val updatePocket = UpdatePocketUseCase(
            pocketRepository = InMemoryPocketRepository(),
            accountRepository = accountRepository,
        )

        assertFailsWith<NotFoundException> {
            updatePocket(PocketFixtures.updatePocketCommand())
        }
    }

    @Test
    fun `fails when target account does not exist`() = runTest {
        val pocketRepository = InMemoryPocketRepository()
        pocketRepository.save(PocketFixtures.pocket())
        val updatePocket = UpdatePocketUseCase(
            pocketRepository = pocketRepository,
            accountRepository = InMemoryAccountRepository(),
        )

        assertFailsWith<NotFoundException> {
            updatePocket(PocketFixtures.updatePocketCommand(accountId = "acc_missing"))
        }
    }
}
