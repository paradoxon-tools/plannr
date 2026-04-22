package de.chennemann.plannr.server.query.projection

import de.chennemann.plannr.server.accounts.usecases.CreateAccount
import de.chennemann.plannr.server.partners.service.CreatePartnerCommand
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.partners.service.UpdatePartnerCommand
import de.chennemann.plannr.server.pockets.usecases.CreatePocket
import de.chennemann.plannr.server.pockets.usecases.UpdatePocket
import de.chennemann.plannr.server.projection.ProjectionDirtyScopeRepository
import de.chennemann.plannr.server.projection.ProjectionDirtyScopeService
import de.chennemann.plannr.server.projection.ProjectionScheduler
import de.chennemann.plannr.server.support.ApiIntegrationTest
import de.chennemann.plannr.server.transactions.usecases.ArchiveTransaction
import de.chennemann.plannr.server.transactions.usecases.CreateTransaction
import de.chennemann.plannr.server.transactions.usecases.UpdateTransaction
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient
import kotlin.test.assertEquals

class ProjectionSchedulerIntegrationTest : ApiIntegrationTest() {
    @Autowired lateinit var databaseClient: DatabaseClient
    @Autowired lateinit var dirtyScopeRepository: ProjectionDirtyScopeRepository
    @Autowired lateinit var dirtyScopeService: ProjectionDirtyScopeService
    @Autowired lateinit var projectionScheduler: ProjectionScheduler
    @Autowired lateinit var createAccount: CreateAccount
    @Autowired lateinit var createPocket: CreatePocket
    @Autowired lateinit var updatePocket: UpdatePocket
    @Autowired lateinit var createTransaction: CreateTransaction
    @Autowired lateinit var updateTransaction: UpdateTransaction
    @Autowired lateinit var archiveTransaction: ArchiveTransaction
    @Autowired lateinit var partnerService: PartnerService

    @BeforeEach
    fun setUp() {
        cleanDatabase(
            "projection_dirty_scope",
            "account_future_transaction_feed",
            "pocket_future_transaction_feed",
            "account_transaction_feed",
            "pocket_transaction_feed",
            "transactions",
            "pocket_query",
            "account_query",
            "recurring_transactions",
            "contracts",
            "partners",
            "pockets",
            "accounts",
            "currencies",
        )
        insertCurrency("EUR")
    }

    @Test
    fun `transaction and metadata writes enqueue dirty scopes and dedupe them`() = runBlocking {
        val account = createAccount(CreateAccount.Command("Main account", "Demo Bank", "EUR", "NO_SHIFT"))
        val pocket = createPocket(CreatePocket.Command(account.id, "Wallet", null, 123, true))
        val partner = partnerService.create(CreatePartnerCommand("Shop", null))
        val transaction = createTransaction(
            CreateTransaction.Command("EXPENSE", "CLEARED", "2026-04-10", 100, "EUR", null, null, "Groceries", partner.id, pocket.id, null),
        )

        updateTransaction(
            UpdateTransaction.Command(transaction.id, "EXPENSE", "CLEARED", "2026-04-11", 100, "EUR", null, null, "Groceries", partner.id, pocket.id, null),
        )
        archiveTransaction(transaction.id)
        updatePocket(de.chennemann.plannr.server.pockets.usecases.UpdatePocket.Command(pocket.id, account.id, "Wallet 2", null, 999, true))
        partnerService.update(UpdatePartnerCommand(partner.id, "Shop 2", null))
        dirtyScopeService.markPocketDirty(pocket.id)
        dirtyScopeService.markPocketDirty(pocket.id)

        val scopes = dirtyScopeRepository.listAll()
        assertEquals(true, scopes.any { it.scopeType == "ACCOUNT" && it.scopeId == account.id })
        assertEquals(true, scopes.any { it.scopeType == "POCKET" && it.scopeId == pocket.id })
        assertEquals(true, scopes.any { it.scopeType == "FULL" && it.scopeId == "ALL" })
        assertEquals(1, scopes.count { it.scopeType == "POCKET" && it.scopeId == pocket.id })
    }

    @Test
    fun `scheduled dirty scope processing rebuilds projections and clears scopes`() = runBlocking {
        val account = createAccount(CreateAccount.Command("Main account", "Demo Bank", "EUR", "NO_SHIFT"))
        val pocket = createPocket(CreatePocket.Command(account.id, "Wallet", null, 123, true))
        createTransaction(CreateTransaction.Command("EXPENSE", "CLEARED", "2026-04-10", 100, "EUR", null, null, "Groceries", null, pocket.id, null))

        projectionScheduler.processDirtyScopes()

        assertEquals(emptyList(), dirtyScopeRepository.listAll())
        assertEquals(1L, count("SELECT COUNT(*) AS value FROM account_transaction_feed WHERE account_id = '${account.id}'"))
        assertEquals(1L, count("SELECT COUNT(*) AS value FROM pocket_transaction_feed WHERE pocket_id = '${pocket.id}'"))
    }

    @Test
    fun `transfer writes mark both pocket scopes dirty`() = runBlocking {
        val account = createAccount(CreateAccount.Command("Main account", "Demo Bank", "EUR", "NO_SHIFT"))
        val sourcePocket = createPocket(CreatePocket.Command(account.id, "Checking", null, 111, true))
        val destinationPocket = createPocket(CreatePocket.Command(account.id, "Savings", null, 222, false))

        createTransaction(CreateTransaction.Command("TRANSFER", "CLEARED", "2026-04-10", 100, "EUR", null, null, "Move", null, sourcePocket.id, destinationPocket.id))

        val scopes = dirtyScopeRepository.listAll()
        assertEquals(true, scopes.any { it.scopeType == "POCKET" && it.scopeId == sourcePocket.id })
        assertEquals(true, scopes.any { it.scopeType == "POCKET" && it.scopeId == destinationPocket.id })
    }

    @Test
    fun `full rebuild safety job restores missing projections`() = runBlocking {
        val account = createAccount(CreateAccount.Command("Main account", "Demo Bank", "EUR", "NO_SHIFT"))
        val pocket = createPocket(CreatePocket.Command(account.id, "Wallet", null, 123, true))
        createTransaction(CreateTransaction.Command("EXPENSE", "CLEARED", "2026-04-10", 100, "EUR", null, null, "Groceries", null, pocket.id, null))

        createTransaction(CreateTransaction.Command("EXPENSE", "PENDING", "2099-04-10", 50, "EUR", null, null, "Future groceries", null, pocket.id, null))

        projectionScheduler.runFullRebuildSafetyJob()
        val baselineHistory = count("SELECT COUNT(*) AS value FROM account_transaction_feed WHERE account_id = '${account.id}'")
        val baselineFuture = count("SELECT COUNT(*) AS value FROM account_future_transaction_feed WHERE account_id = '${account.id}'")
        val baselineBalance = count("SELECT current_balance AS value FROM account_query WHERE account_id = '${account.id}'")

        databaseClient.sql("DELETE FROM account_transaction_feed").fetch().rowsUpdated().awaitSingle()
        databaseClient.sql("DELETE FROM pocket_transaction_feed").fetch().rowsUpdated().awaitSingle()
        databaseClient.sql("DELETE FROM account_future_transaction_feed").fetch().rowsUpdated().awaitSingle()
        databaseClient.sql("DELETE FROM pocket_future_transaction_feed").fetch().rowsUpdated().awaitSingle()
        databaseClient.sql("UPDATE account_query SET current_balance = 999 WHERE account_id = :id").bind("id", account.id).fetch().rowsUpdated().awaitSingle()
        databaseClient.sql("UPDATE pocket_query SET current_balance = 999 WHERE pocket_id = :id").bind("id", pocket.id).fetch().rowsUpdated().awaitSingle()

        projectionScheduler.runFullRebuildSafetyJob()

        assertEquals(1L, count("SELECT COUNT(*) AS value FROM account_transaction_feed WHERE account_id = '${account.id}'"))
        assertEquals(1L, count("SELECT COUNT(*) AS value FROM pocket_transaction_feed WHERE pocket_id = '${pocket.id}'"))
        assertEquals(baselineFuture, count("SELECT COUNT(*) AS value FROM account_future_transaction_feed WHERE account_id = '${account.id}'"))
        assertEquals(1L, count("SELECT COUNT(*) AS value FROM pocket_future_transaction_feed WHERE pocket_id = '${pocket.id}'"))
        assertEquals(baselineBalance, count("SELECT current_balance AS value FROM account_query WHERE account_id = '${account.id}'"))
        assertEquals(-100L, count("SELECT current_balance AS value FROM pocket_query WHERE pocket_id = '${pocket.id}'"))

        val firstAccountHistory = rows("SELECT transaction_id, history_position, signed_amount, balance_after FROM account_transaction_feed WHERE account_id = '${account.id}' ORDER BY history_position")
        val firstAccountFuture = rows("SELECT transaction_id, future_position, signed_amount, projected_balance_after FROM account_future_transaction_feed WHERE account_id = '${account.id}' ORDER BY future_position")

        projectionScheduler.runFullRebuildSafetyJob()

        assertEquals(firstAccountHistory, rows("SELECT transaction_id, history_position, signed_amount, balance_after FROM account_transaction_feed WHERE account_id = '${account.id}' ORDER BY history_position"))
        assertEquals(firstAccountFuture, rows("SELECT transaction_id, future_position, signed_amount, projected_balance_after FROM account_future_transaction_feed WHERE account_id = '${account.id}' ORDER BY future_position"))
    }

    private fun insertCurrency(code: String) = runBlocking {
        databaseClient.sql(
            """
            INSERT INTO currencies (code, name, symbol, decimal_places, symbol_position)
            VALUES (:code, 'Euro', '€', 2, 'before')
            """.trimIndent(),
        )
            .bind("code", code)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    private suspend fun count(sql: String): Long =
        (databaseClient.sql(sql).fetch().one().awaitSingle().getValue("value") as Number).toLong()

    private suspend fun rows(sql: String): List<Map<String, Any?>> =
        databaseClient.sql(sql).fetch().all().collectList().awaitSingle().map { row -> row.mapValues { it.value } }
}
