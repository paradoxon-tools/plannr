package de.chennemann.plannr.server.accounts.application

import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.common.error.NotFoundException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetAccountTest {
    @Test
    fun `returns account by id`() = runTest {
        val repository = InMemoryAccountRepository()
        val account = AccountFixtures.account()
        repository.save(account)
        val getAccount = GetAccount(repository)

        val result = getAccount(AccountFixtures.DEFAULT_ID)

        assertEquals(account, result)
    }

    @Test
    fun `returns not found for unknown account`() = runTest {
        val repository = InMemoryAccountRepository()
        val getAccount = GetAccount(repository)

        assertFailsWith<NotFoundException> {
            getAccount(AccountFixtures.DEFAULT_ID)
        }
    }
}
