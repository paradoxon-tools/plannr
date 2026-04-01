package de.chennemann.plannr.server.accounts.usecases

import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ListAccountsTest {
    @Test
    fun `returns empty list when there are no accounts`() = runTest {
        val repository = InMemoryAccountRepository()
        val listAccounts = ListAccountsUseCase(repository)

        val result = listAccounts()

        assertEquals(emptyList<de.chennemann.plannr.server.accounts.domain.Account>(), result)
    }

    @Test
    fun `returns all accounts in repository order`() = runTest {
        val repository = InMemoryAccountRepository()
        val first = AccountFixtures.account(id = "acc_1", createdAt = 1)
        val second = AccountFixtures.account(id = "acc_2", createdAt = 2, name = "Savings")
        repository.save(first)
        repository.save(second)
        val listAccounts = ListAccountsUseCase(repository)

        val result = listAccounts()

        assertEquals(listOf(first, second), result)
    }
}
