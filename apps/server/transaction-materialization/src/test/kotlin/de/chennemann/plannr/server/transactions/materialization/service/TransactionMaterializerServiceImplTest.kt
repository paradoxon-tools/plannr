package de.chennemann.plannr.server.transactions.materialization.service

import de.chennemann.plannr.server.transactions.materialization.domain.MaterializedTransactionRepository
import de.chennemann.plannr.server.transactions.materialization.persistence.MaterializedTransactionModel
import de.chennemann.plannr.server.transactions.templates.domain.RecurrencePattern
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransactionMaterializerServiceImplTest {
    @Test
    fun `materializes recurring transactions through today only`() = runTest {
        val repository = InMemoryMaterializedTransactionRepository()
        val service = TransactionMaterializerServiceImpl(
            materializedTransactionRepository = repository,
            localDateProvider = { LocalDate.parse("2026-07-23") },
            timeProvider = { 1L },
        )

        val result = service.materialize(
            MaterializationOperation.NewTransactionTemplate(
                transactionTemplate(
                    firstOccurrenceDate = "2026-07-20",
                    recurrenceType = "DAILY",
                ),
            ),
        )

        assertEquals(
            listOf("2026-07-20", "2026-07-21", "2026-07-22", "2026-07-23"),
            result.map(MaterializedTransaction::transactionDate),
        )
        assertEquals(List(result.size) { 1L }, result.map(MaterializedTransaction::financialProfileId))
        assertTrue(result.none { LocalDate.parse(it.transactionDate).isAfter(LocalDate.parse("2026-07-23")) })
    }

    @Test
    fun `nightly reconciliation removes previously materialized future transactions`() = runTest {
        val repository = InMemoryMaterializedTransactionRepository()
        repository.save(transactionModel(id = 1L, date = "2026-07-23"))
        repository.save(transactionModel(id = 2L, date = "2026-07-24"))
        val service = TransactionMaterializerServiceImpl(
            materializedTransactionRepository = repository,
            localDateProvider = { LocalDate.parse("2026-07-23") },
            timeProvider = { 1L },
        )

        val result = service.materialize(
            MaterializationOperation.EndDateChange(
                transactionTemplate(
                    firstOccurrenceDate = "2026-07-23",
                    recurrenceType = "DAILY",
                ),
            ),
        )

        assertEquals(listOf("2026-07-23"), result.map(MaterializedTransaction::transactionDate))
        assertEquals(listOf(1L), result.map(MaterializedTransaction::financialProfileId))
    }

    private fun transactionTemplate(
        firstOccurrenceDate: String,
        recurrenceType: String,
    ) = TransactionTemplate(
        id = TEMPLATE_ID,
        sourcePocketId = 10L,
        destinationPocketId = null,
        financialProfileId = 1L,
        partnerId = null,
        title = "Test",
        description = null,
        amount = 100L,
        currencyCode = "EUR",
        transactionType = "EXPENSE",
        recurrencePattern = RecurrencePattern(
            firstOccurrenceDate = firstOccurrenceDate,
            finalOccurrenceDate = null,
            recurrenceType = recurrenceType,
            skipCount = 0,
            daysOfWeek = null,
            weeksOfMonth = null,
            daysOfMonth = null,
            monthsOfYear = null,
        ),
        previousVersionId = null,
        isArchived = false,
        createdAt = 1L,
    )

    private fun transactionModel(
        id: Long?,
        date: String,
    ) = MaterializedTransactionModel(
        id = id,
        transactionTemplateId = TEMPLATE_ID,
        transactionDate = date,
        sourcePocketId = 10L,
        destinationPocketId = null,
        financialProfileId = 1L,
        partnerId = null,
        title = "Test",
        description = null,
        amount = 100L,
        currencyCode = "EUR",
        transactionType = "EXPENSE",
        createdAt = 1L,
    )

    private companion object {
        const val TEMPLATE_ID = 1L
    }
}

private class InMemoryMaterializedTransactionRepository : MaterializedTransactionRepository {
    private val transactions = linkedMapOf<Long, MaterializedTransactionModel>()

    override suspend fun <S : MaterializedTransactionModel> save(entity: S): S {
        val persisted = entity.copy(id = entity.id ?: ((transactions.keys.maxOrNull() ?: 0L) + 1L))
        transactions[requireNotNull(persisted.id)] = persisted
        @Suppress("UNCHECKED_CAST")
        return persisted as S
    }

    override suspend fun findById(id: Long): MaterializedTransactionModel? = transactions[id]

    override suspend fun existsById(id: Long): Boolean = transactions.containsKey(id)

    override fun findAll(): Flow<MaterializedTransactionModel> = transactions.values.asFlow()

    override fun findAllById(ids: Iterable<Long>): Flow<MaterializedTransactionModel> =
        ids.mapNotNull(transactions::get).asFlow()

    override fun findAllById(ids: Flow<Long>): Flow<MaterializedTransactionModel> = flow {
        ids.collect { id -> transactions[id]?.let { emit(it) } }
    }

    override fun <S : MaterializedTransactionModel> saveAll(entities: Iterable<S>): Flow<S> = flow {
        entities.forEach { emit(save(it)) }
    }

    override fun <S : MaterializedTransactionModel> saveAll(entityStream: Flow<S>): Flow<S> = flow {
        entityStream.collect { emit(save(it)) }
    }

    override fun findAllByTransactionTemplateVersionIdOrderByTransactionDateAscIdAsc(
        transactionTemplateVersionId: Long,
    ): Flow<MaterializedTransactionModel> =
        transactions.values
            .filter { it.transactionTemplateVersionId == transactionTemplateVersionId }
            .sortedWith(compareBy<MaterializedTransactionModel> { it.transactionDate }.thenBy { it.id })
            .asFlow()

    override suspend fun findByTransactionTemplateVersionIdAndTransactionDate(
        transactionTemplateVersionId: Long,
        transactionDate: String,
    ): MaterializedTransactionModel? =
        transactions.values.find {
            it.transactionTemplateVersionId == transactionTemplateVersionId && it.transactionDate == transactionDate
        }

    override suspend fun deleteAllByTransactionTemplateVersionId(transactionTemplateVersionId: Long) {
        transactions.entries.removeIf { it.value.transactionTemplateVersionId == transactionTemplateVersionId }
    }

    override suspend fun deleteAllByTransactionTemplateVersionIdAndTransactionDateNotIn(
        transactionTemplateVersionId: Long,
        transactionDates: Collection<String>,
    ) {
        transactions.entries.removeIf {
            it.value.transactionTemplateVersionId == transactionTemplateVersionId &&
                it.value.transactionDate !in transactionDates
        }
    }

    override suspend fun count(): Long = transactions.size.toLong()

    override suspend fun deleteById(id: Long) {
        transactions.remove(id)
    }

    override suspend fun delete(entity: MaterializedTransactionModel) {
        entity.id?.let(transactions::remove)
    }

    override suspend fun deleteAllById(ids: Iterable<Long>) {
        ids.forEach(transactions::remove)
    }

    override suspend fun deleteAll(entities: Iterable<MaterializedTransactionModel>) {
        entities.mapNotNull { it.id }.forEach(transactions::remove)
    }

    override suspend fun <S : MaterializedTransactionModel> deleteAll(entityStream: Flow<S>) {
        entityStream.collect { delete(it) }
    }

    override suspend fun deleteAll() {
        transactions.clear()
    }
}
