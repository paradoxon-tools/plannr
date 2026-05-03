package de.chennemann.plannr.server.partners.support

import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.domain.PartnerRepository
import de.chennemann.plannr.server.partners.persistence.PartnerModel
import de.chennemann.plannr.server.partners.persistence.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class InMemoryPartnerRepository : PartnerRepository {
    private val partners = linkedMapOf<String, PartnerModel>()

    override suspend fun insert(id: String?, name: String, notes: String?, isArchived: Boolean, createdAt: Long): PartnerModel =
        save(PartnerModel(id, name, notes, isArchived, createdAt))

    override suspend fun update(id: String, name: String, notes: String?, isArchived: Boolean): PartnerModel =
        save(PartnerModel(id, name, notes, isArchived, partners[id]?.createdAt ?: 0L, persisted = true))

    override suspend fun <S : PartnerModel> save(entity: S): S {
        val persisted = entity.withIdIfMissing("par_${partners.size + 1}")
        partners[requireNotNull(persisted.id)] = persisted
        @Suppress("UNCHECKED_CAST")
        return persisted as S
    }

    override suspend fun findById(id: String): PartnerModel? = partners[id]

    override suspend fun existsById(id: String): Boolean = partners.containsKey(id)

    override fun findAll(): Flow<PartnerModel> = partners.values.asFlow()

    override fun findAllById(ids: Iterable<String>): Flow<PartnerModel> =
        ids.mapNotNull(partners::get).asFlow()

    override fun findAllById(ids: Flow<String>): Flow<PartnerModel> = flow {
        ids.collect { id -> partners[id]?.let { emit(it) } }
    }

    override fun <S : PartnerModel> saveAll(entities: Iterable<S>): Flow<S> = flow {
        entities.forEach { emit(save(it)) }
    }

    override fun <S : PartnerModel> saveAll(entityStream: Flow<S>): Flow<S> = flow {
        entityStream.collect { emit(save(it)) }
    }

    override fun findAllByQueryAndArchived(query: String?, archived: Boolean): Flow<PartnerModel> =
        partners.values
            .filter { it.isArchived == archived }
            .filter { query.isNullOrBlank() || it.name.lowercase().contains(query.trim().lowercase()) }
            .sortedWith(compareBy<PartnerModel> { it.createdAt }.thenBy { requireNotNull(it.id) })
            .asFlow()

    override suspend fun count(): Long = partners.size.toLong()

    override suspend fun deleteById(id: String) {
        partners.remove(id)
    }

    override suspend fun delete(entity: PartnerModel) {
        entity.id?.let(partners::remove)
    }

    override suspend fun deleteAllById(ids: Iterable<String>) {
        ids.forEach(partners::remove)
    }

    override suspend fun deleteAll(entities: Iterable<PartnerModel>) {
        entities.mapNotNull { it.id }.forEach(partners::remove)
    }

    override suspend fun <S : PartnerModel> deleteAll(entityStream: Flow<S>) {
        entityStream.collect { delete(it) }
    }

    override suspend fun deleteAll() {
        partners.clear()
    }

    suspend fun save(partner: Partner): Partner = save(partner.toModel()).toDomain()

    private fun PartnerModel.withIdIfMissing(id: String): PartnerModel = copy(id = this.id ?: id)

    private fun PartnerModel.toDomain(): Partner =
        Partner(
            id = requireNotNull(id),
            name = name,
            notes = notes,
            isArchived = isArchived,
            createdAt = createdAt,
        )
}
