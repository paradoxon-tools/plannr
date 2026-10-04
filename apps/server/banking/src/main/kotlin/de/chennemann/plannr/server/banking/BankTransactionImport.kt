package de.chennemann.plannr.server.banking

import de.chennemann.plannr.server.common.error.ApiException
import org.springframework.http.HttpStatus
import tools.jackson.databind.JsonNode
import java.math.BigDecimal
import java.security.MessageDigest
import java.time.LocalDate

internal fun sha256(value: String): String = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
internal fun JsonNode.textOrNull(field: String): String? = get(field)?.takeUnless { it.isNull }?.asString()?.takeIf { it.isNotBlank() }
internal fun providerFailure(): Nothing = throw ApiException(HttpStatus.BAD_GATEWAY, "banking_invalid_response", "Enable Banking returned incomplete or unsupported data")
internal fun JsonNode.requiredText(field: String): String = textOrNull(field) ?: providerFailure()

/** One instance per complete fetch, so indistinguishable rows retain their multiplicity across pages. */
internal class BankTransactionImport {
    private val counts = mutableMapOf<String, Int>()
    fun parse(raw: JsonNode): ImportedTransaction {
        val money = raw.get("transaction_amount") ?: providerFailure()
        val amount = try { BigDecimal(money.requiredText("amount")) } catch (e: NumberFormatException) { providerFailure() }
        if (amount.signum() < 0) providerFailure()
        val signed = when (raw.requiredText("credit_debit_indicator")) { "DBIT" -> -amount; "CRDT" -> amount; else -> providerFailure() }
        val currency = money.requiredText("currency")
        if (!currency.matches(Regex("[A-Z]{3}"))) providerFailure()
        val status = raw.requiredText("status")
        if (status !in setOf("BOOK", "PDNG", "CNCL", "HOLD", "OTHR", "RJCT", "SCHD")) providerFailure()
        fun date(name: String) = raw.textOrNull(name)?.also { try { LocalDate.parse(it) } catch (e: Exception) { providerFailure() } }
        val bookingDate = date("booking_date")
        val valueDate = date("value_date")
        val transactionDate = date("transaction_date")
        val counterparty = raw.get(if (signed.signum() < 0) "creditor" else "debtor")?.textOrNull("name")
        val description = raw.get("remittance_information")?.takeIf { it.isArray }?.joinToString("\n") { it.asString() }.orEmpty()
        val reference = raw.textOrNull("entry_reference")
        fun accountIdentity(field: String): String? = raw.get(field)?.let { account ->
            listOf(account.textOrNull("iban"), account.get("other")?.textOrNull("scheme_name"),
                account.get("other")?.textOrNull("identification")).joinToString("") { "${it?.length ?: -1}:$it;" }
        }
        // transaction_id is explicitly NOT stable in Enable Banking's contract.
        val fingerprint = sha256(listOf(signed.stripTrailingZeros().toPlainString(), currency, status, bookingDate, valueDate,
            transactionDate, counterparty, description, raw.textOrNull("reference_number"),
            accountIdentity("creditor_account"), accountIdentity("debtor_account")).joinToString("") { "${it?.length ?: -1}:$it;" })
        val key = if (reference != null) "entry:$reference" else "fingerprint:$fingerprint:${counts.merge(fingerprint, 1, Int::plus)}"
        return ImportedTransaction(key, reference, if (reference == null) "FINGERPRINT" else "STABLE", status, signed, currency,
            bookingDate, valueDate, transactionDate, counterparty, description, raw)
    }
}

internal data class ImportedTransaction(
    val key: String, val reference: String?, val identityQuality: String, val status: String,
    val amount: BigDecimal, val currency: String, val bookingDate: String?, val valueDate: String?,
    val transactionDate: String?, val counterparty: String?, val description: String, val raw: JsonNode,
)
