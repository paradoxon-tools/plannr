package de.chennemann.plannr.server.banking

import de.chennemann.plannr.server.common.error.ApiException
import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.error.ValidationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import tools.jackson.databind.JsonNode
import java.net.URI
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.UUID

internal fun checkInput(condition: Boolean, message: String) {
    if (!condition) throw ValidationException("banking_validation", message)
}

@Service
class BankConnectionService(
    private val db: BankingDatabase,
    private val provider: BankingProvider,
    @param:Value("\${plannr.banking.redirect-url:}") private val redirectUrl: String,
) {
    suspend fun institutions(country: String): JsonNode {
        checkInput(country.matches(Regex("[A-Z]{2}")), "country must be a two-letter uppercase country code")
        return provider.request("GET", "/aspsps", query = mapOf("country" to country))
    }

    suspend fun start(command: StartConnection): Authorization {
        checkInput(command.bankName.isNotBlank() && command.bankName.length <= 255, "bankName is required")
        checkInput(command.country.matches(Regex("[A-Z]{2}")), "country must be a two-letter uppercase country code")
        val validUntil = try { Instant.parse(command.validUntil) } catch (e: Exception) { throw ValidationException("banking_validation", "validUntil must be an ISO instant") }
        checkInput(validUntil.isAfter(Instant.now()) && validUntil.isBefore(Instant.now().plusSeconds(180 * 86400L)), "validUntil must be within the next 180 days; also respect the bank's maximum consent duration")
        if (redirectUrl.isBlank()) throw ApiException(HttpStatus.SERVICE_UNAVAILABLE, "banking_not_configured", "Banking redirect URL is not configured")
        val redirect = URI(redirectUrl)
        checkInput(redirect.scheme == "https" || (redirect.scheme == "http" && redirect.host in setOf("localhost", "127.0.0.1")), "Configure an HTTPS redirect URL (HTTP is supported for localhost)")
        val state = Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(32).also(SecureRandom()::nextBytes))
        val id = UUID.randomUUID()
        val now = System.currentTimeMillis()
        db.transaction {
            db.execute("""INSERT INTO bank_connections(id, bank_name, country, status, state_hash, state_expires_at, created_at)
                VALUES (:id,:name,:country,'AUTHORIZING',:state,:expires,:now)""", "id" to id, "name" to command.bankName,
                "country" to command.country, "state" to sha256(state), "expires" to now + 30 * 60 * 1000, "now" to now)
        }
        try {
            val body = mutableMapOf<String, Any>("access" to mapOf("valid_until" to validUntil.toString()),
                "aspsp" to mapOf("name" to command.bankName, "country" to command.country),
                "state" to state, "redirect_url" to redirectUrl, "psu_type" to "personal")
            command.authMethod?.let { body["auth_method"] = it }
            val response = provider.request("POST", "/auth", body)
            return Authorization(id, response.requiredText("url"))
        } catch (e: ApiException) {
            db.execute("UPDATE bank_connections SET status='FAILED', state_hash=NULL, last_error=:error WHERE id=:id", "id" to id, "error" to e.code)
            throw e
        }
    }

    suspend fun complete(command: CompleteAuthorization): BankConnection {
        checkInput(command.state.length in 20..256, "Invalid authorization state")
        checkInput((command.code != null) xor (command.error != null), "Provide either code or error")
        checkInput(command.code == null || command.code.isNotBlank(), "Authorization code is empty")
        // Commit consumption before the one-use external exchange: replay and concurrent callbacks cannot exchange twice.
        val connection = db.transaction {
            val row = db.rows("""UPDATE bank_connections SET state_hash=NULL
                WHERE state_hash=:hash AND status='AUTHORIZING' AND state_expires_at>:now RETURNING *""",
                "hash" to sha256(command.state), "now" to System.currentTimeMillis()).singleOrNull()
                ?: throw ValidationException("invalid_authorization_state", "Authorization state is invalid, expired or already used; start a new connection")
            row
        }
        val id = connection.uuid("id")
        if (command.error != null) {
            db.execute("UPDATE bank_connections SET status='FAILED', last_error='authorization_denied' WHERE id=:id", "id" to id)
            return get(id)
        }
        try {
            val session = provider.request("POST", "/sessions", mapOf("code" to command.code))
            db.transaction {
                if (db.connection(id, true).string("status") != "AUTHORIZING") {
                    throw ConflictException("authorization_cancelled", "This connection was disconnected during authorization")
                }
                val bank = session.get("aspsp") ?: providerFailure()
                if (bank.requiredText("name") != connection.string("bank_name") || bank.requiredText("country") != connection.string("country")) providerFailure()
                val sessionId = UUID.fromString(session.requiredText("session_id")).toString()
                val validUntil = Instant.parse(session.get("access")?.requiredText("valid_until") ?: providerFailure()).toEpochMilli()
                db.execute("UPDATE bank_connections SET status='ACTIVE',session_id=:session,valid_until=:until,last_error=NULL WHERE id=:id",
                    "id" to id, "session" to sessionId, "until" to validUntil)
                val accounts = session.get("accounts")?.takeIf { it.isArray } ?: providerFailure()
                for (account in accounts) saveAccount(id, account)
            }
        } catch (e: Exception) {
            db.execute("UPDATE bank_connections SET status='FAILED',last_error='authorization_exchange_failed' WHERE id=:id AND status='AUTHORIZING'", "id" to id)
            if (e is ApiException || e is kotlinx.coroutines.CancellationException) throw e
            providerFailure()
        }
        return get(id)
    }

    private suspend fun saveAccount(connectionId: UUID, account: JsonNode, refreshing: Boolean = false) {
        val hash = account.requiredText("identification_hash")
        val uid = UUID.fromString(account.requiredText("uid")).toString()
        val iban = account.get("account_id")?.textOrNull("iban")
        // Alternative hashes may be shared by different pockets; never use them for identity.
        val existing = db.rows("""SELECT a.id FROM bank_accounts a JOIN bank_connections old ON old.id=a.connection_id
            JOIN bank_connections current ON current.id=:connection
            WHERE a.primary_identification_hash=:hash OR
              (a.primary_identification_hash IS NULL AND old.bank_name=current.bank_name AND old.country=current.country
               AND ((a.connection_id=:connection AND a.provider_uid=:uid) OR (:iban IS NOT NULL AND a.iban=:iban)))""",
            "connection" to connectionId, "hash" to hash, "uid" to uid, "iban" to iban).map { it.long("id") }
        if (existing.size > 1) throw ConflictException("ambiguous_bank_account", "Bank identifiers refer to multiple local bank accounts")
        val currency = account.requiredText("currency")
        val values = arrayOf("connection" to connectionId, "uid" to uid, "name" to (account.textOrNull("name") ?: "Bank account"),
            "iban" to iban, "currency" to currency, "hash" to hash)
        if (existing.isEmpty()) {
            db.one("""INSERT INTO bank_accounts(connection_id,provider_uid,name,iban,currency_code,primary_identification_hash)
                VALUES(:connection,:uid,:name,:iban,:currency,:hash) RETURNING id""", *values).long("id")
        } else {
            val old = db.account(existing.single(), true)
            // Checking an old consent must not move an account away from its renewed connection.
            if (refreshing && old.uuid("connection_id") != connectionId) return
            if (old.string("currency_code") != currency) throw ConflictException("bank_currency_changed", "Bank account currency changed; review the connection")
            if (old.text("iban") != null && iban != null && old.text("iban") != iban) {
                throw ConflictException("bank_identity_changed", "Bank account identity refers to a different IBAN; review the connection")
            }
            db.execute("""UPDATE bank_accounts SET connection_id=:connection,provider_uid=:uid,name=:name,iban=:iban,currency_code=:currency,
                primary_identification_hash=:hash,last_error=NULL WHERE id=:id""",
                *values, "id" to existing.single())
            existing.single()
        }
    }

    suspend fun list() = db.rows("SELECT * FROM bank_connections ORDER BY created_at DESC").map { it.connection(System.currentTimeMillis()) }
    suspend fun get(id: UUID) = db.connection(id).connection(System.currentTimeMillis())

    suspend fun refresh(id: UUID): BankConnection = db.transaction {
        val connection = db.connection(id, true)
        val sessionId = connection.text("session_id") ?: return@transaction get(id)
        val session = provider.request("GET", "/sessions/$sessionId")
        val status = when (session.requiredText("status")) {
            "AUTHORIZED" -> "ACTIVE"
            "EXPIRED" -> "EXPIRED"
            "CLOSED" -> "DISCONNECTED"
            "REVOKED", "CANCELLED", "INVALID" -> "FAILED"
            else -> providerFailure()
        }
        db.execute("UPDATE bank_connections SET status=:status,last_error=:error WHERE id=:id", "id" to id,
            "status" to status, "error" to if (status == "FAILED") "consent_revoked_or_invalid" else null)
        if (status == "ACTIVE") {
            val accounts = session.get("accounts")?.takeIf { it.isArray } ?: providerFailure()
            for (account in accounts) {
                val uid = UUID.fromString(account.asString())
                saveAccount(id, provider.request("GET", "/accounts/$uid/details"), refreshing = true)
            }
        }
        get(id)
    }
    suspend fun accounts(connectionId: UUID?): List<BankAccount> {
        connectionId?.let { db.connection(it) }
        return (if (connectionId == null) db.rows("SELECT * FROM bank_accounts ORDER BY id")
            else db.rows("SELECT * FROM bank_accounts WHERE connection_id=:id ORDER BY id", "id" to connectionId)).map { it.account() }
    }

    suspend fun disconnect(id: UUID): BankConnection = db.transaction {
        val connection = db.connection(id, true)
        if (connection.string("status") != "DISCONNECTED") {
            connection.text("session_id")?.let {
                try { provider.request("DELETE", "/sessions/$it") }
                catch (e: ApiException) { if (e.code != "banking_resource_missing") throw e }
            }
            db.execute("UPDATE bank_connections SET status='DISCONNECTED', session_id=NULL, state_hash=NULL, last_error=NULL WHERE id=:id", "id" to id)
        }
        get(id)
    }

    suspend fun link(id: Long, accountId: Long?): BankAccount = db.transaction {
        val bank = db.account(id, true)
        if (bank.optionalLong("account_id") != accountId) {
            if (db.rows("SELECT 1 FROM bank_reconciliations r JOIN bank_transactions t ON t.id=r.bank_transaction_id WHERE t.bank_account_id=:id LIMIT 1", "id" to id).isNotEmpty()) {
                throw ConflictException("account_has_reconciliations", "Undo reconciliations before changing the account link")
            }
            if (accountId != null) {
                val account = db.one("SELECT * FROM accounts WHERE id=:id FOR SHARE", "id" to accountId)
                checkInput(!account.bool("is_archived"), "Cannot link an archived account")
                checkInput(account.string("currency_code") == bank.string("currency_code"), "Account currencies must match")
            }
            if (accountId == null) db.execute("UPDATE bank_accounts SET account_id=NULL WHERE id=:id", "id" to id)
            else db.execute("UPDATE bank_accounts SET account_id=:account WHERE id=:id", "id" to id, "account" to accountId)
        }
        db.account(id).account()
    }
}
