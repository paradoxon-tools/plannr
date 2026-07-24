package de.chennemann.plannr.server.financialprofiles.service

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.financialprofiles.api.dto.CreateFinancialProfileCommand
import de.chennemann.plannr.server.financialprofiles.domain.FinancialProfileRepository
import de.chennemann.plannr.server.financialprofiles.domain.FinancialProfileUsageRepository
import de.chennemann.plannr.server.financialprofiles.persistence.FinancialProfileModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FinancialProfileServiceImplTest {
    @Test
    fun `creates normalized person profile`() = runTest {
        val service = service()

        val created = service.create(
            CreateFinancialProfileCommand(
                name = "  Alice  ",
                description = "Child",
                kind = " person ",
            ),
        )

        assertEquals("Alice", created.name)
        assertEquals("PERSON", created.kind)
        assertFalse(created.isDefault)
        assertFalse(created.isFallback)
        assertFalse(created.isArchived)
    }

    @Test
    fun `rejects duplicate names ignoring case and whitespace`() = runTest {
        val service = service()
        service.create(CreateFinancialProfileCommand("Alice", null, "PERSON"))

        assertFailsWith<ConflictException> {
            service.create(CreateFinancialProfileCommand(" alice ", null, "GROUP"))
        }
    }

    @Test
    fun `rejects invalid profile values`() = runTest {
        val service = service()

        assertFailsWith<ValidationException> {
            service.create(CreateFinancialProfileCommand(" ", null, "PERSON"))
        }
        assertFailsWith<ValidationException> {
            service.create(CreateFinancialProfileCommand("Alice", null, "FAMILY"))
        }
    }

    @Test
    fun `resolves null assignment to default and explicit active profile`() = runTest {
        val service = service()
        val alice = service.create(CreateFinancialProfileCommand("Alice", null, "PERSON"))

        assertEquals(DEFAULT_ID, service.resolveForAssignment(null).id)
        assertEquals(alice.id, service.resolveForAssignment(alice.id).id)
    }

    @Test
    fun `deleting selected default restores fallback as default`() = runTest {
        val usage = InMemoryFinancialProfileUsageRepository()
        val service = service(usage)
        val household = service.create(CreateFinancialProfileCommand("Household", null, "GROUP"))

        val selected = service.makeDefault(household.id)

        assertTrue(selected.isDefault)
        assertFalse(service.getById(DEFAULT_ID)!!.isDefault)
        assertEquals(household.id, service.resolveForAssignment(null).id)
        assertFailsWith<ConflictException> { service.archive(household.id) }

        service.delete(household.id)

        assertEquals(null, service.getById(household.id))
        assertTrue(service.getById(DEFAULT_ID)!!.isDefault)
        assertEquals(DEFAULT_ID, usage.reassignments.single().fallbackProfileId)
    }

    @Test
    fun `archived profile remains readable but cannot be assigned`() = runTest {
        val service = service()
        val alice = service.create(CreateFinancialProfileCommand("Alice", null, "PERSON"))

        val archived = service.archive(alice.id)

        assertTrue(archived.isArchived)
        assertEquals(alice.id, service.getById(alice.id)?.id)
        assertFailsWith<ValidationException> { service.resolveForAssignment(alice.id) }
        assertFalse(service.unarchive(alice.id).isArchived)
    }

    @Test
    fun `deleting profile reassigns references to fallback`() = runTest {
        val usage = InMemoryFinancialProfileUsageRepository()
        val service = service(usage)
        val alice = service.create(CreateFinancialProfileCommand("Alice", null, "PERSON"))

        service.delete(alice.id)

        assertEquals(null, service.getById(alice.id))
        assertEquals(
            ProfileReassignment(
                sourceProfileId = alice.id,
                fallbackProfileId = DEFAULT_ID,
                fallbackProfileName = "Unassigned",
                fallbackProfileKind = "GROUP",
            ),
            usage.reassignments.single(),
        )
    }

    @Test
    fun `fallback profile cannot be renamed archived or deleted`() = runTest {
        val service = service()

        assertFailsWith<ConflictException> {
            service.update(
                de.chennemann.plannr.server.financialprofiles.api.dto.UpdateFinancialProfileCommand(
                    id = DEFAULT_ID,
                    name = "Household",
                    description = null,
                    kind = "GROUP",
                ),
            )
        }
        assertFailsWith<ConflictException> { service.archive(DEFAULT_ID) }
        assertFailsWith<ConflictException> { service.delete(DEFAULT_ID) }
    }

    private fun service(
        usage: InMemoryFinancialProfileUsageRepository = InMemoryFinancialProfileUsageRepository(),
    ): FinancialProfileServiceImpl =
        FinancialProfileServiceImpl(
            financialProfileRepository = InMemoryFinancialProfileRepository(),
            financialProfileUsageRepository = usage,
            timeProvider = { CREATED_AT },
        )

    private companion object {
        const val DEFAULT_ID = 1L
        const val CREATED_AT = 1_721_000_000_000L
    }
}

private class InMemoryFinancialProfileUsageRepository : FinancialProfileUsageRepository {
    val reassignments = mutableListOf<ProfileReassignment>()

    override suspend fun reassignReferences(
        sourceProfileId: Long,
        fallbackProfileId: Long,
        fallbackProfileName: String,
        fallbackProfileKind: String,
    ) {
        reassignments += ProfileReassignment(
            sourceProfileId,
            fallbackProfileId,
            fallbackProfileName,
            fallbackProfileKind,
        )
    }
}

private data class ProfileReassignment(
    val sourceProfileId: Long,
    val fallbackProfileId: Long,
    val fallbackProfileName: String,
    val fallbackProfileKind: String,
)

private class InMemoryFinancialProfileRepository : FinancialProfileRepository {
    private val profiles = linkedMapOf(
        1L to FinancialProfileModel(
            id = 1L,
            name = "Unassigned",
            description = null,
            kind = "GROUP",
            isDefault = true,
            isFallback = true,
            isArchived = false,
            createdAt = 0L,
        ),
    )

    override suspend fun <S : FinancialProfileModel> save(entity: S): S {
        val persisted = entity.copy(id = entity.id ?: ((profiles.keys.maxOrNull() ?: 0L) + 1L))
        profiles[requireNotNull(persisted.id)] = persisted
        @Suppress("UNCHECKED_CAST")
        return persisted as S
    }

    override suspend fun findByNormalizedName(name: String): FinancialProfileModel? =
        profiles.values.firstOrNull { it.name.trim().equals(name.trim(), ignoreCase = true) }

    override suspend fun findDefault(): FinancialProfileModel? =
        profiles.values.firstOrNull(FinancialProfileModel::isDefault)

    override suspend fun findFallback(): FinancialProfileModel? =
        profiles.values.firstOrNull(FinancialProfileModel::isFallback)

    override suspend fun clearDefault(): Int {
        val defaults = profiles.values.filter(FinancialProfileModel::isDefault)
        defaults.forEach { profiles[requireNotNull(it.id)] = it.copy(isDefault = false) }
        return defaults.size
    }

    override fun findAllByQueryAndArchived(query: String?, archived: Boolean): Flow<FinancialProfileModel> =
        profiles.values
            .filter { it.isArchived == archived && (query == null || it.name.contains(query, ignoreCase = true)) }
            .sortedWith(compareBy<FinancialProfileModel> { it.createdAt }.thenBy { it.id })
            .asFlow()

    override suspend fun findById(id: Long): FinancialProfileModel? = profiles[id]
    override suspend fun existsById(id: Long): Boolean = profiles.containsKey(id)
    override fun findAll(): Flow<FinancialProfileModel> = profiles.values.asFlow()
    override fun findAllById(ids: Iterable<Long>): Flow<FinancialProfileModel> = ids.mapNotNull(profiles::get).asFlow()
    override fun findAllById(ids: Flow<Long>): Flow<FinancialProfileModel> = flow {
        ids.collect { id -> profiles[id]?.let { emit(it) } }
    }
    override fun <S : FinancialProfileModel> saveAll(entities: Iterable<S>): Flow<S> = flow {
        entities.forEach { emit(save(it)) }
    }
    override fun <S : FinancialProfileModel> saveAll(entityStream: Flow<S>): Flow<S> = flow {
        entityStream.collect { emit(save(it)) }
    }
    override suspend fun count(): Long = profiles.size.toLong()
    override suspend fun deleteById(id: Long) {
        profiles.remove(id)
    }
    override suspend fun delete(entity: FinancialProfileModel) {
        entity.id?.let(profiles::remove)
    }
    override suspend fun deleteAllById(ids: Iterable<Long>) {
        ids.forEach(profiles::remove)
    }
    override suspend fun deleteAll(entities: Iterable<FinancialProfileModel>) {
        entities.mapNotNull { it.id }.forEach(profiles::remove)
    }
    override suspend fun <S : FinancialProfileModel> deleteAll(entityStream: Flow<S>) {
        entityStream.collect { delete(it) }
    }
    override suspend fun deleteAll() {
        profiles.clear()
    }
}
