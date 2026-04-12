package de.chennemann.plannr.server.query.pockets.persistence

import de.chennemann.plannr.server.query.pockets.domain.PocketQuery
import de.chennemann.plannr.server.query.pockets.domain.PocketQueryRepository
import de.chennemann.plannr.server.support.ApiIntegrationTest
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient
import kotlin.test.assertEquals
import kotlin.test.assertNull

class R2dbcPocketQueryRepositoryTest : ApiIntegrationTest() {
    @Autowired lateinit var repository: PocketQueryRepository
    @Autowired lateinit var databaseClient: DatabaseClient

    @BeforeEach
    fun setUp() {
        cleanDatabase("pocket_query", "pockets", "accounts", "currencies")
        insertCurrency()
        insertAccount()
        insertPocket()
    }

    @Test
    fun `save or update and find pocket query by id`() = runBlocking {
        val created = PocketQuery(
            pocketId = "poc_123",
            accountId = "acc_123",
            name = "Bills",
            description = "Monthly bills",
            color = 123,
            isDefault = true,
            isArchived = false,
            createdAt = 200L,
            currentBalance = 10L,
        )

        repository.saveOrUpdate(created)

        assertEquals(created, repository.findById("poc_123"))
        assertNull(repository.findById("poc_missing"))
    }

    @Test
    fun `updates pocket query metadata and current balance`() = runBlocking {
        repository.saveOrUpdate(
            PocketQuery(
                pocketId = "poc_123",
                accountId = "acc_123",
                name = "Bills",
                description = null,
                color = 123,
                isDefault = true,
                isArchived = false,
                createdAt = 200L,
                currentBalance = 0L,
            ),
        )

        val updated = repository.saveOrUpdate(
            PocketQuery(
                pocketId = "poc_123",
                accountId = "acc_123",
                name = "Updated bills",
                description = "Updated",
                color = 456,
                isDefault = false,
                isArchived = true,
                createdAt = 200L,
                currentBalance = 111L,
            ),
        )

        assertEquals("Updated bills", updated.name)
        assertEquals("Updated", updated.description)
        assertEquals(456, updated.color)
        assertEquals(false, updated.isDefault)
        assertEquals(true, updated.isArchived)
        assertEquals(111L, updated.currentBalance)
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

    private fun insertPocket() = runBlocking {
        databaseClient.sql(
            """
            INSERT INTO pockets (id, account_id, name, description, color, is_default, is_archived, created_at)
            VALUES ('poc_123', 'acc_123', 'Bills', 'Monthly bills', 123, TRUE, FALSE, 200)
            """.trimIndent(),
        ).fetch().rowsUpdated().awaitSingle()
    }
}
