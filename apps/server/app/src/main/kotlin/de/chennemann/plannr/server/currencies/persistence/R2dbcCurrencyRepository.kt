package de.chennemann.plannr.server.currencies.persistence

import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.domain.CurrencyRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class R2dbcCurrencyRepository(
    private val databaseClient: DatabaseClient,
) : CurrencyRepository {
    override suspend fun save(currency: Currency): Currency {
        databaseClient.sql(
            """
            INSERT INTO currencies (code, name, symbol, decimal_places, symbol_position)
            VALUES (:code, :name, :symbol, :decimalPlaces, :symbolPosition)
            """.trimIndent(),
        )
            .bind("code", currency.code)
            .bind("name", currency.name)
            .bind("symbol", currency.symbol)
            .bind("decimalPlaces", currency.decimalPlaces)
            .bind("symbolPosition", currency.symbolPosition)
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        return currency
    }

    override suspend fun update(currency: Currency): Currency {
        databaseClient.sql(
            """
            UPDATE currencies
            SET name = :name,
                symbol = :symbol,
                decimal_places = :decimalPlaces,
                symbol_position = :symbolPosition
            WHERE code = :code
            """.trimIndent(),
        )
            .bind("code", currency.code)
            .bind("name", currency.name)
            .bind("symbol", currency.symbol)
            .bind("decimalPlaces", currency.decimalPlaces)
            .bind("symbolPosition", currency.symbolPosition)
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        return currency
    }

    override suspend fun findByCode(code: String): Currency? =
        databaseClient.sql(
            """
            SELECT code, name, symbol, decimal_places, symbol_position
            FROM currencies
            WHERE code = :code
            """.trimIndent(),
        )
            .bind("code", code)
            .fetch()
            .one()
            .map(::toCurrency)
            .awaitSingleOrNull()


    private fun toCurrency(row: Map<String, Any>): Currency =
        Currency(
            code = row.getValue("code") as String,
            name = row.getValue("name") as String,
            symbol = row.getValue("symbol") as String,
            decimalPlaces = (row.getValue("decimal_places") as Number).toInt(),
            symbolPosition = row.getValue("symbol_position") as String,
        )
}
