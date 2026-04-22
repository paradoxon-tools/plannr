package de.chennemann.plannr.server.accounts.usecases

import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.support.FakePocketService
import de.chennemann.plannr.server.transactions.recurring.support.InMemoryRecurringTransactionRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UnarchiveAccountTest {
    @Test
    fun `unarchives account and all pockets and contracts in that account`() = runTest {
        val accountRepository = InMemoryAccountRepository()
        val contractRepository = InMemoryContractRepository()
        accountRepository.save(AccountFixtures.account(isArchived = true))
        accountRepository.save(AccountFixtures.account(id = "acc_456", name = "Savings", isArchived = true))
        val pocketService = FakePocketService(
            initialPockets = listOf(
                PocketFixtures.pocket(id = "poc_1", accountId = AccountFixtures.DEFAULT_ID, isArchived = true),
                PocketFixtures.pocket(id = "poc_2", accountId = AccountFixtures.DEFAULT_ID, isArchived = true),
                PocketFixtures.pocket(id = "poc_3", accountId = "acc_456", isArchived = true),
            ),
            onUnarchive = { pocket -> contractRepository.findByPocketId(pocket.id)?.let { contractRepository.update(it.unarchive()) } },
        )
        contractRepository.save(ContractFixtures.contract(id = "con_1", accountId = AccountFixtures.DEFAULT_ID, pocketId = "poc_1", partnerId = null, isArchived = true))
        contractRepository.save(ContractFixtures.contract(id = "con_2", accountId = "acc_456", pocketId = "poc_3", partnerId = null, isArchived = true))
        val unarchiveAccount = UnarchiveAccountUseCase(accountRepository, pocketService, InMemoryRecurringTransactionRepository())

        val result = unarchiveAccount(AccountFixtures.DEFAULT_ID)

        assertEquals(false, result.isArchived)
        assertEquals(false, accountRepository.findById(AccountFixtures.DEFAULT_ID)?.isArchived)
        assertEquals(false, pocketService.getById("poc_1")?.isArchived)
        assertEquals(false, pocketService.getById("poc_2")?.isArchived)
        assertEquals(true, pocketService.getById("poc_3")?.isArchived)
        assertEquals(false, contractRepository.findById("con_1")?.isArchived)
        assertEquals(true, contractRepository.findById("con_2")?.isArchived)
    }

    @Test
    fun `returns not found for unknown account`() = runTest {
        val unarchiveAccount = UnarchiveAccountUseCase(InMemoryAccountRepository(), FakePocketService(emptyList()), InMemoryRecurringTransactionRepository())

        assertFailsWith<NotFoundException> {
            unarchiveAccount("acc_missing")
        }
    }
}
