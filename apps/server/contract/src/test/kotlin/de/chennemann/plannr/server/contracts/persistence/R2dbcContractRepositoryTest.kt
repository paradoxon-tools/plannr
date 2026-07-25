package de.chennemann.plannr.server.contracts.persistence

import de.chennemann.plannr.server.contracts.domain.ContractRepository
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

@Tag("integration")
class R2dbcContractRepositoryTest : ApiIntegrationTest() {
    @Autowired lateinit var contractRepository: ContractRepository

    @BeforeEach
    fun clean() = runBlocking { cleanDatabase("contracts", "pockets", "accounts") }

    @Test
    fun `saves contract with independent identity`() = runBlocking {
        val saved = contractRepository.save(ContractFixtures.contractModel(id = null, partnerId = null))
        assertEquals(saved, contractRepository.findById(requireNotNull(saved.id)))
    }

    @Test
    fun `filters contracts through their account pockets`() = runBlocking {
        insertAccount(1L)
        insertAccount(2L)
        val first = contractRepository.save(ContractFixtures.contractModel(id = null, partnerId = null))
        val second = contractRepository.save(
            ContractFixtures.contractModel(id = null, partnerId = null).copy(isArchived = true, createdAt = 2L),
        )
        insertPocket(1L, 1L, requireNotNull(first.id))
        insertPocket(2L, 2L, requireNotNull(second.id))

        assertEquals(listOf(first.id), contractRepository.findAllByAccountIdAndArchived(null, false).toList().map { it.id })
        assertEquals(listOf(second.id), contractRepository.findAllByAccountIdAndArchived(null, true).toList().map { it.id })
        assertEquals(listOf(first.id), contractRepository.findAllByAccountIdAndArchived(1L, false).toList().map { it.id })
    }

    private suspend fun insertAccount(id: Long) {
        databaseClient.sql(
            """
            INSERT INTO accounts (id, name, institution, currency_code, weekend_handling, is_archived, created_at)
            VALUES (:id, :name, 'Demo Bank', 'EUR', 'next_business_day', FALSE, 1)
            """,
        ).bind("id", id).bind("name", "Account $id").fetch().rowsUpdated().awaitSingle()
    }

    private suspend fun insertPocket(id: Long, accountId: Long, contractId: Long) {
        databaseClient.sql(
            """
            INSERT INTO pockets (id, account_id, contract_id, name, description, color, is_default, is_archived, created_at)
            VALUES (:id, :accountId, :contractId, NULL, NULL, NULL, FALSE, FALSE, 1)
            """,
        ).bind("id", id).bind("accountId", accountId).bind("contractId", contractId)
            .fetch().rowsUpdated().awaitSingle()
    }
}
