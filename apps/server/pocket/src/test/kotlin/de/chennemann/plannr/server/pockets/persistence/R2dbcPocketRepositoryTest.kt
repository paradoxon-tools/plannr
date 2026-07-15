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
            insertAccount(1L, "Main")
            insertAccount(2L, "Savings")
        }
    }

    @Test
    fun `saves and finds pocket by id`() = runBlocking {
        val pocket = PocketFixtures.pocket()

        val saved = pocketRepository.save(pocket.toModel().copy(id = null))

        assertEquals(pocket.copy(id = saved.toDomain().id), pocketRepository.findById(saved.toDomain().id)?.toDomain())
        assertNull(pocketRepository.findById(999L))
    }

    @Test
    fun `updates and finds pocket by id`() = runBlocking {
        val original = PocketFixtures.pocket()
        insertPocket(original.toModel())
        val updated = PocketFixtures.pocket(
            accountId = 2L,
            name = "Updated",
            description = null,
            color = 42,
            isDefault = true,
            isContractPocket = original.isContractPocket,
            isArchived = true,
        )

        pocketRepository.save(updated.toModel())

        assertEquals(updated, pocketRepository.findById(PocketFixtures.DEFAULT_ID)?.toDomain())
    }

    @Test
    fun `finds all pockets ordered by created at and id and supports filters`() = runBlocking {
        insertPocket(PocketModel(id = 2L, accountId = 1L, name = "Second", description = null, color = 0, isDefault = false, isContractPocket = false, isArchived = false, createdAt = 2))
        insertPocket(PocketModel(id = 1L, accountId = 1L, name = "First", description = null, color = 0, isDefault = false, isContractPocket = false, isArchived = true, createdAt = 1))
        insertPocket(PocketModel(id = 3L, accountId = 2L, name = "Third", description = null, color = 0, isDefault = false, isContractPocket = false, isArchived = false, createdAt = 3))

        val all = pocketRepository.findAllByAccountIdAndArchived(accountId = null, archived = null).toList()
        val filtered = pocketRepository.findAllByAccountIdAndArchived(accountId = 1L, archived = true).toList()

        assertEquals(listOf(1L, 2L, 3L), all.map { it.id })
        assertEquals(listOf(1L), filtered.map { it.id })
    }

    @Test
    fun `finds default pocket for account`() = runBlocking {
        insertPocket(PocketFixtures.pocket(id = 1L, accountId = 1L, isDefault = false).toModel())
        insertPocket(PocketFixtures.pocket(id = 2L, accountId = 1L, name = "Default", isDefault = true).toModel())
        insertPocket(PocketFixtures.pocket(id = 3L, accountId = 2L, name = "Other default", isDefault = true).toModel())

        val defaultPocket = pocketRepository.findDefaultByAccountId(1L)?.toDomain()

        assertEquals(2L, defaultPocket?.id)
        assertEquals("Default", defaultPocket?.name)
        assertNull(pocketRepository.findDefaultByAccountId(999L))
    }

    private suspend fun insertAccount(id: Long, name: String) {
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

    private suspend fun insertPocket(pocket: PocketModel) {
        val spec = databaseClient.sql(
            """
            INSERT INTO pockets (id, account_id, name, description, color, is_default, is_contract_pocket, is_archived, created_at)
            VALUES (:id, :accountId, :name, :description, :color, :isDefault, :isContractPocket, :isArchived, :createdAt)
            """.trimIndent(),
        )
            .bind("id", requireNotNull(pocket.id))
            .bind("accountId", pocket.accountId)
            .bind("name", pocket.name)
            .bind("color", pocket.color)
            .bind("isDefault", pocket.isDefault)
            .bind("isContractPocket", pocket.isContractPocket)
            .bind("isArchived", pocket.isArchived)
            .bind("createdAt", pocket.createdAt)
        val boundSpec = pocket.description?.let { spec.bind("description", it) }
            ?: spec.bindNull("description", String::class.java)
        boundSpec.fetch().rowsUpdated().awaitSingle()
    }
}
