package de.chennemann.plannr.server.pockets.persistence

import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.support.ApiIntegrationTest
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
            cleanDatabase("pockets", "accounts", "currencies")
            insertCurrency()
            insertAccount("acc_123", "Main")
            insertAccount("acc_456", "Savings")
        }
    }

    @Test
    fun `saves and finds pocket by id`() = runBlocking {
        val pocket = PocketFixtures.pocket()

        pocketRepository.save(pocket)

        assertEquals(pocket, pocketRepository.findById(PocketFixtures.DEFAULT_ID))
        assertNull(pocketRepository.findById("poc_missing"))
    }

    @Test
    fun `updates and finds pocket by id`() = runBlocking {
        pocketRepository.save(PocketFixtures.pocket())
        val updated = PocketFixtures.pocket(
            accountId = "acc_456",
            name = "Updated",
            description = null,
            color = 42,
            isDefault = true,
            isArchived = true,
        )

        pocketRepository.update(updated)

        assertEquals(updated, pocketRepository.findById(PocketFixtures.DEFAULT_ID))
    }

    @Test
    fun `finds all pockets ordered by created at and id and supports filters`() = runBlocking {
        pocketRepository.save(PocketFixtures.pocket(id = "poc_2", accountId = "acc_123", createdAt = 2, name = "Second"))
        pocketRepository.save(PocketFixtures.pocket(id = "poc_1", accountId = "acc_123", createdAt = 1, name = "First", isArchived = true))
        pocketRepository.save(PocketFixtures.pocket(id = "poc_3", accountId = "acc_456", createdAt = 3, name = "Third"))

        val all = pocketRepository.findAll()
        val filtered = pocketRepository.findAll(accountId = "acc_123", archived = true)

        assertEquals(listOf("poc_1", "poc_2", "poc_3"), all.map { it.id })
        assertEquals(listOf("poc_1"), filtered.map { it.id })
    }

    private suspend fun insertCurrency() {
        databaseClient.sql(
            """
            INSERT INTO currencies (code, name, symbol, decimal_places, symbol_position)
            VALUES ('EUR', 'Euro', 'EUR', 2, 'before')
            """.trimIndent(),
        ).fetch().rowsUpdated().awaitSingle()
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
