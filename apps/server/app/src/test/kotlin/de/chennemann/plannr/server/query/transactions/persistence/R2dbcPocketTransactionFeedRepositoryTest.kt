package de.chennemann.plannr.server.query.transactions.persistence

import de.chennemann.plannr.server.query.transactions.domain.PocketTransactionFeedRepository
import de.chennemann.plannr.server.support.ApiIntegrationTest
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient
import kotlin.test.assertEquals

class R2dbcPocketTransactionFeedRepositoryTest : ApiIntegrationTest() {
    @Autowired lateinit var repository: PocketTransactionFeedRepository
    @Autowired lateinit var databaseClient: DatabaseClient

    @BeforeEach
    fun setUp() {
        cleanDatabase("pocket_transaction_feed", "partners", "pockets", "accounts", "currencies")
        insertCurrency()
        insertAccount("acc_123")
        insertPocket("poc_123", "acc_123", "Wallet", 123)
        insertPocket("poc_transfer", "acc_123", "Savings", 456)
        insertPocket("poc_other", "acc_123", "Other", 789)
        insertPartner()
        insertFeedRow(pocketId = "poc_123", transactionId = "tx_1", historyPosition = 1, transferPocketId = "poc_transfer", transferPocketName = "Savings", transferPocketColor = 456)
        insertFeedRow(pocketId = "poc_123", transactionId = "tx_2", historyPosition = 2, transferPocketId = "poc_transfer", transferPocketName = "Savings", transferPocketColor = 456)
        insertFeedRow(pocketId = "poc_other", transactionId = "tx_other", historyPosition = 1, transferPocketId = "poc_123", transferPocketName = "Wallet", transferPocketColor = 123)
    }

    @Test
    fun `fetches first page in descending order and maps fields`() = runBlocking {
        val page = repository.findPage(pocketId = "poc_123", before = null, limit = 1)

        assertEquals(1, page.size)
        assertEquals("tx_2", page.first().transactionId)
        assertEquals(2L, page.first().historyPosition)
        assertEquals("Partner", page.first().partnerName)
        assertEquals("Savings", page.first().transferPocketName)
        assertEquals(456, page.first().transferPocketColor)
        assertEquals(100L, page.first().transactionAmount)
        assertEquals(-100L, page.first().signedAmount)
        assertEquals(-200L, page.first().balanceAfter)
    }

    @Test
    fun `fetches next page using before and filters by pocket id`() = runBlocking {
        val page = repository.findPage(pocketId = "poc_123", before = 2L, limit = 10)

        assertEquals(listOf("tx_1"), page.map { it.transactionId })
        assertEquals(true, page.none { it.pocketId == "poc_other" })
    }

    @Test
    fun `maps nullable denormalized pocket feed fields`() = runBlocking {
        insertPocket("poc_null", "acc_123", "Null pocket", 999)
        insertFeedRowWithNulls(pocketId = "poc_null", transactionId = "tx_null", historyPosition = 1)

        val page = repository.findPage(pocketId = "poc_null", before = null, limit = 10)

        assertEquals(1, page.size)
        assertEquals(null, page.first().partnerId)
        assertEquals(null, page.first().partnerName)
        assertEquals(null, page.first().transferPocketId)
        assertEquals(null, page.first().transferPocketName)
        assertEquals(null, page.first().transferPocketColor)
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

    private fun insertFeedRow(
        pocketId: String,
        transactionId: String,
        historyPosition: Long,
        transferPocketId: String,
        transferPocketName: String,
        transferPocketColor: Int,
    ) = runBlocking {
        databaseClient.sql(
            """
            INSERT INTO pocket_transaction_feed (
                pocket_id, account_id, transaction_id, history_position, transaction_date, type, status, description,
                transaction_amount, signed_amount, balance_after, partner_id, partner_name,
                transfer_pocket_id, transfer_pocket_name, transfer_pocket_color, is_archived
            ) VALUES (
                :pocketId, 'acc_123', :transactionId, :historyPosition, '2026-04-10', 'transfer', 'booked', 'desc',
                100, -100, :balanceAfter, 'par_123', 'Partner',
                :transferPocketId, :transferPocketName, :transferPocketColor, FALSE
            )
            """.trimIndent(),
        )
            .bind("pocketId", pocketId)
            .bind("transactionId", transactionId)
            .bind("historyPosition", historyPosition)
            .bind("balanceAfter", -100L * historyPosition)
            .bind("transferPocketId", transferPocketId)
            .bind("transferPocketName", transferPocketName)
            .bind("transferPocketColor", transferPocketColor)
            .fetch().rowsUpdated().awaitSingle()
    }

    private fun insertFeedRowWithNulls(
        pocketId: String,
        transactionId: String,
        historyPosition: Long,
    ) = runBlocking {
        databaseClient.sql(
            """
            INSERT INTO pocket_transaction_feed (
                pocket_id, account_id, transaction_id, history_position, transaction_date, type, status, description,
                transaction_amount, signed_amount, balance_after, partner_id, partner_name,
                transfer_pocket_id, transfer_pocket_name, transfer_pocket_color, is_archived
            ) VALUES (
                :pocketId, 'acc_123', :transactionId, :historyPosition, '2026-04-11', 'income', 'booked', 'nullable',
                100, 100, 100, null, null,
                null, null, null, FALSE
            )
            """.trimIndent(),
        )
            .bind("pocketId", pocketId)
            .bind("transactionId", transactionId)
            .bind("historyPosition", historyPosition)
            .fetch().rowsUpdated().awaitSingle()
    }
}
