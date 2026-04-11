package de.chennemann.plannr.server.query.projection

import de.chennemann.plannr.server.accounts.usecases.CreateAccount
import de.chennemann.plannr.server.partners.usecases.CreatePartner
import de.chennemann.plannr.server.partners.usecases.UpdatePartner
import de.chennemann.plannr.server.pockets.usecases.CreatePocket
import de.chennemann.plannr.server.pockets.usecases.UpdatePocket
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
    @Autowired lateinit var createPartner: CreatePartner
    @Autowired lateinit var updatePartner: UpdatePartner

    @BeforeEach
    fun setUp() {
        cleanDatabase(
            "projection_dirty_scope",
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
        val partner = createPartner(de.chennemann.plannr.server.partners.usecases.CreatePartner.Command("Shop", null))
        val transaction = createTransaction(
            CreateTransaction.Command("EXPENSE", "CLEARED", "2026-04-10", 100, "EUR", null, null, "Groceries", partner.id, pocket.id, null),
        )

        updateTransaction(
            UpdateTransaction.Command(transaction.id, "EXPENSE", "CLEARED", "2026-04-11", 100, "EUR", null, null, "Groceries", partner.id, pocket.id, null),
        )
        archiveTransaction(transaction.id)
        updatePocket(de.chennemann.plannr.server.pockets.usecases.UpdatePocket.Command(pocket.id, account.id, "Wallet 2", null, 999, true))
        updatePartner(de.chennemann.plannr.server.partners.usecases.UpdatePartner.Command(partner.id, "Shop 2"))
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
    fun `full rebuild safety job restores missing projections`() = runBlocking {
        val account = createAccount(CreateAccount.Command("Main account", "Demo Bank", "EUR", "NO_SHIFT"))
        val pocket = createPocket(CreatePocket.Command(account.id, "Wallet", null, 123, true))
        createTransaction(CreateTransaction.Command("EXPENSE", "CLEARED", "2026-04-10", 100, "EUR", null, null, "Groceries", null, pocket.id, null))

        databaseClient.sql("DELETE FROM account_transaction_feed").fetch().rowsUpdated().awaitSingle()
        databaseClient.sql("DELETE FROM pocket_transaction_feed").fetch().rowsUpdated().awaitSingle()

        projectionScheduler.runFullRebuildSafetyJob()

        assertEquals(1L, count("SELECT COUNT(*) AS value FROM account_transaction_feed WHERE account_id = '${account.id}'"))
        assertEquals(1L, count("SELECT COUNT(*) AS value FROM pocket_transaction_feed WHERE pocket_id = '${pocket.id}'"))
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
}
