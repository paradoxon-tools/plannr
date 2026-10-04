package de.chennemann.plannr.server.banking

import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import tools.jackson.databind.JsonNode

data class StartConnection(val bankName: String, val country: String, val validUntil: String, val authMethod: String? = null)
data class Authorization(val connectionId: UUID, val authorizationUrl: String)
data class CompleteAuthorization(val state: String, val code: String? = null, val error: String? = null)
data class AccountLink(val accountId: Long)
data class SyncRequest(val dateFrom: LocalDate, val dateTo: LocalDate)
data class SyncResult(val imported: Int, val syncedAt: Long)
data class ReconcileRequest(val materializationId: Long, val acceptAmountDifference: Boolean = false, val note: String? = null)
data class IgnoreRequest(val ignored: Boolean)
data class BankConnection(
    val id: UUID, val bankName: String, val country: String, val status: String,
    val validUntil: Long?, val createdAt: Long, val lastError: String?,
)
data class BankAccount(
    val id: Long, val connectionId: UUID, val name: String, val iban: String?, val currencyCode: String,
    val accountId: Long?, val balances: JsonNode, val lastSyncedAt: Long?, val lastError: String?,
)
data class BankTransaction(
    val id: Long, val bankAccountId: Long, val entryReference: String?, val identityQuality: String,
    val bookingStatus: String, val amount: BigDecimal, val currencyCode: String,
    val bookingDate: LocalDate?, val valueDate: LocalDate?, val transactionDate: LocalDate?,
    val counterparty: String?, val description: String, val reconciliationStatus: String,
    val isCurrent: Boolean, val firstSeenAt: Long, val lastSeenAt: Long,
)
data class PlannedTransaction(
    val materializationId: Long, val templateVersionId: Long, val occurrenceDate: String,
    val title: String, val amount: BigDecimal, val currencyCode: String, val reconciliationStatus: String,
)
data class Reconciliation(
    val bankTransactionId: Long, val accountId: Long, val templateVersionId: Long,
    val occurrenceDate: String, val plannedAmount: BigDecimal, val actualAmount: BigDecimal,
    val difference: BigDecimal, val plannedSnapshot: JsonNode, val note: String?, val createdAt: Long,
)
data class MatchSuggestion(val planned: PlannedTransaction, val amountDifference: BigDecimal, val daysDifference: Long)
