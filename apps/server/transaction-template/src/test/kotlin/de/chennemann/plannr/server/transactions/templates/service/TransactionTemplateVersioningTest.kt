package de.chennemann.plannr.server.transactions.templates.service

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
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateCommand
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateVersionCommand
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateWithVersionsCommand
import de.chennemann.plannr.server.transactions.templates.api.dto.UpdateTransactionTemplateCommand
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplateRepository
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplateVersionRepository
import de.chennemann.plannr.server.transactions.templates.persistence.TransactionTemplateModel
import de.chennemann.plannr.server.transactions.templates.persistence.TransactionTemplateVersionModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TransactionTemplateVersioningTest {
    @Test
    fun `batch creates multiple templates with their complete version timelines`() = runTest {
        val service = fixture().service

        val templates = service.createBatch(
            listOf(
                batchCommand(version(799, "2025-01-15"), version(899, "2025-06-15")),
                batchCommand(version(2_500, "2025-02-01"), version(2_750, "2026-02-01")),
            ),
        )

        assertEquals(listOf(2, 2), templates.map { it.versions.size })
        assertEquals(listOf(899L, 2_750L), templates.map { it.currentVersion.amount })
        assertEquals("2025-06-14", templates.first().versions.first().validUntil)
        assertEquals("2026-01-31", templates.last().versions.first().validUntil)
    }

    @Test
    fun `a new template always contains an initial version`() = runTest {
        val service = fixture().service
        val template = service.create(command(799, "2025-01-15"))
        assertEquals(1, template.versions.size)
        assertEquals(template.id, template.currentVersion.transactionTemplateId)
        assertEquals(799, template.currentVersion.amount)
        assertNull(template.currentVersion.validUntil)
    }

    @Test
    fun `creating a future version closes the preceding validity window`() = runTest {
        val fixture = fixture()
        val template = fixture.service.create(command(799, "2025-01-15"))
        val updated = fixture.service.createVersion(template.id, version(899, "2025-06-15"))
        assertEquals(listOf("2025-01-15", "2025-06-15"), updated.versions.map { it.validFrom })
        assertEquals("2025-06-14", updated.versions.first().validUntil)
        assertNull(updated.versions.last().validUntil)
    }

    @Test
    fun `correcting a version start corrects the previous validity end`() = runTest {
        val fixture = fixture()
        val template = fixture.service.create(command(799, "2025-01-15"))
        val versioned = fixture.service.createVersion(template.id, version(899, "2025-06-15"))
        val successor = versioned.currentVersion
        val corrected = fixture.service.update(versioned.updateCommand(successor, "2025-07-15"))
        assertEquals("2025-07-14", corrected.versions.first().validUntil)
        assertEquals("2025-07-15", corrected.currentVersion.validFrom)
    }

    @Test
    fun `deleting the latest version reopens its predecessor`() = runTest {
        val fixture = fixture()
        val template = fixture.service.create(command(799, "2025-01-15"))
        val versioned = fixture.service.createVersion(template.id, version(899, "2025-06-15"))
        val remaining = fixture.service.deleteVersion(template.id, versioned.currentVersion.id)
        assertEquals(1, remaining?.versions?.size)
        assertNull(remaining?.currentVersion?.validUntil)
    }

    private fun fixture(): Fixture {
        val templates = InMemoryTemplateRepository()
        val versions = InMemoryVersionRepository()
        val materializer = RecordingMaterializer()
        return Fixture(
            TransactionTemplateServiceImpl(
                templates, versions, materializer, FakeProfiles(), FakeContracts(), { 1L },
            ),
        )
    }

    private fun command(amount: Long, start: String) = CreateTransactionTemplateCommand(
        null, 1, null, 1, null, "Subscription", null, amount, "EUR", "EXPENSE", start, null,
        "MONTHLY", 0, null, null, listOf(15), null, null,
    )

    private fun version(amount: Long, start: String) = CreateTransactionTemplateVersionCommand(
        amount, start, null, "MONTHLY", 0, null, null, listOf(15), null, null,
    )

    private fun batchCommand(vararg versions: CreateTransactionTemplateVersionCommand) =
        CreateTransactionTemplateWithVersionsCommand(
            null, 1, null, 1, null, "Subscription", null, "EUR", "EXPENSE", versions.toList(),
        )

    private fun de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate.updateCommand(
        version: de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplateVersion,
        start: String,
    ) =
        UpdateTransactionTemplateCommand(
            version.id, contractId, sourcePocketId, destinationPocketId, financialProfileId, partnerId, title,
            description, version.amount, currencyCode, transactionType, start, version.recurrencePattern.finalOccurrenceDate,
            version.recurrencePattern.recurrenceType, version.recurrencePattern.skipCount, version.recurrencePattern.daysOfWeek,
            version.recurrencePattern.weeksOfMonth, version.recurrencePattern.daysOfMonth, version.recurrencePattern.monthsOfYear, null,
        )

    private data class Fixture(val service: TransactionTemplateServiceImpl)
}

private class InMemoryTemplateRepository : TransactionTemplateRepository {
    private val data = linkedMapOf<Long, TransactionTemplateModel>()
    override suspend fun <S : TransactionTemplateModel> save(entity: S): S = entity.copy(id = entity.id ?: (data.size + 1L)).also { data[requireNotNull(it.id)] = it } as S
    override suspend fun findById(id: Long) = data[id]
    override suspend fun existsById(id: Long) = id in data
    override fun findAll() = data.values.asFlow()
    override fun findAllById(ids: Iterable<Long>) = ids.mapNotNull(data::get).asFlow()
    override fun findAllById(ids: Flow<Long>) = flow { ids.collect { data[it]?.let { value -> emit(value) } } }
    override fun <S : TransactionTemplateModel> saveAll(entities: Iterable<S>) = flow { entities.forEach { emit(save(it)) } }
    override fun <S : TransactionTemplateModel> saveAll(entityStream: Flow<S>) = flow { entityStream.collect { emit(save(it)) } }
    override fun findAllByIsArchivedOrderByCreatedAtAscIdAsc(isArchived: Boolean) = data.values.filter { it.isArchived == isArchived }.asFlow()
    override suspend fun count() = data.size.toLong()
    override suspend fun deleteById(id: Long) { data.remove(id) }
    override suspend fun delete(entity: TransactionTemplateModel) { entity.id?.let(data::remove) }
    override suspend fun deleteAllById(ids: Iterable<Long>) { ids.forEach(data::remove) }
    override suspend fun deleteAll(entities: Iterable<TransactionTemplateModel>) { entities.forEach { delete(it) } }
    override suspend fun <S : TransactionTemplateModel> deleteAll(entityStream: Flow<S>) { entityStream.collect { delete(it) } }
    override suspend fun deleteAll() { data.clear() }
}

private class InMemoryVersionRepository : TransactionTemplateVersionRepository {
    private val data = linkedMapOf<Long, TransactionTemplateVersionModel>()
    override suspend fun <S : TransactionTemplateVersionModel> save(entity: S): S = entity.copy(id = entity.id ?: (data.size + 1L)).also { data[requireNotNull(it.id)] = it } as S
    override suspend fun findById(id: Long) = data[id]
    override suspend fun existsById(id: Long) = id in data
    override fun findAll() = data.values.asFlow()
    override fun findAllById(ids: Iterable<Long>) = ids.mapNotNull(data::get).asFlow()
    override fun findAllById(ids: Flow<Long>) = flow { ids.collect { data[it]?.let { value -> emit(value) } } }
    override fun <S : TransactionTemplateVersionModel> saveAll(entities: Iterable<S>) = flow { entities.forEach { emit(save(it)) } }
    override fun <S : TransactionTemplateVersionModel> saveAll(entityStream: Flow<S>) = flow { entityStream.collect { emit(save(it)) } }
    override fun findAllByTransactionTemplateIdOrderByValidFromAscIdAsc(transactionTemplateId: Long) = data.values.filter { it.transactionTemplateId == transactionTemplateId }.sortedBy { it.validFrom }.asFlow()
    override suspend fun deleteAllByTransactionTemplateId(transactionTemplateId: Long) { data.entries.removeIf { it.value.transactionTemplateId == transactionTemplateId } }
    override suspend fun count() = data.size.toLong()
    override suspend fun deleteById(id: Long) { data.remove(id) }
    override suspend fun delete(entity: TransactionTemplateVersionModel) { entity.id?.let(data::remove) }
    override suspend fun deleteAllById(ids: Iterable<Long>) { ids.forEach(data::remove) }
    override suspend fun deleteAll(entities: Iterable<TransactionTemplateVersionModel>) { entities.forEach { delete(it) } }
    override suspend fun <S : TransactionTemplateVersionModel> deleteAll(entityStream: Flow<S>) { entityStream.collect { delete(it) } }
    override suspend fun deleteAll() { data.clear() }
}

private class RecordingMaterializer : TransactionMaterializerService {
    val operations = mutableListOf<MaterializationOperation>()
    override suspend fun materialize(operation: MaterializationOperation): List<MaterializedTransaction> = emptyList<MaterializedTransaction>().also { operations += operation }
}

private class FakeProfiles : FinancialProfileService {
    private val profile = FinancialProfile(1, "Default", null, true, true, false, 1)
    override suspend fun resolveForAssignment(id: Long?) = profile.copy(id = id ?: 1)
    override suspend fun getById(id: Long) = profile
    override suspend fun create(command: CreateFinancialProfileCommand) = unsupported<FinancialProfile>()
    override suspend fun update(command: UpdateFinancialProfileCommand) = unsupported<FinancialProfile>()
    override suspend fun makeDefault(id: Long) = unsupported<FinancialProfile>()
    override suspend fun archive(id: Long) = unsupported<FinancialProfile>()
    override suspend fun unarchive(id: Long) = unsupported<FinancialProfile>()
    override suspend fun delete(id: Long) = Unit
    override suspend fun list(query: String?, archived: Boolean) = listOf(profile)
}

private class FakeContracts : ContractService {
    override suspend fun getById(id: Long): Contract? = null
    override suspend fun create(command: CreateContractCommand) = unsupported<Contract>()
    override suspend fun update(command: UpdateContractCommand) = unsupported<Contract>()
    override suspend fun list(accountId: Long?, archived: Boolean) = emptyList<Contract>()
}

private fun <T> unsupported(): T = throw UnsupportedOperationException()
