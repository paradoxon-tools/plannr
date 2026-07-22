package de.chennemann.plannr.server.contracts.persistence

import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.domain.upsert
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.support.ApiIntegrationTest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Tag("integration")
class R2dbcContractRepositoryTest : ApiIntegrationTest() {
    @Autowired lateinit var contractRepository: ContractRepository

    @BeforeEach
    fun clean() = runBlocking {
        cleanDatabase("contracts", "pockets", "accounts")
    }

    @Test
    fun `saves contract metadata keyed by pocket id`() = runBlocking {
        insertAccount(1L)
        insertPocket(1L, 1L, "Internet", false, 1L)

        val contract = ContractFixtures.contractModel(partnerId = null)
        contractRepository.upsert(contract)

        assertEquals(contract, contractRepository.findById(1L))
    }

    @Test
    fun `finds joined contracts by account and archive state`() = runBlocking {
        insertAccount(1L)
        insertAccount(2L)
        insertPocket(1L, 1L, "Internet", false, 1L)
        insertPocket(2L, 2L, "Rent", true, 2L)
        contractRepository.upsert(ContractFixtures.contractModel(pocketId = 1L, partnerId = null, signingDate = "2024-01-01"))
        contractRepository.upsert(ContractFixtures.contractModel(pocketId = 2L, partnerId = null, signingDate = "2024-02-01"))

        val active = contractRepository.findAllWithPocketsByAccountIdAndArchived(null, false).toList()
        val archived = contractRepository.findAllWithPocketsByAccountIdAndArchived(null, true).toList()
        val accountScoped = contractRepository.findAllWithPocketsByAccountIdAndArchived(1L, false).toList()

        assertEquals(listOf(1L), active.map { it.id })
        assertEquals(listOf(2L), archived.map { it.id })
        assertEquals(listOf(1L), accountScoped.map { it.id })
        assertEquals("2024-01-01", active.single().signingDate)
    }

    @Test
    fun `does not expose a contract pocket without contract metadata`() = runBlocking {
        insertAccount(1L)
        insertPocket(1L, 1L, "Internet", false, 1L)

        assertEquals(emptyList(), contractRepository.findAllWithPocketsByAccountIdAndArchived(null, false).toList())
        assertNull(contractRepository.findWithPocketByPocketId(1L))
    }

    private suspend fun insertAccount(id: Long) {
        databaseClient.sql(
            """
            INSERT INTO accounts (id, name, institution, currency_code, weekend_handling, is_archived, created_at)
            VALUES (:id, :name, 'Demo Bank', 'EUR', 'next_business_day', FALSE, 1)
            """,
        )
            .bind("id", id)
            .bind("name", "Account $id")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    private suspend fun insertPocket(id: Long, accountId: Long, name: String, archived: Boolean, createdAt: Long) {
        databaseClient.sql(
            """
            INSERT INTO pockets (id, account_id, name, description, color, is_default, is_contract_pocket, is_archived, created_at)
            VALUES (:id, :accountId, :name, NULL, 123456, FALSE, TRUE, :archived, :createdAt)
            """,
        )
            .bind("id", id)
            .bind("accountId", accountId)
            .bind("name", name)
            .bind("archived", archived)
            .bind("createdAt", createdAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
