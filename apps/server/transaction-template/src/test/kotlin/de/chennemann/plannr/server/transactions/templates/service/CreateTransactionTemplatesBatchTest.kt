package de.chennemann.plannr.server.transactions.templates.service

import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.contracts.api.dto.Contract
import de.chennemann.plannr.server.contracts.api.dto.CreateContractCommand
import de.chennemann.plannr.server.contracts.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.contracts.service.ContractService
import de.chennemann.plannr.server.financialprofiles.api.dto.CreateFinancialProfileCommand
import de.chennemann.plannr.server.financialprofiles.api.dto.FinancialProfile
import de.chennemann.plannr.server.financialprofiles.api.dto.UpdateFinancialProfileCommand
import de.chennemann.plannr.server.financialprofiles.service.FinancialProfileService
import de.chennemann.plannr.server.transactions.materialization.service.MaterializationOperation
import de.chennemann.plannr.server.transactions.materialization.service.MaterializedTransaction
import de.chennemann.plannr.server.transactions.materialization.service.TransactionMaterializerService
import de.chennemann.plannr.server.transactions.projection.service.TransactionProjectionChangeEvent
import de.chennemann.plannr.server.transactions.projection.service.TransactionProjectionEventQueue
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateCommand
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplateRepository
import de.chennemann.plannr.server.transactions.templates.persistence.TransactionTemplateModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class CreateTransactionTemplatesBatchTest {
    @Test
    fun `creates and materializes independent transaction templates`() = runTest {
        val repository = InMemoryTransactionTemplateRepository()
        val materializer = RecordingTransactionMaterializerService()
        val projectionQueue = RecordingProjectionEventQueue()
        val service = TransactionTemplateServiceImpl(
            transactionTemplateRepository = repository,
            transactionMaterializerService = materializer,
            financialProfileService = FakeFinancialProfileService(),
            contractService = FakeContractService(),
            timeProvider = { CREATED_AT },
            projectionEventQueue = projectionQueue,
        )

        val created = service.createBatch(
            listOf(
                createCommand(
                    amount = 799,
                    firstOccurrenceDate = "2025-01-15",
                    recurrenceType = "MONTHLY",
                    daysOfMonth = listOf(15),
                ),
                createCommand(
                    amount = 2500,
                    firstOccurrenceDate = "2025-06-01",
                    recurrenceType = "YEARLY",
                    daysOfMonth = listOf(1),
                    monthsOfYear = listOf(6),
                ),
            ),
        )

        assertEquals(listOf(799L, 2500L), created.map { it.amount })
        assertEquals(listOf(1L, 1L), created.map { it.financialProfileId })
        assertEquals(listOf("MONTHLY", "YEARLY"), created.map { it.recurrencePattern.recurrenceType })
        assertEquals(listOf(1L, 2L), materializer.operations.map { it.transactionTemplate.id })
        assertEquals(
            listOf<TransactionProjectionChangeEvent>(
                TransactionProjectionChangeEvent.TransactionTemplateChanged(1L),
                TransactionProjectionChangeEvent.TransactionTemplateChanged(2L),
            ),
            projectionQueue.events,
        )
        assertEquals(2L, repository.count())
    }

    @Test
    fun `rejects an empty batch`() = runTest {
        val repository = InMemoryTransactionTemplateRepository()
        val service = TransactionTemplateServiceImpl(
            transactionTemplateRepository = repository,
            transactionMaterializerService = RecordingTransactionMaterializerService(),
            financialProfileService = FakeFinancialProfileService(),
            contractService = FakeContractService(),
            timeProvider = { CREATED_AT },
        )

        val exception = assertFailsWith<ValidationException> {
            service.createBatch(emptyList())
        }

        assertEquals("At least one transaction template is required", exception.message)
        assertEquals(0L, repository.count())
    }

    @Test
    fun `inherits and refreshes profile from linked contract`() = runTest {
        val repository = InMemoryTransactionTemplateRepository()
        val materializer = RecordingTransactionMaterializerService()
        val contracts = mutableMapOf(
            1L to contract(financialProfileId = 7L),
        )
        val service = TransactionTemplateServiceImpl(
            transactionTemplateRepository = repository,
            transactionMaterializerService = materializer,
            financialProfileService = FakeFinancialProfileService(),
            contractService = FakeContractService(contracts),
            timeProvider = { CREATED_AT },
        )

        val created = service.create(
            createCommand(
                amount = 799,
                firstOccurrenceDate = "2025-01-15",
                recurrenceType = "MONTHLY",
                daysOfMonth = listOf(15),
            ),
        )

        assertEquals(7L, created.financialProfileId)

        contracts[1L] = contract(financialProfileId = 8L)
        service.refreshFinancialProfilesForPocket(1L)

        assertEquals(8L, service.getById(created.id)?.financialProfileId)
        assertEquals(2, materializer.operations.size)
        assertIs<MaterializationOperation.FullRefresh>(materializer.operations.last())
        assertEquals(8L, materializer.operations.last().transactionTemplate.financialProfileId)
    }

    private fun contract(financialProfileId: Long) =
        Contract(
            id = 1L,
            pocketId = 1L,
            financialProfileId = financialProfileId,
            partnerId = null,
            signingDate = null,
            expirationDate = null,
            lastCancellationDate = null,
        )

    private fun createCommand(
        amount: Long,
        firstOccurrenceDate: String,
        recurrenceType: String,
        daysOfMonth: List<Int>,
        monthsOfYear: List<Int>? = null,
    ) = CreateTransactionTemplateCommand(
        sourcePocketId = 1L,
        destinationPocketId = null,
        financialProfileId = null,
        partnerId = 2L,
        title = "Subscription",
        description = null,
        amount = amount,
        currencyCode = "EUR",
        transactionType = "EXPENSE",
        firstOccurrenceDate = firstOccurrenceDate,
        finalOccurrenceDate = null,
        recurrenceType = recurrenceType,
        skipCount = 0,
        daysOfWeek = null,
        weeksOfMonth = null,
        daysOfMonth = daysOfMonth,
        monthsOfYear = monthsOfYear,
        maxRecurrenceCount = null,
    )

    private companion object {
        const val CREATED_AT = 1_721_000_000_000L
    }
}

private class FakeFinancialProfileService : FinancialProfileService {
    private val default = FinancialProfile(
        id = 1L,
        name = "Household",
        description = null,
        isDefault = true,
        isFallback = true,
        isArchived = false,
        createdAt = 1L,
    )

    override suspend fun resolveForAssignment(id: Long?): FinancialProfile = default.copy(id = id ?: default.id)
    override suspend fun getById(id: Long): FinancialProfile? = default.takeIf { it.id == id }
    override suspend fun create(command: CreateFinancialProfileCommand): FinancialProfile = unsupported()
    override suspend fun update(command: UpdateFinancialProfileCommand): FinancialProfile = unsupported()
    override suspend fun makeDefault(id: Long): FinancialProfile = unsupported()
    override suspend fun archive(id: Long): FinancialProfile = unsupported()
    override suspend fun unarchive(id: Long): FinancialProfile = unsupported()
    override suspend fun delete(id: Long) = unsupported<Unit>()
    override suspend fun list(query: String?, archived: Boolean): List<FinancialProfile> = listOf(default)

    private fun <T> unsupported(): T = throw UnsupportedOperationException("Not used")
}

private class FakeContractService(
    private val contracts: Map<Long, Contract> = emptyMap(),
) : ContractService {
    override suspend fun getById(id: Long): Contract? = contracts[id]
    override suspend fun create(command: CreateContractCommand): Contract = unsupported()
    override suspend fun update(command: UpdateContractCommand): Contract = unsupported()
    override suspend fun list(accountId: Long?, archived: Boolean): List<Contract> = contracts.values.toList()

    private fun <T> unsupported(): T = throw UnsupportedOperationException("Not used")
}

private class RecordingTransactionMaterializerService : TransactionMaterializerService {
    val operations = mutableListOf<MaterializationOperation>()

    override suspend fun materialize(operation: MaterializationOperation): List<MaterializedTransaction> {
        operations += operation
        return emptyList()
    }
}

private class RecordingProjectionEventQueue : TransactionProjectionEventQueue {
    val events = mutableListOf<TransactionProjectionChangeEvent>()

    override suspend fun enqueue(event: TransactionProjectionChangeEvent) {
        events += event
    }
}

private class InMemoryTransactionTemplateRepository : TransactionTemplateRepository {
    private val templates = linkedMapOf<Long, TransactionTemplateModel>()

    override suspend fun <S : TransactionTemplateModel> save(entity: S): S {
        val persisted = entity.copy(id = entity.id ?: (templates.size + 1).toLong())
        templates[requireNotNull(persisted.id)] = persisted
        @Suppress("UNCHECKED_CAST")
        return persisted as S
    }

    override suspend fun findById(id: Long): TransactionTemplateModel? = templates[id]

    override suspend fun existsById(id: Long): Boolean = templates.containsKey(id)

    override fun findAll(): Flow<TransactionTemplateModel> = templates.values.asFlow()

    override fun findAllById(ids: Iterable<Long>): Flow<TransactionTemplateModel> =
        ids.mapNotNull(templates::get).asFlow()

    override fun findAllById(ids: Flow<Long>): Flow<TransactionTemplateModel> = flow {
        ids.collect { id -> templates[id]?.let { emit(it) } }
    }

    override fun <S : TransactionTemplateModel> saveAll(entities: Iterable<S>): Flow<S> = flow {
        entities.forEach { emit(save(it)) }
    }

    override fun <S : TransactionTemplateModel> saveAll(entityStream: Flow<S>): Flow<S> = flow {
        entityStream.collect { emit(save(it)) }
    }

    override fun findAllByIsArchivedOrderByCreatedAtAscIdAsc(isArchived: Boolean): Flow<TransactionTemplateModel> =
        templates.values
            .filter { it.isArchived == isArchived }
            .sortedWith(compareBy<TransactionTemplateModel> { it.createdAt }.thenBy { it.id })
            .asFlow()

    override fun findAllByPocketId(pocketId: Long): Flow<TransactionTemplateModel> =
        templates.values
            .filter { it.sourcePocketId == pocketId || it.destinationPocketId == pocketId }
            .sortedWith(compareBy<TransactionTemplateModel> { it.createdAt }.thenBy { it.id })
            .asFlow()

    override fun findAllBySourcePocketIdAndIsArchivedOrDestinationPocketIdAndIsArchivedOrderByCreatedAtAscIdAsc(
        sourcePocketId: Long,
        sourceIsArchived: Boolean,
        destinationPocketId: Long,
        destinationIsArchived: Boolean,
    ): Flow<TransactionTemplateModel> =
        templates.values
            .filter {
                (it.sourcePocketId == sourcePocketId && it.isArchived == sourceIsArchived) ||
                    (it.destinationPocketId == destinationPocketId && it.isArchived == destinationIsArchived)
            }
            .sortedWith(compareBy<TransactionTemplateModel> { it.createdAt }.thenBy { it.id })
            .asFlow()

    override suspend fun count(): Long = templates.size.toLong()

    override suspend fun deleteById(id: Long) {
        templates.remove(id)
    }

    override suspend fun delete(entity: TransactionTemplateModel) {
        entity.id?.let(templates::remove)
    }

    override suspend fun deleteAllById(ids: Iterable<Long>) {
        ids.forEach(templates::remove)
    }

    override suspend fun deleteAll(entities: Iterable<TransactionTemplateModel>) {
        entities.mapNotNull { it.id }.forEach(templates::remove)
    }

    override suspend fun <S : TransactionTemplateModel> deleteAll(entityStream: Flow<S>) {
        entityStream.collect { delete(it) }
    }

    override suspend fun deleteAll() {
        templates.clear()
    }
}
