package de.chennemann.plannr.server.savinggoals.support

import de.chennemann.plannr.server.savinggoals.domain.SavingGoalRepository
import de.chennemann.plannr.server.savinggoals.persistence.SavingGoalModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class InMemorySavingGoalRepository : SavingGoalRepository {
    private val goals = linkedMapOf<Long, SavingGoalModel>()

    override suspend fun <S : SavingGoalModel> save(entity: S): S {
        val persisted = entity.copy(id = entity.id ?: ((goals.keys.maxOrNull() ?: 0L) + 1L))
        goals[requireNotNull(persisted.id)] = persisted
        @Suppress("UNCHECKED_CAST")
        return persisted as S
    }

    override fun findAllByAccountIdAndArchived(accountId: Long?, archived: Boolean): Flow<SavingGoalModel> =
        goals.values
            .filter { it.isArchived == archived }
            .sortedWith(compareBy<SavingGoalModel> { it.createdAt }.thenBy { requireNotNull(it.id) })
            .asFlow()

    override suspend fun findById(id: Long) = goals[id]
    override suspend fun existsById(id: Long) = goals.containsKey(id)
    override fun findAll(): Flow<SavingGoalModel> = goals.values.asFlow()
    override fun findAllById(ids: Iterable<Long>): Flow<SavingGoalModel> = ids.mapNotNull(goals::get).asFlow()
    override fun findAllById(ids: Flow<Long>): Flow<SavingGoalModel> = flow {
        ids.collect { id -> goals[id]?.let { emit(it) } }
    }
    override fun <S : SavingGoalModel> saveAll(entities: Iterable<S>): Flow<S> = flow {
        entities.forEach { emit(save(it)) }
    }
    override fun <S : SavingGoalModel> saveAll(entityStream: Flow<S>): Flow<S> = flow {
        entityStream.collect { emit(save(it)) }
    }
    override suspend fun count() = goals.size.toLong()
    override suspend fun deleteById(id: Long) { goals.remove(id) }
    override suspend fun delete(entity: SavingGoalModel) { entity.id?.let(goals::remove) }
    override suspend fun deleteAllById(ids: Iterable<Long>) { ids.forEach(goals::remove) }
    override suspend fun deleteAll(entities: Iterable<SavingGoalModel>) { entities.mapNotNull { it.id }.forEach(goals::remove) }
    override suspend fun <S : SavingGoalModel> deleteAll(entityStream: Flow<S>) { entityStream.collect { delete(it) } }
    override suspend fun deleteAll() { goals.clear() }
}
