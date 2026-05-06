package de.chennemann.plannr.server.contracts.persistence

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.persistence.toDomain
import de.chennemann.plannr.server.accounts.persistence.toModel
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.persistence.toDomain
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.partners.api.dto.CreatePartnerCommand
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.support.ApiIntegrationTest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertNull

class R2dbcContractRepositoryTest : ApiIntegrationTest() {
    @Autowired lateinit var contractRepository: ContractRepository
    @Autowired lateinit var pocketRepository: PocketRepository
    @Autowired lateinit var partnerService: PartnerService
    @Autowired lateinit var accountRepository: AccountRepository
    private lateinit var defaultPartnerId: String
    private var mainAccountId: Long = 0L
    private var savingsAccountId: Long = 0L

    @BeforeEach
    fun setUp() {
        runBlocking {
            cleanDatabase("contracts", "partners", "pockets", "accounts")
            mainAccountId = accountRepository.insert(Account(1L, "Main Account", "Demo Bank", "EUR", "NO_SHIFT", false, 1_710_000_000L)).id
            savingsAccountId = accountRepository.insert(Account(2L, "Savings", "Demo Bank", "EUR", "NO_SHIFT", false, 1_710_000_001L)).id
            pocketRepository.insert(Pocket("poc_123", mainAccountId, "Bills", "Monthly fixed costs", 123456, false, false, 1_710_000_100L))
            pocketRepository.insert(Pocket("poc_456", savingsAccountId, "Rent", "Monthly fixed costs", 123456, false, false, 1_710_000_101L))
            defaultPartnerId = partnerService.create(CreatePartnerCommand(name = "ACME Corp", notes = "Preferred partner")).id
            partnerService.create(CreatePartnerCommand(name = "Telecom GmbH", notes = null))
        }
    }

    @Test
    fun `saves and finds contract by id and pocket id`() = runBlocking {
        val contract = ContractFixtures.contract(accountId = mainAccountId, partnerId = defaultPartnerId)

        contractRepository.insert(
            id = contract.id,
            accountId = contract.accountId,
            pocketId = contract.pocketId,
            partnerId = contract.partnerId,
            name = contract.name,
            startDate = contract.startDate,
            endDate = contract.endDate,
            notes = contract.notes,
            isArchived = contract.isArchived,
            createdAt = contract.createdAt,
        )

        assertEquals(contract, contractRepository.findById(ContractFixtures.DEFAULT_ID)?.toDomain())
        assertEquals(contract, contractRepository.findByPocketId(ContractFixtures.DEFAULT_POCKET_ID)?.toDomain())
        assertNull(contractRepository.findById("con_missing"))
    }

    @Test
    fun `updates and finds contract by id`() = runBlocking {
        val original = ContractFixtures.contract(accountId = mainAccountId, partnerId = defaultPartnerId)
        contractRepository.insert(
            id = original.id,
            accountId = original.accountId,
            pocketId = original.pocketId,
            partnerId = original.partnerId,
            name = original.name,
            startDate = original.startDate,
            endDate = original.endDate,
            notes = original.notes,
            isArchived = original.isArchived,
            createdAt = original.createdAt,
        )
        val updated = ContractFixtures.contract(
            accountId = savingsAccountId,
            pocketId = "poc_456",
            partnerId = null,
            name = "Updated",
            endDate = null,
            notes = null,
            isArchived = true,
        )

        contractRepository.update(
            id = updated.id,
            accountId = updated.accountId,
            pocketId = updated.pocketId,
            partnerId = updated.partnerId,
            name = updated.name,
            startDate = updated.startDate,
            endDate = updated.endDate,
            notes = updated.notes,
            isArchived = updated.isArchived,
        )

        assertEquals(updated, contractRepository.findById(ContractFixtures.DEFAULT_ID)?.toDomain())
    }

    @Test
    fun `finds all contracts ordered by created at and id and supports filters`() = runBlocking {
        contractRepository.insert(ContractFixtures.contract(id = "con_2", accountId = savingsAccountId, pocketId = "poc_456", partnerId = null, createdAt = 2, name = "Second"))
        contractRepository.insert(ContractFixtures.contract(id = "con_1", accountId = mainAccountId, partnerId = defaultPartnerId, createdAt = 1, name = "First", isArchived = true))

        val defaultList = contractRepository.findAllByAccountIdAndArchived(accountId = null, archived = false).toList()
        val archivedList = contractRepository.findAllByAccountIdAndArchived(accountId = null, archived = true).toList()
        val accountList = contractRepository.findAllByAccountIdAndArchived(accountId = savingsAccountId, archived = false).toList()

        assertEquals(listOf("con_2"), defaultList.map { it.id })
        assertEquals(listOf("con_1"), archivedList.map { it.id })
        assertEquals(listOf("con_2"), accountList.map { it.id })
    }
}

private suspend fun ContractRepository.insert(contract: de.chennemann.plannr.server.contracts.domain.Contract) {
    insert(
        id = contract.id,
        accountId = contract.accountId,
        pocketId = contract.pocketId,
        partnerId = contract.partnerId,
        name = contract.name,
        startDate = contract.startDate,
        endDate = contract.endDate,
        notes = contract.notes,
        isArchived = contract.isArchived,
        createdAt = contract.createdAt,
    )
}

private suspend fun AccountRepository.insert(account: Account): Account = save(account.toModel().copy(id = null)).toDomain()

private suspend fun PocketRepository.insert(pocket: Pocket) {
    insert(
        id = pocket.id,
        accountId = pocket.accountId,
        name = pocket.name,
        description = pocket.description,
        color = pocket.color,
        isDefault = pocket.isDefault,
        isArchived = pocket.isArchived,
        createdAt = pocket.createdAt,
    )
}
