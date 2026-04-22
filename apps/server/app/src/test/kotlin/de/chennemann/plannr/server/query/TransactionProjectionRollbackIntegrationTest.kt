package de.chennemann.plannr.server.query

import de.chennemann.plannr.server.accounts.usecases.CreateAccount
import de.chennemann.plannr.server.common.events.ApplicationEventHandler
import de.chennemann.plannr.server.pockets.service.CreatePocketCommand
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.support.ApiIntegrationTest
import de.chennemann.plannr.server.transactions.events.TransactionCreated
import de.chennemann.plannr.server.transactions.usecases.CreateTransaction
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.r2dbc.core.DatabaseClient

@Import(TransactionProjectionRollbackIntegrationTest.FailingProjectorConfiguration::class)
class TransactionProjectionRollbackIntegrationTest : ApiIntegrationTest() {
    @Autowired lateinit var databaseClient: DatabaseClient
    @Autowired lateinit var createAccount: CreateAccount
    @Autowired lateinit var pocketService: PocketService
    @Autowired lateinit var createTransaction: CreateTransaction

    @BeforeEach
    fun setUp() {
        cleanDatabase(
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
    fun `projector failure rolls back command and query writes`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Demo Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )
        val pocket = pocketService.create(CreatePocketCommand(account.id, "Wallet", null, 123, true))

        assertFailsWith<IllegalStateException> {
            runBlocking {
                createTransaction(
                    CreateTransaction.Command(
                        type = "EXPENSE",
                        status = "CLEARED",
                        transactionDate = "2026-04-10",
                        amount = 100,
                        currencyCode = "EUR",
                        exchangeRate = null,
                        destinationAmount = null,
                        description = "boom",
                        partnerId = null,
                        sourcePocketId = pocket.id,
                        destinationPocketId = null,
                    ),
                )
            }
        }

        assertEquals(0L, count("SELECT COUNT(*) AS value FROM transactions"))
        assertEquals(0L, count("SELECT COUNT(*) AS value FROM account_transaction_feed"))
        assertEquals(0L, count("SELECT COUNT(*) AS value FROM pocket_transaction_feed"))
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

    private suspend fun count(sql: String): Long = singleLong(sql)

    private suspend fun singleLong(sql: String): Long =
        (databaseClient.sql(sql)
            .fetch()
            .one()
            .awaitSingle()
            .getValue("value") as Number).toLong()

    @TestConfiguration
    class FailingProjectorConfiguration {
        @Bean
        fun failingTransactionCreatedHandler(): ApplicationEventHandler<TransactionCreated> = object : ApplicationEventHandler<TransactionCreated> {
            override val eventType: KClass<TransactionCreated> = TransactionCreated::class

            override suspend fun handle(event: TransactionCreated) {
                if (event.transaction.description == "boom") {
                    throw IllegalStateException("forced projector failure")
                }
            }
        }
    }
}
