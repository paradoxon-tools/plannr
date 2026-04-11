package de.chennemann.plannr.server.query.accounts.persistence

import de.chennemann.plannr.server.query.accounts.domain.AccountQuery
import de.chennemann.plannr.server.query.accounts.domain.AccountQueryRepository
import de.chennemann.plannr.server.support.ApiIntegrationTest
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient
import kotlin.test.assertEquals
import kotlin.test.assertNull

class R2dbcAccountQueryRepositoryTest : ApiIntegrationTest() {
    @Autowired lateinit var repository: AccountQueryRepository
    @Autowired lateinit var databaseClient: DatabaseClient

    @BeforeEach
    fun setUp() {
        cleanDatabase("account_query", "accounts", "currencies")
        insertCurrency()
        insertAccount()
    }

    @Test
    fun `save or update and find account query by id`() = runBlocking {
        val created = AccountQuery(
            accountId = "acc_123",
            name = "Main account",
            institution = "Demo Bank",
            currencyCode = "EUR",
            weekendHandling = "NO_SHIFT",
            isArchived = false,
            createdAt = 100L,
            currentBalance = 250L,
        )

        repository.saveOrUpdate(created)

        assertEquals(created, repository.findById("acc_123"))
        assertNull(repository.findById("acc_missing"))
    }

    @Test
    fun `updates current balance and metadata`() = runBlocking {
        repository.saveOrUpdate(
            AccountQuery(
                accountId = "acc_123",
                name = "Main account",
                institution = "Demo Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
                isArchived = false,
                createdAt = 100L,
                currentBalance = 0L,
            ),
        )

        val updated = repository.saveOrUpdate(
            AccountQuery(
                accountId = "acc_123",
                name = "Updated account",
                institution = "Updated Bank",
                currencyCode = "EUR",
                weekendHandling = "MOVE_AFTER",
                isArchived = true,
                createdAt = 100L,
                currentBalance = 999L,
            ),
        )

        assertEquals("Updated account", updated.name)
        assertEquals("Updated Bank", updated.institution)
        assertEquals("MOVE_AFTER", updated.weekendHandling)
        assertEquals(true, updated.isArchived)
        assertEquals(999L, updated.currentBalance)
    }

    private fun insertCurrency() = runBlocking {
        databaseClient.sql("INSERT INTO currencies (code, name, symbol, decimal_places, symbol_position) VALUES ('EUR', 'Euro', '€', 2, 'before')")
            .fetch().rowsUpdated().awaitSingle()
    }

    private fun insertAccount() = runBlocking {
        databaseClient.sql(
            """
            INSERT INTO accounts (id, name, institution, currency_code, weekend_handling, is_archived, created_at)
            VALUES ('acc_123', 'Main account', 'Demo Bank', 'EUR', 'same_day', FALSE, 100)
            """.trimIndent(),
        ).fetch().rowsUpdated().awaitSingle()
    }
}
