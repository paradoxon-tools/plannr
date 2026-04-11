package de.chennemann.plannr.server

import de.chennemann.plannr.server.support.ApiIntegrationTest
import de.chennemann.plannr.server.support.expectApiError
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.beans.factory.annotation.Autowired

class EnumValidationIntegrationTest : ApiIntegrationTest() {
    @Autowired lateinit var databaseClient: DatabaseClient

    @BeforeEach
    fun setUp() {
        cleanDatabase("transactions", "recurring_transactions", "contracts", "partners", "pockets", "accounts", "currencies")
        runBlocking {
            databaseClient.sql(
                """
                INSERT INTO currencies (code, name, symbol, decimal_places, symbol_position)
                VALUES ('EUR', 'Euro', '€', 2, 'before')
                """.trimIndent(),
            ).fetch().rowsUpdated().awaitSingle()
            databaseClient.sql(
                """
                INSERT INTO accounts (id, name, institution, currency_code, weekend_handling, is_archived, created_at)
                VALUES ('acc_123', 'Main account', 'Demo Bank', 'EUR', 'NO_SHIFT', FALSE, 1)
                """.trimIndent(),
            ).fetch().rowsUpdated().awaitSingle()
            databaseClient.sql(
                """
                INSERT INTO pockets (id, account_id, name, description, color, is_default, is_archived, created_at)
                VALUES ('poc_123', 'acc_123', 'Wallet', NULL, 123, TRUE, FALSE, 1)
                """.trimIndent(),
            ).fetch().rowsUpdated().awaitSingle()
        }
    }

    @Test
    fun `account api rejects invalid weekend handling enum`() {
        webTestClient.post()
            .uri("/accounts")
            .bodyValue(
                mapOf(
                    "name" to "Main account",
                    "institution" to "Demo Bank",
                    "currencyCode" to "EUR",
                    "weekendHandling" to "not_a_weekend_mode",
                ),
            )
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .expectApiError("validation_error", "Account weekend handling is invalid")
    }

    @Test
    fun `transaction api rejects invalid transaction enums`() {
        webTestClient.post()
            .uri("/transactions")
            .bodyValue(
                mapOf(
                    "type" to "not_a_transaction_type",
                    "status" to "booked",
                    "transactionDate" to "2026-04-10",
                    "amount" to 100,
                    "currencyCode" to "EUR",
                    "exchangeRate" to null,
                    "destinationAmount" to null,
                    "description" to "Groceries",
                    "partnerId" to null,
                    "sourcePocketId" to "poc_123",
                    "destinationPocketId" to null,
                ),
            )
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .expectApiError("validation_error", "Transaction type is invalid")
    }

    @Test
    fun `recurring transaction api rejects invalid recurrence enum`() {
        webTestClient.post()
            .uri("/recurring-transactions")
            .bodyValue(
                mapOf(
                    "contractId" to null,
                    "sourcePocketId" to "poc_123",
                    "destinationPocketId" to null,
                    "partnerId" to null,
                    "title" to "Rent",
                    "description" to "Monthly rent",
                    "amount" to 1000,
                    "currencyCode" to "EUR",
                    "transactionType" to "EXPENSE",
                    "firstOccurrenceDate" to "2026-04-10",
                    "finalOccurrenceDate" to null,
                    "recurrenceType" to "not_a_recurrence_type",
                    "skipCount" to 0,
                    "daysOfWeek" to null,
                    "weeksOfMonth" to null,
                    "daysOfMonth" to null,
                    "monthsOfYear" to null,
                ),
            )
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .expectApiError("validation_error", "Recurring transaction recurrence type is invalid")
    }
}
