package de.chennemann.plannr.server.query.transactions.persistence

import de.chennemann.plannr.server.transactions.domain.AccountTransactionFeedRepository
import de.chennemann.plannr.server.support.ApiIntegrationTest
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient
import kotlin.test.assertEquals

class R2dbcAccountTransactionFeedRepositoryTest : ApiIntegrationTest() {
    @Autowired lateinit var repository: AccountTransactionFeedRepository
    @Autowired lateinit var databaseClient: DatabaseClient

    @BeforeEach
    fun setUp() {
        cleanDatabase("account_transaction_feed", "partners", "pockets", "accounts", "currencies")
        insertCurrency()
        insertAccount("acc_123")
        insertAccount("acc_999")
        insertPocket("poc_src", "acc_123", "Source", 123)
        insertPocket("poc_dst", "acc_123", "Destination", 456)
        insertPartner()
        insertFeedRow(accountId = "acc_123", transactionId = "tx_1", historyPosition = 1, description = "first")
        insertFeedRow(accountId = "acc_123", transactionId = "tx_2", historyPosition = 2, description = "second")
        insertFeedRow(accountId = "acc_123", transactionId = "tx_3", historyPosition = 3, description = "third")
        insertFeedRow(accountId = "acc_999", transactionId = "tx_other", historyPosition = 1, description = "other")
    }

    @Test
    fun `fetches first page in descending order and maps fields`() = runBlocking {
        val page = repository.findPage(accountId = "acc_123", before = null, limit = 2)

        assertEquals(2, page.size)
        assertEquals(listOf("tx_3", "tx_2"), page.map { it.transactionId })
        assertEquals(listOf(3L, 2L), page.map { it.historyPosition })
        assertEquals("Partner", page.first().partnerName)
        assertEquals("Source", page.first().sourcePocketName)
        assertEquals(123, page.first().sourcePocketColor)
        assertEquals("Destination", page.first().destinationPocketName)
        assertEquals(456, page.first().destinationPocketColor)
        assertEquals(100L, page.first().transactionAmount)
        assertEquals(-100L, page.first().signedAmount)
        assertEquals(-300L, page.first().balanceAfter)
    }

    @Test
    fun `fetches next page using before and filters by account id`() = runBlocking {
        val page = repository.findPage(accountId = "acc_123", before = 3L, limit = 10)

        assertEquals(listOf("tx_2", "tx_1"), page.map { it.transactionId })
        assertEquals(true, page.none { it.accountId == "acc_999" })
    }

    @Test
    fun `maps nullable denormalized account feed fields`() = runBlocking {
        insertAccount("acc_null")
        insertFeedRowWithNulls(accountId = "acc_null", transactionId = "tx_null", historyPosition = 1)

        val page = repository.findPage(accountId = "acc_null", before = null, limit = 10)

        assertEquals(1, page.size)
        assertEquals(null, page.first().partnerId)
        assertEquals(null, page.first().partnerName)
        assertEquals(null, page.first().sourcePocketId)
        assertEquals(null, page.first().sourcePocketName)
        assertEquals(null, page.first().sourcePocketColor)
        assertEquals(null, page.first().destinationPocketId)
        assertEquals(null, page.first().destinationPocketName)
        assertEquals(null, page.first().destinationPocketColor)
    }

    private fun insertCurrency() = runBlocking {
        databaseClient.sql("INSERT INTO currencies (code, name, symbol, decimal_places, symbol_position) VALUES ('EUR', 'Euro', '€', 2, 'before')")
            .fetch().rowsUpdated().awaitSingle()
    }

    private fun insertAccount(id: String) = runBlocking {
        databaseClient.sql("INSERT INTO accounts (id, name, institution, currency_code, weekend_handling, is_archived, created_at) VALUES (:id, 'Account', 'Bank', 'EUR', 'same_day', FALSE, 1)")
            .bind("id", id)
            .fetch().rowsUpdated().awaitSingle()
    }

    private fun insertPocket(id: String, accountId: String, name: String, color: Int) = runBlocking {
        databaseClient.sql("INSERT INTO pockets (id, account_id, name, description, color, is_default, is_archived, created_at) VALUES (:id, :accountId, :name, null, :color, FALSE, FALSE, 1)")
            .bind("id", id)
            .bind("accountId", accountId)
            .bind("name", name)
            .bind("color", color)
            .fetch().rowsUpdated().awaitSingle()
    }

    private fun insertPartner() = runBlocking {
        databaseClient.sql("INSERT INTO partners (id, name, notes, is_archived, created_at) VALUES ('par_123', 'Partner', null, FALSE, 1)")
            .fetch().rowsUpdated().awaitSingle()
    }

    private fun insertFeedRow(accountId: String, transactionId: String, historyPosition: Long, description: String) = runBlocking {
        databaseClient.sql(
            """
            INSERT INTO account_transaction_feed (
                account_id, transaction_id, history_position, transaction_date, type, status, description,
                transaction_amount, signed_amount, balance_after, partner_id, partner_name,
                source_pocket_id, source_pocket_name, source_pocket_color,
                destination_pocket_id, destination_pocket_name, destination_pocket_color, is_archived
            ) VALUES (
                :accountId, :transactionId, :historyPosition, '2026-04-10', 'expense', 'booked', :description,
                100, -100, :balanceAfter, 'par_123', 'Partner',
                'poc_src', 'Source', 123,
                'poc_dst', 'Destination', 456, FALSE
            )
            """.trimIndent(),
        )
            .bind("accountId", accountId)
            .bind("transactionId", transactionId)
            .bind("historyPosition", historyPosition)
            .bind("description", description)
            .bind("balanceAfter", -100L * historyPosition)
            .fetch().rowsUpdated().awaitSingle()
    }

    private fun insertFeedRowWithNulls(accountId: String, transactionId: String, historyPosition: Long) = runBlocking {
        databaseClient.sql(
            """
            INSERT INTO account_transaction_feed (
                account_id, transaction_id, history_position, transaction_date, type, status, description,
                transaction_amount, signed_amount, balance_after, partner_id, partner_name,
                source_pocket_id, source_pocket_name, source_pocket_color,
                destination_pocket_id, destination_pocket_name, destination_pocket_color, is_archived
            ) VALUES (
                :accountId, :transactionId, :historyPosition, '2026-04-11', 'income', 'booked', 'nullable',
                100, 100, 100, null, null,
                null, null, null,
                null, null, null, FALSE
            )
            """.trimIndent(),
        )
            .bind("accountId", accountId)
            .bind("transactionId", transactionId)
            .bind("historyPosition", historyPosition)
            .fetch().rowsUpdated().awaitSingle()
    }
}
