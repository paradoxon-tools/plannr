package de.chennemann.plannr.server.currencies.domain

import de.chennemann.plannr.server.currencies.persistence.CurrencyModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface CurrencyRepository :
    CoroutineCrudRepository<CurrencyModel, String>,
    CoroutineSortingRepository<CurrencyModel, String> {
    @Modifying
    @Query(
        """
        INSERT INTO currencies (code, name, symbol, decimal_places, symbol_position)
        VALUES (:code, :name, :symbol, :decimalPlaces, :symbolPosition)
        """,
    )
    suspend fun insert(code: String, name: String, symbol: String, decimalPlaces: Int, symbolPosition: String): Int

    @Modifying
    @Query(
        """
        UPDATE currencies
        SET name = :name,
            symbol = :symbol,
            decimal_places = :decimalPlaces,
            symbol_position = :symbolPosition
        WHERE code = :code
        """,
    )
    suspend fun updateByCode(code: String, name: String, symbol: String, decimalPlaces: Int, symbolPosition: String): Int

    @Query(
        """
        SELECT code, name, symbol, decimal_places, symbol_position
        FROM currencies
        WHERE code = :code
        """,
    )
    suspend fun findByCode(code: String): CurrencyModel?
    fun findAllByOrderByCodeAsc(): Flow<CurrencyModel>
}
