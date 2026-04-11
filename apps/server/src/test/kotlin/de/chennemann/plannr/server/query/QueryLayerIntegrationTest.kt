package de.chennemann.plannr.server.query

import de.chennemann.plannr.server.accounts.usecases.ArchiveAccount
import de.chennemann.plannr.server.accounts.usecases.CreateAccount
import de.chennemann.plannr.server.accounts.usecases.UnarchiveAccount
import de.chennemann.plannr.server.accounts.usecases.UpdateAccount
import de.chennemann.plannr.server.partners.usecases.CreatePartner
import de.chennemann.plannr.server.partners.usecases.UpdatePartner
import de.chennemann.plannr.server.pockets.usecases.ArchivePocket
import de.chennemann.plannr.server.pockets.usecases.CreatePocket
import de.chennemann.plannr.server.pockets.usecases.UnarchivePocket
import de.chennemann.plannr.server.pockets.usecases.UpdatePocket
import de.chennemann.plannr.server.support.ApiIntegrationTest
import de.chennemann.plannr.server.support.expectApiError
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient
import kotlin.test.assertEquals

class QueryLayerIntegrationTest : ApiIntegrationTest() {
    @Autowired lateinit var databaseClient: DatabaseClient
    @Autowired lateinit var createAccount: CreateAccount
    @Autowired lateinit var updateAccount: UpdateAccount
    @Autowired lateinit var archiveAccount: ArchiveAccount
    @Autowired lateinit var unarchiveAccount: UnarchiveAccount
    @Autowired lateinit var createPocket: CreatePocket
    @Autowired lateinit var updatePocket: UpdatePocket
    @Autowired lateinit var archivePocket: ArchivePocket
    @Autowired lateinit var unarchivePocket: UnarchivePocket
    @Autowired lateinit var createPartner: CreatePartner
    @Autowired lateinit var updatePartner: UpdatePartner

    @BeforeEach
    fun setUp() {
        cleanDatabase(
            "pocket_transaction_feed",
            "account_transaction_feed",
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
    fun `account detail query returns projected account and current balance`() = runBlocking {
        val created = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Test Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )

        webTestClient.get()
            .uri("/query/accounts/${created.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(created.id)
            .jsonPath("$.name").isEqualTo("Main account")
            .jsonPath("$.institution").isEqualTo("Test Bank")
            .jsonPath("$.currencyCode").isEqualTo("EUR")
            .jsonPath("$.weekendHandling").isEqualTo("NO_SHIFT")
            .jsonPath("$.currentBalance").isEqualTo(0)
            .jsonPath("$.isArchived").isEqualTo(false)

        updateAccount(
            UpdateAccount.Command(
                id = created.id,
                name = "Updated account",
                institution = "Updated Bank",
                currencyCode = "EUR",
                weekendHandling = "MOVE_AFTER",
            ),
        )

        webTestClient.get()
            .uri("/query/accounts/${created.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("Updated account")
            .jsonPath("$.institution").isEqualTo("Updated Bank")
            .jsonPath("$.weekendHandling").isEqualTo("MOVE_AFTER")
            .jsonPath("$.currentBalance").isEqualTo(0)
    }

    @Test
    fun `pocket detail query returns projected pocket and empty feed`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Test Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )
        val pocket = createPocket(
            CreatePocket.Command(
                accountId = account.id,
                name = "Bills",
                description = "Monthly bills",
                color = 123,
                isDefault = true,
            ),
        )

        webTestClient.get()
            .uri("/query/pockets/${pocket.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(pocket.id)
            .jsonPath("$.accountId").isEqualTo(account.id)
            .jsonPath("$.name").isEqualTo("Bills")
            .jsonPath("$.description").isEqualTo("Monthly bills")
            .jsonPath("$.color").isEqualTo(123)
            .jsonPath("$.isDefault").isEqualTo(true)
            .jsonPath("$.currentBalance").isEqualTo(0)

        updatePocket(
            UpdatePocket.Command(
                id = pocket.id,
                accountId = account.id,
                name = "Updated bills",
                description = "Updated description",
                color = 456,
                isDefault = false,
            ),
        )

        webTestClient.get()
            .uri("/query/pockets/${pocket.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(0)

        webTestClient.get()
            .uri("/query/pockets/${pocket.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("Updated bills")
            .jsonPath("$.description").isEqualTo("Updated description")
            .jsonPath("$.color").isEqualTo(456)
            .jsonPath("$.isDefault").isEqualTo(false)
    }

    @Test
    fun `account transaction feed query paginates by history position descending`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Test Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )
        val sourcePocket = createPocket(CreatePocket.Command(account.id, "Source", null, 100, true))
        val destinationPocket = createPocket(CreatePocket.Command(account.id, "Destination", null, 200, false))

        insertAccountFeedRow(
            accountId = account.id,
            transactionId = "tx_1",
            historyPosition = 1,
            transactionDate = "2026-04-01",
            description = "first",
            transactionAmount = 100,
            signedAmount = -100,
            balanceAfter = -100,
            sourcePocketId = sourcePocket.id,
            sourcePocketName = sourcePocket.name,
            sourcePocketColor = sourcePocket.color,
        )
        insertAccountFeedRow(
            accountId = account.id,
            transactionId = "tx_2",
            historyPosition = 2,
            transactionDate = "2026-04-02",
            description = "second",
            transactionAmount = 50,
            signedAmount = 50,
            balanceAfter = -50,
            destinationPocketId = destinationPocket.id,
            destinationPocketName = destinationPocket.name,
            destinationPocketColor = destinationPocket.color,
        )
        insertAccountFeedRow(
            accountId = account.id,
            transactionId = "tx_3",
            historyPosition = 3,
            transactionDate = "2026-04-03",
            description = "third",
            transactionAmount = 70,
            signedAmount = -70,
            balanceAfter = -120,
            sourcePocketId = sourcePocket.id,
            sourcePocketName = sourcePocket.name,
            sourcePocketColor = sourcePocket.color,
        )

        webTestClient.get()
            .uri("/query/accounts/${account.id}/transactions?limit=2")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(2)
            .jsonPath("$.items[0].transactionId").isEqualTo("tx_3")
            .jsonPath("$.items[0].historyPosition").isEqualTo(3)
            .jsonPath("$.items[1].transactionId").isEqualTo("tx_2")
            .jsonPath("$.items[1].historyPosition").isEqualTo(2)
            .jsonPath("$.nextBefore").isEqualTo(2)

        webTestClient.get()
            .uri("/query/accounts/${account.id}/transactions?limit=2&before=2")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].transactionId").isEqualTo("tx_1")
            .jsonPath("$.items[0].historyPosition").isEqualTo(1)
            .jsonPath("$.nextBefore").isEqualTo(1)
    }

    @Test
    fun `pocket transaction feed query paginates by history position descending`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Test Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )
        val pocket = createPocket(CreatePocket.Command(account.id, "Pocket", null, 100, true))
        val transferPocket = createPocket(CreatePocket.Command(account.id, "Savings", null, 200, false))

        insertPocketFeedRow(
            pocketId = pocket.id,
            accountId = account.id,
            transactionId = "ptx_1",
            historyPosition = 1,
            transactionDate = "2026-04-01",
            description = "first",
            transactionAmount = 100,
            signedAmount = -100,
            balanceAfter = -100,
            transferPocketId = transferPocket.id,
            transferPocketName = transferPocket.name,
            transferPocketColor = transferPocket.color,
        )
        insertPocketFeedRow(
            pocketId = pocket.id,
            accountId = account.id,
            transactionId = "ptx_2",
            historyPosition = 2,
            transactionDate = "2026-04-02",
            description = "second",
            transactionAmount = 40,
            signedAmount = 40,
            balanceAfter = -60,
            transferPocketId = transferPocket.id,
            transferPocketName = transferPocket.name,
            transferPocketColor = transferPocket.color,
        )

        webTestClient.get()
            .uri("/query/pockets/${pocket.id}/transactions?limit=1")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].transactionId").isEqualTo("ptx_2")
            .jsonPath("$.items[0].transferPocketName").isEqualTo("Savings")
            .jsonPath("$.nextBefore").isEqualTo(2)

        webTestClient.get()
            .uri("/query/pockets/${pocket.id}/transactions?limit=1&before=2")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].transactionId").isEqualTo("ptx_1")
            .jsonPath("$.nextBefore").isEqualTo(1)
    }

    @Test
    fun `pocket updates propagate denormalized feed metadata`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Test Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )
        val pocket = createPocket(CreatePocket.Command(account.id, "Pocket", null, 100, true))
        val otherPocket = createPocket(CreatePocket.Command(account.id, "Other", null, 200, false))

        insertAccountFeedRow(
            accountId = account.id,
            transactionId = "tx_source",
            historyPosition = 1,
            transactionDate = "2026-04-01",
            description = "source-row",
            transactionAmount = 10,
            signedAmount = -10,
            balanceAfter = -10,
            sourcePocketId = pocket.id,
            sourcePocketName = pocket.name,
            sourcePocketColor = pocket.color,
        )
        insertAccountFeedRow(
            accountId = account.id,
            transactionId = "tx_destination",
            historyPosition = 2,
            transactionDate = "2026-04-02",
            description = "destination-row",
            transactionAmount = 20,
            signedAmount = 20,
            balanceAfter = 10,
            destinationPocketId = pocket.id,
            destinationPocketName = pocket.name,
            destinationPocketColor = pocket.color,
        )
        insertPocketFeedRow(
            pocketId = otherPocket.id,
            accountId = account.id,
            transactionId = "ptx_transfer",
            historyPosition = 1,
            transactionDate = "2026-04-03",
            description = "transfer-row",
            transactionAmount = 30,
            signedAmount = 30,
            balanceAfter = 30,
            transferPocketId = pocket.id,
            transferPocketName = pocket.name,
            transferPocketColor = pocket.color,
        )

        updatePocket(
            UpdatePocket.Command(
                id = pocket.id,
                accountId = account.id,
                name = "Renamed pocket",
                description = pocket.description,
                color = 999,
                isDefault = pocket.isDefault,
            ),
        )

        val sourceRow = querySingle("SELECT source_pocket_name, source_pocket_color FROM account_transaction_feed WHERE transaction_id = 'tx_source'")
        val destinationRow = querySingle("SELECT destination_pocket_name, destination_pocket_color FROM account_transaction_feed WHERE transaction_id = 'tx_destination'")
        val transferRow = querySingle("SELECT transfer_pocket_name, transfer_pocket_color FROM pocket_transaction_feed WHERE transaction_id = 'ptx_transfer'")

        assertEquals("Renamed pocket", sourceRow.getValue("source_pocket_name"))
        assertEquals(999, (sourceRow.getValue("source_pocket_color") as Number).toInt())
        assertEquals("Renamed pocket", destinationRow.getValue("destination_pocket_name"))
        assertEquals(999, (destinationRow.getValue("destination_pocket_color") as Number).toInt())
        assertEquals("Renamed pocket", transferRow.getValue("transfer_pocket_name"))
        assertEquals(999, (transferRow.getValue("transfer_pocket_color") as Number).toInt())
    }

    @Test
    fun `partner updates propagate denormalized feed metadata`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Test Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )
        val pocket = createPocket(CreatePocket.Command(account.id, "Pocket", null, 100, true))
        val partner = createPartner(CreatePartner.Command(name = "Old partner", notes = null))

        insertAccountFeedRow(
            accountId = account.id,
            transactionId = "tx_partner",
            historyPosition = 1,
            transactionDate = "2026-04-01",
            description = "partner-row",
            transactionAmount = 10,
            signedAmount = -10,
            balanceAfter = -10,
            partnerId = partner.id,
            partnerName = partner.name,
            sourcePocketId = pocket.id,
            sourcePocketName = pocket.name,
            sourcePocketColor = pocket.color,
        )
        insertPocketFeedRow(
            pocketId = pocket.id,
            accountId = account.id,
            transactionId = "ptx_partner",
            historyPosition = 1,
            transactionDate = "2026-04-01",
            description = "partner-row",
            transactionAmount = 10,
            signedAmount = -10,
            balanceAfter = -10,
            partnerId = partner.id,
            partnerName = partner.name,
        )

        updatePartner(UpdatePartner.Command(id = partner.id, name = "New partner", notes = null))

        val accountRow = querySingle("SELECT partner_name FROM account_transaction_feed WHERE transaction_id = 'tx_partner'")
        val pocketRow = querySingle("SELECT partner_name FROM pocket_transaction_feed WHERE transaction_id = 'ptx_partner'")

        assertEquals("New partner", accountRow.getValue("partner_name"))
        assertEquals("New partner", pocketRow.getValue("partner_name"))
    }

    @Test
    fun `account and pocket archive state is projected to query summaries`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Test Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )
        val pocket = createPocket(CreatePocket.Command(account.id, "Bills", null, 123, true))

        archiveAccount(account.id)
        archivePocket(pocket.id)

        webTestClient.get()
            .uri("/query/accounts/${account.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isArchived").isEqualTo(true)

        webTestClient.get()
            .uri("/query/pockets/${pocket.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isArchived").isEqualTo(true)

        unarchiveAccount(account.id)
        unarchivePocket(pocket.id)

        webTestClient.get()
            .uri("/query/accounts/${account.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isArchived").isEqualTo(false)

        webTestClient.get()
            .uri("/query/pockets/${pocket.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isArchived").isEqualTo(false)
    }

    @Test
    fun `query endpoints return not found for unknown ids`() {
        webTestClient.get()
            .uri("/query/accounts/acc_missing")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .expectApiError(code = "not_found", details = mapOf("id" to "acc_missing"))

        webTestClient.get()
            .uri("/query/pockets/poc_missing")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .expectApiError(code = "not_found", details = mapOf("id" to "poc_missing"))
    }

    private fun insertCurrency(code: String) {
        runBlocking {
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
    }

    private suspend fun insertAccountFeedRow(
        accountId: String,
        transactionId: String,
        historyPosition: Long,
        transactionDate: String,
        description: String,
        transactionAmount: Long,
        signedAmount: Long,
        balanceAfter: Long,
        partnerId: String? = null,
        partnerName: String? = null,
        sourcePocketId: String? = null,
        sourcePocketName: String? = null,
        sourcePocketColor: Int? = null,
        destinationPocketId: String? = null,
        destinationPocketName: String? = null,
        destinationPocketColor: Int? = null,
    ) {
        var spec = databaseClient.sql(
            """
            INSERT INTO account_transaction_feed (
                account_id, transaction_id, history_position, transaction_date, type, status, description,
                transaction_amount, signed_amount, balance_after, partner_id, partner_name,
                source_pocket_id, source_pocket_name, source_pocket_color,
                destination_pocket_id, destination_pocket_name, destination_pocket_color, is_archived
            ) VALUES (
                :accountId, :transactionId, :historyPosition, :transactionDate, 'expense', 'booked', :description,
                :transactionAmount, :signedAmount, :balanceAfter, :partnerId, :partnerName,
                :sourcePocketId, :sourcePocketName, :sourcePocketColor,
                :destinationPocketId, :destinationPocketName, :destinationPocketColor, FALSE
            )
            """.trimIndent(),
        )
            .bind("accountId", accountId)
            .bind("transactionId", transactionId)
            .bind("historyPosition", historyPosition)
            .bind("transactionDate", transactionDate)
            .bind("description", description)
            .bind("transactionAmount", transactionAmount)
            .bind("signedAmount", signedAmount)
            .bind("balanceAfter", balanceAfter)
        spec = bindNullable(spec, "partnerId", partnerId, String::class.java)
        spec = bindNullable(spec, "partnerName", partnerName, String::class.java)
        spec = bindNullable(spec, "sourcePocketId", sourcePocketId, String::class.java)
        spec = bindNullable(spec, "sourcePocketName", sourcePocketName, String::class.java)
        spec = bindNullable(spec, "sourcePocketColor", sourcePocketColor, Int::class.javaObjectType)
        spec = bindNullable(spec, "destinationPocketId", destinationPocketId, String::class.java)
        spec = bindNullable(spec, "destinationPocketName", destinationPocketName, String::class.java)
        spec = bindNullable(spec, "destinationPocketColor", destinationPocketColor, Int::class.javaObjectType)
        spec.fetch().rowsUpdated().awaitSingle()
    }

    private suspend fun insertPocketFeedRow(
        pocketId: String,
        accountId: String,
        transactionId: String,
        historyPosition: Long,
        transactionDate: String,
        description: String,
        transactionAmount: Long,
        signedAmount: Long,
        balanceAfter: Long,
        partnerId: String? = null,
        partnerName: String? = null,
        transferPocketId: String? = null,
        transferPocketName: String? = null,
        transferPocketColor: Int? = null,
    ) {
        var spec = databaseClient.sql(
            """
            INSERT INTO pocket_transaction_feed (
                pocket_id, account_id, transaction_id, history_position, transaction_date, type, status, description,
                transaction_amount, signed_amount, balance_after, partner_id, partner_name,
                transfer_pocket_id, transfer_pocket_name, transfer_pocket_color, is_archived
            ) VALUES (
                :pocketId, :accountId, :transactionId, :historyPosition, :transactionDate, 'transfer', 'booked', :description,
                :transactionAmount, :signedAmount, :balanceAfter, :partnerId, :partnerName,
                :transferPocketId, :transferPocketName, :transferPocketColor, FALSE
            )
            """.trimIndent(),
        )
            .bind("pocketId", pocketId)
            .bind("accountId", accountId)
            .bind("transactionId", transactionId)
            .bind("historyPosition", historyPosition)
            .bind("transactionDate", transactionDate)
            .bind("description", description)
            .bind("transactionAmount", transactionAmount)
            .bind("signedAmount", signedAmount)
            .bind("balanceAfter", balanceAfter)
        spec = bindNullable(spec, "partnerId", partnerId, String::class.java)
        spec = bindNullable(spec, "partnerName", partnerName, String::class.java)
        spec = bindNullable(spec, "transferPocketId", transferPocketId, String::class.java)
        spec = bindNullable(spec, "transferPocketName", transferPocketName, String::class.java)
        spec = bindNullable(spec, "transferPocketColor", transferPocketColor, Int::class.javaObjectType)
        spec.fetch().rowsUpdated().awaitSingle()
    }

    private suspend fun querySingle(sql: String): Map<String, Any?> =
        databaseClient.sql(sql)
            .fetch()
            .one()
            .awaitSingle()

    private fun <T : Any> bindNullable(
        spec: DatabaseClient.GenericExecuteSpec,
        name: String,
        value: T?,
        type: Class<T>,
    ): DatabaseClient.GenericExecuteSpec =
        if (value == null) {
            spec.bindNull(name, type)
        } else {
            spec.bind(name, value)
        }
}
