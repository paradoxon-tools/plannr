package de.chennemann.plannr.server.currencies.support

import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.domain.CurrencyRepository
import de.chennemann.plannr.server.currencies.persistence.CurrencyModel
import de.chennemann.plannr.server.currencies.persistence.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.springframework.data.domain.Sort

class InMemoryCurrencyRepository : CurrencyRepository {
    private val currencies = linkedMapOf<String, CurrencyModel>()

    override suspend fun insert(code: String, name: String, symbol: String, decimalPlaces: Int, symbolPosition: String): Int {
        currencies[code] = CurrencyModel(code, name, symbol, decimalPlaces, symbolPosition)
        return 1
    }

    override suspend fun updateByCode(code: String, name: String, symbol: String, decimalPlaces: Int, symbolPosition: String): Int {
        currencies[code] = CurrencyModel(code, name, symbol, decimalPlaces, symbolPosition, persisted = true)
        return 1
    }

    override suspend fun <S : CurrencyModel> save(entity: S): S {
        currencies[entity.code] = entity
        return entity
    }

    override suspend fun findById(id: String): CurrencyModel? = currencies[id]

    override suspend fun findByCode(code: String): CurrencyModel? = currencies[code]

    override suspend fun existsById(id: String): Boolean = currencies.containsKey(id)

    override fun findAll(): Flow<CurrencyModel> = currencies.values.asFlow()

    override fun findAll(sort: Sort): Flow<CurrencyModel> = findAllByOrderByCodeAsc()

    override fun findAllByOrderByCodeAsc(): Flow<CurrencyModel> =
        currencies.values.sortedBy { it.code }.asFlow()

    override fun findAllById(ids: Iterable<String>): Flow<CurrencyModel> =
        ids.mapNotNull(currencies::get).asFlow()

    override fun findAllById(ids: Flow<String>): Flow<CurrencyModel> = flow {
        ids.collect { id -> currencies[id]?.let { emit(it) } }
    }

    override fun <S : CurrencyModel> saveAll(entities: Iterable<S>): Flow<S> = flow {
        entities.forEach { emit(save(it)) }
    }

    override fun <S : CurrencyModel> saveAll(entityStream: Flow<S>): Flow<S> = flow {
        entityStream.collect { emit(save(it)) }
    }

    override suspend fun count(): Long = currencies.size.toLong()

    override suspend fun deleteById(id: String) {
        currencies.remove(id)
    }

    override suspend fun delete(entity: CurrencyModel) {
        currencies.remove(entity.code)
    }

    override suspend fun deleteAllById(ids: Iterable<String>) {
        ids.forEach(currencies::remove)
    }

    override suspend fun deleteAll(entities: Iterable<CurrencyModel>) {
        entities.map { it.code }.forEach(currencies::remove)
    }

    override suspend fun <S : CurrencyModel> deleteAll(entityStream: Flow<S>) {
        entityStream.collect { delete(it) }
    }

    override suspend fun deleteAll() {
        currencies.clear()
    }

    suspend fun save(currency: Currency): Currency = save(currency.toModel()).toDomain()

    private fun CurrencyModel.toDomain(): Currency =
        Currency(
            code = code,
            name = name,
            symbol = symbol,
            decimalPlaces = decimalPlaces,
            symbolPosition = symbolPosition,
        )
}
