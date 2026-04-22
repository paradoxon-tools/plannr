package de.chennemann.plannr.server.contracts.persistence

import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.currencies.service.CurrencyService
import de.chennemann.plannr.server.partners.service.CreatePartnerCommand
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.support.ApiIntegrationTest
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
    @Autowired lateinit var currencyService: CurrencyService
    private lateinit var defaultPartnerId: String

    @BeforeEach
    fun setUp() {
        runBlocking {
            cleanDatabase("contracts", "partners", "pockets", "accounts", "currencies")
            currencyService.ensureExists("EUR")
            accountRepository.save(AccountFixtures.account())
            accountRepository.save(AccountFixtures.account(id = "acc_456", name = "Savings"))
            pocketRepository.save(PocketFixtures.pocket())
            pocketRepository.save(PocketFixtures.pocket(id = "poc_456", accountId = "acc_456", name = "Rent"))
            defaultPartnerId = partnerService.create(CreatePartnerCommand(name = "ACME Corp", notes = "Preferred partner")).id
            partnerService.create(CreatePartnerCommand(name = "Telecom GmbH", notes = null))
        }
    }

    @Test
    fun `saves and finds contract by id and pocket id`() = runBlocking {
        val contract = ContractFixtures.contract(partnerId = defaultPartnerId)

        contractRepository.save(contract)

        assertEquals(contract, contractRepository.findById(ContractFixtures.DEFAULT_ID))
        assertEquals(contract, contractRepository.findByPocketId(ContractFixtures.DEFAULT_POCKET_ID))
        assertNull(contractRepository.findById("con_missing"))
    }

    @Test
    fun `updates and finds contract by id`() = runBlocking {
        contractRepository.save(ContractFixtures.contract(partnerId = defaultPartnerId))
        val updated = ContractFixtures.contract(
            accountId = "acc_456",
            pocketId = "poc_456",
            partnerId = null,
            name = "Updated",
            endDate = null,
            notes = null,
            isArchived = true,
        )

        contractRepository.update(updated)

        assertEquals(updated, contractRepository.findById(ContractFixtures.DEFAULT_ID))
    }

    @Test
    fun `finds all contracts ordered by created at and id and supports filters`() = runBlocking {
        contractRepository.save(ContractFixtures.contract(id = "con_2", accountId = "acc_456", pocketId = "poc_456", partnerId = null, createdAt = 2, name = "Second"))
        contractRepository.save(ContractFixtures.contract(id = "con_1", partnerId = defaultPartnerId, createdAt = 1, name = "First", isArchived = true))

        val defaultList = contractRepository.findAll()
        val archivedList = contractRepository.findAll(archived = true)
        val accountList = contractRepository.findAll(accountId = "acc_456")

        assertEquals(listOf("con_2"), defaultList.map { it.id })
        assertEquals(listOf("con_1"), archivedList.map { it.id })
        assertEquals(listOf("con_2"), accountList.map { it.id })
    }
}
