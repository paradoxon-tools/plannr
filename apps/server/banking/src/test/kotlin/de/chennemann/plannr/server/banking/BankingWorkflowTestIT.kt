package de.chennemann.plannr.server.banking

import de.chennemann.plannr.server.common.error.ApiException
import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.error.ValidationException
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.springframework.http.HttpStatus
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.core.DatabaseClient
import tools.jackson.databind.JsonNode
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.math.BigDecimal
import java.sql.DriverManager
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlin.test.*

/** Runs against an explicitly supplied disposable PostgreSQL database, using a fresh schema per test. */
class BankingWorkflowTestIT {
    private val mapper = jacksonObjectMapper()
    private lateinit var schema: String
    private lateinit var jdbcUrl: String
    private lateinit var user: String
    private lateinit var password: String
    private lateinit var db: BankingDatabase
    private lateinit var provider: FakeProvider
    private lateinit var connections: BankConnectionService
    private lateinit var sync: BankSyncService
    private lateinit var reconciliation: ReconciliationService
    private val range = SyncRequest(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-09-30"))

    @BeforeEach fun setup() {
        val url = System.getenv("BANKING_TEST_R2DBC_URL")
        assumeTrue(!url.isNullOrBlank(), "Set BANKING_TEST_R2DBC_URL to a disposable PostgreSQL database")
        user = System.getenv("BANKING_TEST_DB_USER") ?: "banking_test"
        password = System.getenv("BANKING_TEST_DB_PASSWORD") ?: ""
        jdbcUrl = url!!.replaceFirst("r2dbc:", "jdbc:")
        schema = "banking_test_" + UUID.randomUUID().toString().replace("-", "")
        Flyway.configure().dataSource(jdbcUrl, user, password).schemas(schema).defaultSchema(schema)
            .locations("classpath:db/migration").load().migrate()
        val factory = ConnectionFactories.get(ConnectionFactoryOptions.parse(url).mutate()
            .option(ConnectionFactoryOptions.USER, user).option(ConnectionFactoryOptions.PASSWORD, password)
            .option(Option.valueOf<String>("schema"), schema).build())
        db = BankingDatabase(DatabaseClient.create(factory), R2dbcTransactionManager(factory), mapper)
        provider = FakeProvider()
        connections = BankConnectionService(db, provider, "http://localhost:9000/banking/callback")
        sync = BankSyncService(db, provider)
        reconciliation = ReconciliationService(db)
    }

    @AfterEach fun cleanup() {
        if (!::schema.isInitialized) return
        require(schema.matches(Regex("banking_test_[a-f0-9]{32}")))
        DriverManager.getConnection(jdbcUrl, user, password).use { connection ->
            connection.createStatement().use { it.execute("DROP SCHEMA $schema CASCADE") }
        }
    }

    private suspend fun connect(): BankAccount {
        connections.start(StartConnection("Test Bank", "DE", Instant.now().plusSeconds(86400).toString()))
        val result = connections.complete(CompleteAuthorization(provider.state, "code"))
        assertEquals("ACTIVE", result.status)
        return connections.accounts(result.id).single()
    }

    private suspend fun account(currency: String = "EUR"): Long = db.one("""INSERT INTO accounts(name,institution,currency_code,weekend_handling,created_at)
        VALUES(:name,'Test',:currency,'NO_SHIFT',0) RETURNING id""", "name" to UUID.randomUUID().toString(), "currency" to currency).long("id")

    private suspend fun planned(accountId: Long, amount: Long = 1234, destinationAccount: Long? = null): Long {
        suspend fun pocket(account: Long) = db.one("INSERT INTO pockets(account_id,name,color,created_at) VALUES(:id,'Main',0,0) RETURNING id", "id" to account).long("id")
        val source = pocket(accountId)
        val destination = destinationAccount?.let { pocket(it) }
        val profile = db.one("SELECT id FROM financial_profiles WHERE is_fallback").long("id")
        val template = db.one("""INSERT INTO transaction_templates(source_pocket_id,title,currency_code,transaction_type,created_at,financial_profile_id)
            VALUES(:pocket,'Rent','EUR','EXPENSE',0,:profile) RETURNING id""", "pocket" to source, "profile" to profile).long("id")
        val version = db.one("""INSERT INTO transaction_template_versions(transaction_template_id,amount,first_occurrence_date,recurrence_type,valid_from,created_at)
            VALUES(:template,:amount,'2026-09-01','MONTHLY','2026-09-01',0) RETURNING id""", "template" to template, "amount" to amount).long("id")
        val id = db.one("""INSERT INTO transaction_materializations(transaction_template_id,transaction_template_version_id,transaction_date,source_pocket_id,
            title,amount,currency_code,transaction_type,created_at,financial_profile_id)
            VALUES(:template,:version,'2026-09-01',:pocket,'Rent',:amount,'EUR','EXPENSE',0,:profile) RETURNING id""",
            "template" to template, "version" to version, "pocket" to source, "amount" to amount, "profile" to profile).long("id")
        if (destination != null) db.execute("UPDATE transaction_materializations SET destination_pocket_id=:destination,transaction_type='TRANSFER' WHERE id=:id", "destination" to destination, "id" to id)
        return id
    }

    private suspend fun imported(bank: BankAccount): List<BankTransaction> {
        sync.sync(bank.id, range)
        return reconciliation.transactions(bank.id, null, null, null, 100, 0)
    }

    @Test fun `callback state is one-use and error callback does not exchange a code`() = runBlocking<Unit> {
        connections.start(StartConnection("Test Bank", "DE", Instant.now().plusSeconds(86400).toString()))
        val state = provider.state
        assertFailsWith<ValidationException> { connections.complete(CompleteAuthorization("unknown-state-long-enough", "code")) }
        assertEquals("FAILED", connections.complete(CompleteAuthorization(state, error="access_denied")).status)
        assertEquals(0, provider.exchanges)
        assertFailsWith<ValidationException> { connections.complete(CompleteAuthorization(state, "code")) }
    }

    @Test fun `expired state cannot be exchanged`() = runBlocking<Unit> {
        val authorization = connections.start(StartConnection("Test Bank", "DE", Instant.now().plusSeconds(86400).toString()))
        db.execute("UPDATE bank_connections SET state_expires_at=0 WHERE id=:id", "id" to authorization.connectionId)
        assertFailsWith<ValidationException> { connections.complete(CompleteAuthorization(provider.state, "code")) }
        assertEquals(0, provider.exchanges)
    }

    @Test fun `renewal preserves local bank account link and imported history`() = runBlocking<Unit> {
        val original = connect()
        val accountId = account()
        connections.link(original.id, accountId)
        val transaction = imported(original).single()
        provider.uid = UUID.randomUUID().toString()
        val renewed = connect()
        assertEquals(original.id, renewed.id)
        assertEquals(accountId, renewed.accountId)
        assertNotEquals(original.connectionId, renewed.connectionId)
        assertEquals(transaction.id, imported(renewed).single().id)
        connections.disconnect(original.connectionId)
        assertEquals("ACTIVE", connections.get(renewed.connectionId).status)
    }

    @Test fun `linking requires matching currency and one bank account per planning account`() = runBlocking<Unit> {
        val bank = connect()
        assertFailsWith<ValidationException> { connections.link(bank.id, account("USD")) }
        val accountId = account()
        connections.link(bank.id, accountId)
        provider.hash = "another-account"
        val second = connect()
        assertFailsWith<ConflictException> { connections.link(second.id, accountId) }
        connections.link(bank.id, null)
        assertEquals(accountId, connections.link(second.id, accountId).accountId)
    }

    @Test fun `pagination imports atomically and repeated synchronization keeps ids`() = runBlocking<Unit> {
        val bank = connect()
        provider.pages = listOf(listOf(provider.tx("one")), listOf(provider.tx("two")))
        val first = imported(bank)
        assertEquals(2, first.size)
        assertEquals(first.map { it.id }, imported(bank).map { it.id })
        provider.pages = listOf(listOf(provider.tx("three")), listOf(provider.tx("four")))
        provider.failSecondPage = true
        assertFailsWith<ApiException> { sync.sync(bank.id, range) }
        assertEquals(first.map { it.id }, reconciliation.transactions(bank.id, null, null, null, 100, 0).map { it.id })
        assertEquals("banking_provider_error", connections.accounts(null).single().lastError)
    }

    @Test fun `pending stable entry becomes booked without duplication and preserves review state`() = runBlocking<Unit> {
        val bank = connect()
        provider.pages = listOf(listOf(provider.tx("one", status="PDNG")))
        val pending = imported(bank).single()
        assertEquals("PDNG", pending.bookingStatus)
        reconciliation.ignore(pending.id, IgnoreRequest(true))
        provider.pages = listOf(listOf(provider.tx("one")))
        val booked = imported(bank).single()
        assertEquals(pending.id, booked.id)
        assertEquals("BOOK", booked.bookingStatus)
        assertEquals("IGNORED", booked.reconciliationStatus)
    }

    @Test fun `fingerprint imports retain identical rows and mark vanished observations stale`() = runBlocking<Unit> {
        val bank = connect()
        provider.pages = listOf(listOf(provider.tx(null), provider.tx(null)))
        val first = imported(bank)
        assertEquals(2, first.size)
        assertEquals(first.map { it.id }, imported(bank).map { it.id })
        provider.pages = listOf(listOf(provider.tx(null)))
        val next = imported(bank)
        assertEquals(1, next.count { it.isCurrent })
        assertEquals(2, next.size)
    }

    @Test fun `reconcile suggest undo and ignore preserve immutable planned snapshot`() = runBlocking<Unit> {
        val bank = connect()
        val accountId = account()
        connections.link(bank.id, accountId)
        val materializationId = planned(accountId)
        val transaction = imported(bank).single()
        assertEquals(materializationId, reconciliation.suggestions(transaction.id, 7).single().planned.materializationId)
        val match = reconciliation.reconcile(transaction.id, ReconcileRequest(materializationId))
        assertEquals(BigDecimal("0.00"), match.difference)
        assertEquals("RECONCILED", reconciliation.get(transaction.id).reconciliationStatus)
        assertEquals("RECONCILED", reconciliation.planned(accountId, range.dateFrom, range.dateTo, 100, 0).single().reconciliationStatus)
        assertFailsWith<ConflictException> { connections.link(bank.id, null) }
        assertFailsWith<ConflictException> { reconciliation.ignore(transaction.id, IgnoreRequest(true)) }
        db.execute("DELETE FROM transaction_materializations WHERE id=:id", "id" to materializationId)
        assertEquals("Rent", reconciliation.reconciliation(transaction.id).plannedSnapshot.get("title").asString())
        assertEquals(1, reconciliation.reconciliations(accountId, 100, 0).size)
        assertEquals("UNMATCHED", reconciliation.undo(transaction.id).reconciliationStatus)
        assertEquals("IGNORED", reconciliation.ignore(transaction.id, IgnoreRequest(true)).reconciliationStatus)
        assertEquals("UNMATCHED", reconciliation.ignore(transaction.id, IgnoreRequest(false)).reconciliationStatus)
    }

    @Test fun `wrong account pending and amount differences require explicit resolution`() = runBlocking<Unit> {
        val bank = connect()
        val accountId = account()
        connections.link(bank.id, accountId)
        val materializationId = planned(accountId, 1300)
        val transaction = imported(bank).single()
        assertFailsWith<ValidationException> { reconciliation.reconcile(transaction.id, ReconcileRequest(materializationId)) }
        val match = reconciliation.reconcile(transaction.id, ReconcileRequest(materializationId, true))
        assertEquals(BigDecimal("0.66"), match.difference)
        reconciliation.undo(transaction.id)
        val otherPlan = planned(account())
        assertFailsWith<ApiException> { reconciliation.reconcile(transaction.id, ReconcileRequest(otherPlan)) }
        provider.pages = listOf(listOf(provider.tx("pending", status="PDNG")))
        val pending = imported(bank).first { it.bookingStatus == "PDNG" }
        assertFailsWith<ValidationException> { reconciliation.reconcile(pending.id, ReconcileRequest(materializationId, true)) }
    }

    @Test fun `one occurrence cannot reconcile two actual transactions`() = runBlocking<Unit> {
        val bank = connect()
        val accountId = account()
        connections.link(bank.id, accountId)
        val plan = planned(accountId)
        provider.pages = listOf(listOf(provider.tx("one"), provider.tx("two")))
        val actual = imported(bank)
        reconciliation.reconcile(actual[0].id, ReconcileRequest(plan))
        assertFailsWith<ConflictException> { reconciliation.reconcile(actual[1].id, ReconcileRequest(plan)) }
        assertEquals("UNMATCHED", reconciliation.get(actual[1].id).reconciliationStatus)
    }

    @Test fun `both transfer legs reconcile independently`() = runBlocking<Unit> {
        val sourceBank = connect()
        val source = account()
        val destination = account()
        connections.link(sourceBank.id, source)
        val plan = planned(source, destinationAccount=destination)
        val debit = imported(sourceBank).single()
        reconciliation.reconcile(debit.id, ReconcileRequest(plan))
        provider.hash = "destination"
        val destinationBank = connect()
        connections.link(destinationBank.id, destination)
        provider.pages = listOf(listOf(provider.tx("credit", direction="CRDT")))
        val credit = imported(destinationBank).single()
        reconciliation.reconcile(credit.id, ReconcileRequest(plan))
        assertEquals("RECONCILED", reconciliation.get(credit.id).reconciliationStatus)
        assertEquals("RECONCILED", reconciliation.get(debit.id).reconciliationStatus)
    }

    @Test fun `reconciled amount change fails whole sync and disconnect retains history`() = runBlocking<Unit> {
        val bank = connect()
        val accountId = account()
        connections.link(bank.id, accountId)
        val plan = planned(accountId)
        val transaction = imported(bank).single()
        reconciliation.reconcile(transaction.id, ReconcileRequest(plan))
        provider.pages = listOf(listOf(provider.tx("one", amount="20.00")))
        assertFailsWith<ConflictException> { sync.sync(bank.id, range) }
        assertEquals(BigDecimal("-12.34"), reconciliation.get(transaction.id).amount)
        connections.disconnect(bank.connectionId)
        assertFailsWith<ConflictException> { sync.sync(bank.id, range) }
        assertEquals("RECONCILED", reconciliation.get(transaction.id).reconciliationStatus)
        assertEquals(1, provider.deletions)
    }

    @Test fun `revoked and expired sessions prevent imports`() = runBlocking<Unit> {
        val bank = connect()
        provider.sessionStatus = "REVOKED"
        assertEquals("FAILED", connections.refresh(bank.connectionId).status)
        assertFailsWith<ConflictException> { sync.sync(bank.id, range) }
        val renewed = connect()
        db.execute("UPDATE bank_connections SET valid_until=0 WHERE id=:id", "id" to renewed.connectionId)
        assertEquals("EXPIRED", connections.get(renewed.connectionId).status)
        assertFailsWith<ConflictException> { sync.sync(renewed.id, range) }
    }

    @Test fun `linked planning account cannot change currency or be deleted`() = runBlocking<Unit> {
        val bank = connect()
        val accountId = account()
        connections.link(bank.id, accountId)
        assertFailsWith<org.springframework.dao.DataIntegrityViolationException> {
            db.execute("UPDATE accounts SET currency_code='USD' WHERE id=:id", "id" to accountId)
        }
        assertFailsWith<org.springframework.dao.DataIntegrityViolationException> {
            db.execute("DELETE FROM accounts WHERE id=:id", "id" to accountId)
        }
        connections.link(bank.id, null)
        assertEquals(1L, db.execute("DELETE FROM accounts WHERE id=:id", "id" to accountId))
    }

    @Test fun `competing banking mutations return busy and can retry after commit`() = runBlocking<Unit> {
        val bank = connect()
        val accountId = account()
        val locked = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val holding = async {
            db.transaction { locked.complete(Unit); release.await() }
        }
        locked.await()
        try {
            assertEquals("banking_busy", assertFailsWith<ConflictException> { connections.link(bank.id, accountId) }.code)
        } finally { release.complete(Unit) }
        holding.await()
        assertEquals(accountId, connections.link(bank.id, accountId).accountId)
    }

    @Test fun `reconciliation survives materialization identity regeneration`() = runBlocking<Unit> {
        val bank = connect()
        val accountId = account()
        connections.link(bank.id, accountId)
        val plan = planned(accountId)
        val actual = imported(bank).single()
        reconciliation.reconcile(actual.id, ReconcileRequest(plan))
        val snapshot = reconciliation.reconciliation(actual.id).plannedSnapshot.toString()
        db.execute("DELETE FROM transaction_materializations WHERE id=:id", "id" to plan)
        db.execute("INSERT INTO transaction_materializations SELECT * FROM json_populate_record(NULL::transaction_materializations,CAST(:snapshot AS json))", "snapshot" to snapshot)
        db.execute("UPDATE transaction_materializations SET id=:new WHERE id=:old", "new" to plan+1000, "old" to plan)
        val rebuilt = reconciliation.planned(accountId, range.dateFrom, range.dateTo, 100, 0).single()
        assertEquals(plan+1000, rebuilt.materializationId)
        assertEquals("RECONCILED", rebuilt.reconciliationStatus)
    }

    @Test fun `shared alternative hashes keep five pockets separate and survive renewal`() = runBlocking<Unit> {
        provider.sessionAccounts = (1..5).map { n -> mapper.readTree("""{
            "uid":"${UUID.randomUUID()}","identification_hash":"pocket-$n",
            "identification_hashes":["pocket-$n","shared-fuzzy-hash"],
            "name":"Pocket $n","currency":"EUR","account_id":{"iban":"DE000$n"}}""") }
        val auth = connections.start(StartConnection("Test Bank", "DE", Instant.now().plusSeconds(86400).toString()))
        connections.complete(CompleteAuthorization(provider.state, "code"))
        val original = connections.accounts(auth.connectionId)
        assertEquals(5, original.size)
        assertEquals((1..5).map { "DE000$it" }.toSet(), original.map { it.iban }.toSet())
        val linked = original.last()
        val plan = account()
        connections.link(linked.id, plan)
        val transaction = imported(linked).single()
        provider.sessionAccounts = provider.accounts().map { node ->
            (node.deepCopy() as tools.jackson.databind.node.ObjectNode).put("uid", UUID.randomUUID().toString())
        }
        val renewal = connections.start(StartConnection("Test Bank", "DE", Instant.now().plusSeconds(86400).toString()))
        connections.complete(CompleteAuthorization(provider.state, "code"))
        val renewed = connections.accounts(renewal.connectionId)
        assertEquals(original.map { it.id }.toSet(), renewed.map { it.id }.toSet())
        assertEquals(plan, renewed.single { it.id == linked.id }.accountId)
        assertEquals(transaction.id, imported(renewed.single { it.id == linked.id }).single().id)
    }

    @Test fun `refresh recovers missing pockets while preserving legacy account history and link`() = runBlocking<Unit> {
        val legacy = connect()
        val plan = account()
        connections.link(legacy.id, plan)
        val transaction = imported(legacy).single()
        db.execute("UPDATE bank_accounts SET primary_identification_hash=NULL WHERE id=:id", "id" to legacy.id)
        val retained = provider.accounts().single()
        provider.sessionAccounts = (1..4).map { n -> mapper.readTree("""{
            "uid":"${UUID.randomUUID()}","identification_hash":"missing-$n",
            "identification_hashes":["missing-$n","shared-fuzzy-hash"],
            "name":"Pocket $n","currency":"EUR","account_id":{"iban":"DE000$n"}}""") } + listOf(retained)
        assertEquals("ACTIVE", connections.refresh(legacy.connectionId).status)
        val recovered = connections.accounts(legacy.connectionId)
        assertEquals(5, recovered.size)
        val preserved = recovered.single { it.iban == legacy.iban }
        assertEquals(legacy.id, preserved.id)
        assertEquals(plan, preserved.accountId)
        assertEquals(transaction.id, imported(preserved).single().id)
        connections.refresh(legacy.connectionId)
        assertEquals(recovered.map { it.id }, connections.accounts(legacy.connectionId).map { it.id })
    }

    private inner class FakeProvider : BankingProvider {
        var sessionAccounts: List<JsonNode>? = null
        fun accounts(): List<JsonNode> = sessionAccounts ?: listOf(mapper.readTree("""{
            "uid":"$uid","identification_hash":"$hash","identification_hashes":["$hash"],
            "name":"Checking","currency":"EUR","account_id":{"iban":"DE123"}}"""))
        var state = ""
        var uid = UUID.randomUUID().toString()
        var hash = "stable-account-hash"
        var exchanges = 0
        var deletions = 0
        var failSecondPage = false
        var sessionStatus = "AUTHORIZED"
        var pages = listOf(listOf(tx("one")))
        fun tx(reference: String?, status: String = "BOOK", amount: String = "12.34", direction: String = "DBIT"): JsonNode = mapper.readTree("""{
            ${if (reference == null) "" else "\"entry_reference\":\"$reference\","}
            "transaction_amount":{"amount":"$amount","currency":"EUR"},"credit_debit_indicator":"$direction",
            "status":"$status","booking_date":"2026-09-01","remittance_information":["Rent"]}""")
        override suspend fun request(method: String, path: String, body: Any?, query: Map<String, String>): JsonNode = when {
            path == "/auth" -> {
                state = (body as Map<*, *>)["state"] as String
                mapper.readTree("""{"url":"https://auth.enablebanking.com/test"}""")
            }
            path == "/sessions" -> {
                exchanges++
                mapper.readTree("""{"session_id":"${UUID.randomUUID()}","aspsp":{"name":"Test Bank","country":"DE"},
                    "access":{"valid_until":"${Instant.now().plusSeconds(86400)}"},
                    "accounts":[${accounts().joinToString(",")}]}""")
            }
            method == "DELETE" -> { deletions++; mapper.readTree("{}") }
            path.startsWith("/sessions/") -> mapper.readTree("""{"status":"$sessionStatus",
                "accounts":[${accounts().joinToString(",") { "\"${it.requiredText("uid")}\"" }}],
                "accounts_data":[${accounts().joinToString(",")}]}""")
            path.endsWith("/details") -> accounts().single { path == "/accounts/${it.requiredText("uid")}/details" }
            path.endsWith("/balances") -> mapper.readTree("""{"balances":[{"name":"Booked","balance_amount":{"amount":"100.00","currency":"EUR"},"balance_type":"CLBD"}]}""")
            path.endsWith("/transactions") -> {
                val page = query["continuation_key"]?.toInt() ?: 0
                if (failSecondPage && page == 1) throw ApiException(HttpStatus.BAD_GATEWAY,"banking_provider_error","Simulated failure")
                mapper.readTree("""{"transactions":[${pages[page].joinToString(",")}],"continuation_key":${if (page + 1 < pages.size) "\"${page+1}\"" else "null"}}""")
            }
            else -> error("Unexpected provider request: $method $path")
        }
    }
}
