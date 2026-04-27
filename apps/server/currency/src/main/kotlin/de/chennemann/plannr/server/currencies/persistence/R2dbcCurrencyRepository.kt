package de.chennemann.plannr.server.currencies.persistence

import de.chennemann.plannr.server.currencies.domain.CurrencyRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class R2dbcCurrencyRepository(
    private val databaseClient: DatabaseClient,
) : CurrencyRepository {
    override suspend fun save(currency: CurrencyModel): de.chennemann.plannr.server.currencies.domain.Currency {
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

        return currency.toDomain()
    }

    override suspend fun update(currency: CurrencyModel): de.chennemann.plannr.server.currencies.domain.Currency {
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

        return currency.toDomain()
    }

    override suspend fun findByCode(code: String): de.chennemann.plannr.server.currencies.domain.Currency? =
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

    override suspend fun findAll(): List<de.chennemann.plannr.server.currencies.domain.Currency> =
        databaseClient.sql(
            """
            SELECT code, name, symbol, decimal_places, symbol_position
            FROM currencies
            ORDER BY code ASC
            """.trimIndent(),
            )
            .fetch()
            .all()
            .map(::toCurrency)
            .collectList()
            .awaitSingle()

    private fun toCurrency(row: Map<String, Any>): de.chennemann.plannr.server.currencies.domain.Currency =
        CurrencyModel(
            code = row.getValue("code") as String,
            name = row.getValue("name") as String,
            symbol = row.getValue("symbol") as String,
            decimalPlaces = (row.getValue("decimal_places") as Number).toInt(),
            symbolPosition = row.getValue("symbol_position") as String,
        ).toDomain()
}
