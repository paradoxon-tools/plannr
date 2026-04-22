package de.chennemann.plannr.server.accounts.service

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
        accountRepository.save(AccountFixtures.account(isArchived = true))
        accountRepository.save(AccountFixtures.account(id = "acc_456", name = "Savings", isArchived = true))
        val archiveCascade = RecordingAccountArchiveCascade()
        val accountService = accountService(accountRepository = accountRepository, archiveCascade = archiveCascade)

        val result = accountService.unarchive(AccountFixtures.DEFAULT_ID)

        assertEquals(false, result.isArchived)
        assertEquals(false, accountRepository.findById(AccountFixtures.DEFAULT_ID)?.isArchived)
        assertEquals(listOf(AccountFixtures.DEFAULT_ID), archiveCascade.unarchivedAccountIds)
    }

    @Test
    fun `returns not found for unknown account`() = runTest {
        val accountService = accountService(accountRepository = InMemoryAccountRepository())

        assertFailsWith<NotFoundException> {
            accountService.unarchive("acc_missing")
        }
    }
}
