package de.chennemann.plannr.server.banking

import de.chennemann.plannr.server.common.error.ApiException
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.math.BigDecimal
import java.security.KeyPairGenerator
import java.security.Signature
import java.time.Instant
import java.util.Base64
import kotlin.test.*

class BankTransactionImportTest {
    private val mapper = jacksonObjectMapper()
    private fun tx(extra: String = "", amount: String = "12.34", direction: String = "DBIT", status: String = "BOOK") =
        mapper.readTree("""{"transaction_amount":{"currency":"EUR","amount":"$amount"},"credit_debit_indicator":"$direction","status":"$status","booking_date":"2026-09-01"$extra}""")

    @Test fun `debits and credits retain exact decimal amounts`() {
        assertEquals(BigDecimal("-12.34"), BankTransactionImport().parse(tx()).amount)
        assertEquals(BigDecimal("0.001"), BankTransactionImport().parse(tx(amount="0.001", direction="CRDT")).amount)
    }

    @Test fun `entry reference wins over volatile transaction id`() {
        val first = BankTransactionImport().parse(tx(",\"entry_reference\":\"stable\",\"transaction_id\":\"first\""))
        val second = BankTransactionImport().parse(tx(",\"entry_reference\":\"stable\",\"transaction_id\":\"second\""))
        assertEquals(first.key, second.key)
        assertEquals("STABLE", first.identityQuality)
    }

    @Test fun `fallback identity retains duplicate multiplicity and is repeatable across imports`() {
        val batch = BankTransactionImport()
        val first = batch.parse(tx(",\"transaction_id\":\"a\""))
        val second = batch.parse(tx(",\"transaction_id\":\"b\""))
        assertNotEquals(first.key, second.key)
        assertEquals(first.key, BankTransactionImport().parse(tx(",\"transaction_id\":\"changed\"", amount="12.340")).key)
        assertEquals("FINGERPRINT", first.identityQuality)
    }

    @Test fun `all documented booking states are retained`() {
        for (status in listOf("BOOK", "PDNG", "CNCL", "HOLD", "OTHR", "RJCT", "SCHD")) {
            assertEquals(status, BankTransactionImport().parse(tx(status=status)).status)
        }
    }

    @Test fun `fallback identity ignores ordering of account identification properties`() {
        val first = tx(",\"creditor_account\":{\"iban\":\"DE123\",\"other\":{\"scheme_name\":\"BBAN\",\"identification\":\"123\"}}")
        val second = tx(",\"creditor_account\":{\"other\":{\"identification\":\"123\",\"scheme_name\":\"BBAN\"},\"iban\":\"DE123\"}")
        assertEquals(BankTransactionImport().parse(first).key, BankTransactionImport().parse(second).key)
    }

    @Test fun `malformed direction amount date and status fail without importing`() {
        assertFailsWith<ApiException> { BankTransactionImport().parse(tx(direction="UNKNOWN")) }
        assertFailsWith<ApiException> { BankTransactionImport().parse(tx(amount="NaN")) }
        assertFailsWith<ApiException> { BankTransactionImport().parse(tx(amount="-1")) }
        assertFailsWith<ApiException> { BankTransactionImport().parse(tx(status="UNKNOWN")) }
        assertFailsWith<ApiException> { BankTransactionImport().parse(mapper.readTree(tx().toString().replace("2026-09-01", "yesterday"))) }
    }

    @Test fun `jwt signature and claims follow Enable Banking authentication`() {
        val pair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val now = Instant.parse("2026-09-01T10:00:00Z")
        val parts = EnableBankingClient.jwt(mapper, "application", pair.private, now).split('.')
        val decode = Base64.getUrlDecoder()
        val header = mapper.readTree(decode.decode(parts[0]))
        val claims = mapper.readTree(decode.decode(parts[1]))
        assertEquals("RS256", header.get("alg").asString())
        assertEquals("application", header.get("kid").asString())
        assertEquals("enablebanking.com", claims.get("iss").asString())
        assertEquals("api.enablebanking.com", claims.get("aud").asString())
        assertEquals(now.epochSecond + 300, claims.get("exp").asLong())
        assertTrue(Signature.getInstance("SHA256withRSA").apply {
            initVerify(pair.public); update("${parts[0]}.${parts[1]}".toByteArray())
        }.verify(decode.decode(parts[2])))
    }
}
