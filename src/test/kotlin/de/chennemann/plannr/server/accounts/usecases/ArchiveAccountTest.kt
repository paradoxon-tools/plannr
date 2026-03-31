package de.chennemann.plannr.server.accounts.usecases

import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ArchiveAccountTest {
    @Test
    fun `archives account and all pockets in that account`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        val pocketRepository = InMemoryPocketRepository()
        accountRepository.save(AccountFixtures.account())
        accountRepository.save(AccountFixtures.account(id = "acc_456", name = "Savings"))
        pocketRepository.save(PocketFixtures.pocket(id = "poc_1", accountId = AccountFixtures.DEFAULT_ID, isArchived = false))
        pocketRepository.save(PocketFixtures.pocket(id = "poc_2", accountId = AccountFixtures.DEFAULT_ID, isArchived = false))
        pocketRepository.save(PocketFixtures.pocket(id = "poc_3", accountId = "acc_456", isArchived = false))
        val archiveAccount = ArchiveAccountUseCase(accountRepository, pocketRepository)

        val result = archiveAccount(AccountFixtures.DEFAULT_ID)

        assertEquals(true, result.isArchived)
        assertEquals(true, accountRepository.findById(AccountFixtures.DEFAULT_ID)?.isArchived)
        assertEquals(true, pocketRepository.findById("poc_1")?.isArchived)
        assertEquals(true, pocketRepository.findById("poc_2")?.isArchived)
        assertEquals(false, pocketRepository.findById("poc_3")?.isArchived)
    }

    @Test
    fun `returns not found for unknown account`() = runTest {
        val archiveAccount = ArchiveAccountUseCase(InMemoryAccountRepository(), InMemoryPocketRepository())

        assertFailsWith<NotFoundException> {
            archiveAccount("acc_missing")
        }
    }
}
