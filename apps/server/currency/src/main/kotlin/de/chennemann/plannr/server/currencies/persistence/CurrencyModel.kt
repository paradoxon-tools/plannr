package de.chennemann.plannr.server.currencies.persistence

import de.chennemann.plannr.server.currencies.domain.Currency
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("currencies")
data class CurrencyModel(
    @Id
    val code: String,
    val name: String,
    val symbol: String,
    @Column("decimal_places")
    val decimalPlaces: Int,
    @Column("symbol_position")
    val symbolPosition: String,
    @Transient
    val persisted: Boolean = false,
) : Persistable<String> {
    @PersistenceCreator
    constructor(
        code: String,
        name: String,
        symbol: String,
        decimalPlaces: Int,
        symbolPosition: String,
    ) : this(code, name, symbol, decimalPlaces, symbolPosition, persisted = true)

    override fun getId(): String = code

    override fun isNew(): Boolean = !persisted

    fun persisted(): CurrencyModel = copy(persisted = true)
}

internal fun CurrencyModel.toDomain(): Currency =
    Currency(
        code = code,
        name = name,
        symbol = symbol,
        decimalPlaces = decimalPlaces,
        symbolPosition = symbolPosition,
    )

internal fun Currency.toModel(): CurrencyModel =
    CurrencyModel(
        code = code,
        name = name,
        symbol = symbol,
        decimalPlaces = decimalPlaces,
        symbolPosition = symbolPosition,
    )

internal fun Currency.toPersistedModel(): CurrencyModel = toModel().persisted()
