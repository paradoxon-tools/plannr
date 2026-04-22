package de.chennemann.plannr.server.accounts.persistence

import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.support.ApiIntegrationTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertNull

class R2dbcAccountRepositoryTest : ApiIntegrationTest() {
    @Autowired
    lateinit var accountRepository: AccountRepository

    @BeforeEach
    fun setUp() {
        cleanDatabase("accounts", "currencies")
        runBlocking {
            databaseClient.sql("INSERT INTO currencies (code, name, symbol, decimal_places, symbol_position) VALUES ('EUR', 'Euro', '€', 2, 'before')")
                .fetch().rowsUpdated().block()
        }
    }

    @Test
    fun `saves and finds account by id`() = runBlocking {
        val account = AccountFixtures.account()

        accountRepository.save(account)

        assertEquals(account, accountRepository.findById(AccountFixtures.DEFAULT_ID))
        assertNull(accountRepository.findById("acc_missing"))
    }

    @Test
    fun `updates and finds account by id`() = runBlocking {
        accountRepository.save(AccountFixtures.account())
        val updated = AccountFixtures.account(name = "Updated", institution = "Updated Bank", weekendHandling = "NO_SHIFT")

        accountRepository.update(updated)

        assertEquals(updated, accountRepository.findById(AccountFixtures.DEFAULT_ID))
    }

}
