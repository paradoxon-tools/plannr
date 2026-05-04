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
        cleanDatabase("accounts")
    }

    @Test
    fun `saves and finds account by id`() = runBlocking {
        val account = AccountFixtures.account()

        accountRepository.insert(
            id = account.id,
            name = account.name,
            institution = account.institution,
            currencyCode = account.currencyCode,
            weekendHandling = account.weekendHandling,
            isArchived = account.isArchived,
            createdAt = account.createdAt,
        )

        assertEquals(account, accountRepository.findById(AccountFixtures.DEFAULT_ID)?.toDomain())
        assertNull(accountRepository.findById("acc_missing"))
    }

    @Test
    fun `updates and finds account by id`() = runBlocking {
        val original = AccountFixtures.account()
        accountRepository.insert(
            id = original.id,
            name = original.name,
            institution = original.institution,
            currencyCode = original.currencyCode,
            weekendHandling = original.weekendHandling,
            isArchived = original.isArchived,
            createdAt = original.createdAt,
        )
        val updated = AccountFixtures.account(name = "Updated", institution = "Updated Bank", weekendHandling = "NO_SHIFT")

        accountRepository.update(
            id = updated.id,
            name = updated.name,
            institution = updated.institution,
            currencyCode = updated.currencyCode,
            weekendHandling = updated.weekendHandling,
            isArchived = updated.isArchived,
        )

        assertEquals(updated, accountRepository.findById(AccountFixtures.DEFAULT_ID)?.toDomain())
    }
}
