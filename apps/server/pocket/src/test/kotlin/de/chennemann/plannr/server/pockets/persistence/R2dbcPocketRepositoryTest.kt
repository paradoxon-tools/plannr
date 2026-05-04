package de.chennemann.plannr.server.pockets.persistence

import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.persistence.toDomain
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.support.ApiIntegrationTest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertNull

class R2dbcPocketRepositoryTest : ApiIntegrationTest() {
    @Autowired
    lateinit var pocketRepository: PocketRepository

    @BeforeEach
    fun setUp() {
        runBlocking {
            cleanDatabase("pockets", "accounts")
            insertAccount("acc_123", "Main")
            insertAccount("acc_456", "Savings")
        }
    }

    @Test
    fun `saves and finds pocket by id`() = runBlocking {
        val pocket = PocketFixtures.pocket()

        pocketRepository.insert(
            id = pocket.id,
            accountId = pocket.accountId,
            name = pocket.name,
            description = pocket.description,
            color = pocket.color,
            isDefault = pocket.isDefault,
            isArchived = pocket.isArchived,
            createdAt = pocket.createdAt,
        )

        assertEquals(pocket, pocketRepository.findById(PocketFixtures.DEFAULT_ID)?.toDomain())
        assertNull(pocketRepository.findById("poc_missing"))
    }

    @Test
    fun `updates and finds pocket by id`() = runBlocking {
        val original = PocketFixtures.pocket()
        pocketRepository.insert(
            id = original.id,
            accountId = original.accountId,
            name = original.name,
            description = original.description,
            color = original.color,
            isDefault = original.isDefault,
            isArchived = original.isArchived,
            createdAt = original.createdAt,
        )
        val updated = PocketFixtures.pocket(
            accountId = "acc_456",
            name = "Updated",
            description = null,
            color = 42,
            isDefault = true,
            isArchived = true,
        )

        pocketRepository.update(
            id = updated.id,
            accountId = updated.accountId,
            name = updated.name,
            description = updated.description,
            color = updated.color,
            isDefault = updated.isDefault,
            isArchived = updated.isArchived,
        )

        assertEquals(updated, pocketRepository.findById(PocketFixtures.DEFAULT_ID)?.toDomain())
    }

    @Test
    fun `finds all pockets ordered by created at and id and supports filters`() = runBlocking {
        pocketRepository.insert(id = "poc_2", accountId = "acc_123", name = "Second", description = null, color = 0, isDefault = false, isArchived = false, createdAt = 2)
        pocketRepository.insert(id = "poc_1", accountId = "acc_123", name = "First", description = null, color = 0, isDefault = false, isArchived = true, createdAt = 1)
        pocketRepository.insert(id = "poc_3", accountId = "acc_456", name = "Third", description = null, color = 0, isDefault = false, isArchived = false, createdAt = 3)

        val all = pocketRepository.findAllByAccountIdAndArchived(accountId = null, archived = null).toList()
        val filtered = pocketRepository.findAllByAccountIdAndArchived(accountId = "acc_123", archived = true).toList()

        assertEquals(listOf("poc_1", "poc_2", "poc_3"), all.map { it.id })
        assertEquals(listOf("poc_1"), filtered.map { it.id })
    }

    private suspend fun insertAccount(id: String, name: String) {
        databaseClient.sql(
            """
            INSERT INTO accounts (id, name, institution, currency_code, weekend_handling, is_archived, created_at)
            VALUES (:id, :name, 'Bank', 'EUR', 'NO_SHIFT', FALSE, 1)
            """.trimIndent(),
        )
            .bind("id", id)
            .bind("name", name)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
