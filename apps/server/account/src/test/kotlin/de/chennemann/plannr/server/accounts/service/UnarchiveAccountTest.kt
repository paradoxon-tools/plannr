package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.persistence.toModel
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.common.error.NotFoundException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UnarchiveAccountTest {
    @Test
    fun `unarchives account and all pockets and contracts in that account`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        accountRepository.save(AccountFixtures.account(isArchived = true).toModel())
        accountRepository.save(AccountFixtures.account(id = 2L, name = "Savings", isArchived = true).toModel())
        val pocketService = RecordingPocketService()
        val accountService = accountService(accountRepository = accountRepository, pocketService = pocketService)

        val result = accountService.unarchive(AccountFixtures.DEFAULT_ID)

        assertEquals(false, result.isArchived)
        assertEquals(false, accountRepository.findById(AccountFixtures.DEFAULT_ID)?.isArchived)
        assertEquals(listOf(AccountFixtures.DEFAULT_ID), pocketService.unarchivedAccountIds)
    }

    @Test
    fun `returns not found for unknown account`() = runTest {
        val accountService = accountService(accountRepository = InMemoryAccountRepository())

        assertFailsWith<NotFoundException> {
            accountService.unarchive(999L)
        }
    }
}
