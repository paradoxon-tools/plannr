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

class CoroutineAccountRepositoryTest : ApiIntegrationTest() {
    @Autowired
    lateinit var accountRepository: AccountRepository

    @BeforeEach
    fun setUp() {
        cleanDatabase("accounts")
    }

    @Test
    fun `saves and finds account by id`() = runBlocking {
        val account = AccountFixtures.account()

        val saved = accountRepository.save(account.toModel().copy(id = null)).toDomain()

        assertEquals(account.copy(id = saved.id), accountRepository.findById(saved.id)?.toDomain())
        assertNull(accountRepository.findById(999L))
    }

    @Test
    fun `updates and finds account by id`() = runBlocking {
        val original = AccountFixtures.account()
        val saved = accountRepository.save(original.toModel().copy(id = null)).toDomain()
        val updated = AccountFixtures.account(
            id = saved.id,
            name = "Updated",
            institution = "Updated Bank",
            weekendHandling = "NO_SHIFT",
        )

        accountRepository.save(updated.toModel())

        assertEquals(updated, accountRepository.findById(saved.id)?.toDomain())
    }

    @Test
    fun `finds account by name and institution`() = runBlocking {
        val account = accountRepository.save(
            AccountFixtures.account(name = "Primary Account", institution = "Demo Bank").toModel().copy(id = null),
        ).toDomain()
        accountRepository.save(
            AccountFixtures.account(name = "Primary Account", institution = "Other Bank").toModel().copy(id = null),
        )

        assertEquals(account, accountRepository.findByNameAndInstitution("Primary Account", "Demo Bank")?.toDomain())
        assertNull(accountRepository.findByNameAndInstitution("Missing Account", "Demo Bank"))
        assertNull(accountRepository.findByNameAndInstitution("Primary Account", "Missing Bank"))
    }
}
