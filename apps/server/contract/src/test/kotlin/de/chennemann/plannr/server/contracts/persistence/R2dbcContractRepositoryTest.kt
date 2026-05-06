package de.chennemann.plannr.server.contracts.persistence

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.persistence.toDomain
import de.chennemann.plannr.server.accounts.persistence.toModel
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.persistence.toDomain
import de.chennemann.plannr.server.contracts.persistence.toModel
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.partners.api.dto.CreatePartnerCommand
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.support.ApiIntegrationTest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient
import kotlin.test.assertEquals
import kotlin.test.assertNull

class R2dbcContractRepositoryTest : ApiIntegrationTest() {
    @Autowired lateinit var contractRepository: ContractRepository
    @Autowired lateinit var partnerService: PartnerService
    @Autowired lateinit var accountRepository: AccountRepository
    @Autowired lateinit var testDatabaseClient: DatabaseClient
    private var defaultpartnerId: Long = 0L
    private var mainAccountId: Long = 0L
    private var savingsAccountId: Long = 0L

    @BeforeEach
    fun setUp() {
        runBlocking {
            cleanDatabase("contracts", "partners", "pockets", "accounts")
            mainAccountId = accountRepository.insert(Account(1L, "Main Account", "Demo Bank", "EUR", "NO_SHIFT", false, 1_710_000_000L)).id
            savingsAccountId = accountRepository.insert(Account(2L, "Savings", "Demo Bank", "EUR", "NO_SHIFT", false, 1_710_000_001L)).id
            insertPocket(1L, mainAccountId, "Bills", "Monthly fixed costs", 1_710_000_100L)
            insertPocket(2L, savingsAccountId, "Rent", "Monthly fixed costs", 1_710_000_101L)
            defaultpartnerId = partnerService.create(CreatePartnerCommand(name = "ACME Corp", notes = "Preferred partner")).id
            partnerService.create(CreatePartnerCommand(name = "Telecom GmbH", notes = null))
        }
    }

    @Test
    fun `saves and finds contract by id and pocket id`() = runBlocking {
        val contract = ContractFixtures.contract(accountId = mainAccountId, partnerId = defaultpartnerId)

        insertContract(contract.toModel())

        assertEquals(contract, contractRepository.findById(ContractFixtures.DEFAULT_ID)?.toDomain())
        assertEquals(contract, contractRepository.findByPocketId(ContractFixtures.DEFAULT_POCKET_ID)?.toDomain())
        assertNull(contractRepository.findById(999L))
    }

    @Test
    fun `updates and finds contract by id`() = runBlocking {
        val original = ContractFixtures.contract(accountId = mainAccountId, partnerId = defaultpartnerId)
        insertContract(original.toModel())
        val updated = ContractFixtures.contract(
            accountId = savingsAccountId,
            pocketId = 2L,
            partnerId = null,
            name = "Updated",
            endDate = null,
            notes = null,
            isArchived = true,
        )

        contractRepository.save(updated.toModel())

        assertEquals(updated, contractRepository.findById(ContractFixtures.DEFAULT_ID)?.toDomain())
    }

    @Test
    fun `finds all contracts ordered by created at and id and supports filters`() = runBlocking {
        insertContract(ContractFixtures.contract(id = 2L, accountId = savingsAccountId, pocketId = 2L, partnerId = null, createdAt = 2, name = "Second").toModel())
        insertContract(ContractFixtures.contract(id = 1L, accountId = mainAccountId, partnerId = defaultpartnerId, createdAt = 1, name = "First", isArchived = true).toModel())

        val defaultList = contractRepository.findAllByAccountIdAndArchived(accountId = null, archived = false).toList()
        val archivedList = contractRepository.findAllByAccountIdAndArchived(accountId = null, archived = true).toList()
        val accountList = contractRepository.findAllByAccountIdAndArchived(accountId = savingsAccountId, archived = false).toList()

        assertEquals(listOf(2L), defaultList.map { it.id })
        assertEquals(listOf(1L), archivedList.map { it.id })
        assertEquals(listOf(2L), accountList.map { it.id })
    }

    private suspend fun insertPocket(id: Long, accountId: Long, name: String, description: String?, createdAt: Long) {
        testDatabaseClient.sql(
            """
            INSERT INTO pockets (id, account_id, name, description, color, is_default, is_contract_pocket, is_archived, created_at)
            VALUES (:id, :accountId, :name, :description, 123456, FALSE, FALSE, FALSE, :createdAt)
            """.trimIndent(),
        )
            .bind("id", id)
            .bind("accountId", accountId)
            .bind("name", name)
            .bind("description", requireNotNull(description))
            .bind("createdAt", createdAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    private suspend fun insertContract(contract: ContractModel) {
        val spec = testDatabaseClient.sql(
            """
            INSERT INTO contracts (id, account_id, pocket_id, partner_id, name, start_date, end_date, notes, is_archived, created_at)
            VALUES (:id, :accountId, :pocketId, :partnerId, :name, :startDate, :endDate, :notes, :isArchived, :createdAt)
            """.trimIndent(),
        )
            .bind("id", requireNotNull(contract.id))
            .bind("accountId", contract.accountId)
            .bind("pocketId", contract.pocketId)
            .bind("name", contract.name)
            .bind("startDate", contract.startDate)
            .bind("isArchived", contract.isArchived)
            .bind("createdAt", contract.createdAt)

        val withPartner = contract.partnerId?.let { spec.bind("partnerId", it) }
            ?: spec.bindNull("partnerId", java.lang.Long::class.java)
        val withEndDate = contract.endDate?.let { withPartner.bind("endDate", it) }
            ?: withPartner.bindNull("endDate", String::class.java)
        val withNotes = contract.notes?.let { withEndDate.bind("notes", it) }
            ?: withEndDate.bindNull("notes", String::class.java)

        withNotes.fetch().rowsUpdated().awaitSingle()
    }
}

private suspend fun AccountRepository.insert(account: Account): Account = save(account.toModel().copy(id = null)).toDomain()
