package de.chennemann.plannr.server.transactions.recurring.persistence

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.persistence.toDomain
import de.chennemann.plannr.server.accounts.persistence.toModel
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.partners.api.dto.CreatePartnerCommand
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.support.RecurringTransactionFixtures
import de.chennemann.plannr.server.support.ApiIntegrationTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals

class R2dbcRecurringTransactionRepositoryTest : ApiIntegrationTest() {
    @Autowired lateinit var recurringTransactionRepository: RecurringTransactionRepository
    @Autowired lateinit var accountRepository: AccountRepository
    @Autowired lateinit var pocketRepository: PocketRepository
    @Autowired lateinit var partnerService: PartnerService
    @Autowired lateinit var contractRepository: ContractRepository
    private lateinit var defaultPartnerId: String
    private var accountId: Long = 0L

    @BeforeEach
    fun setUp() {
        runBlocking {
            cleanDatabase("recurring_transactions", "contracts", "partners", "pockets", "accounts")
            accountId = accountRepository.insert(AccountFixtures.account()).id
            pocketRepository.insert(PocketFixtures.pocket(accountId = accountId))
            pocketRepository.insert(PocketFixtures.pocket(id = "poc_456", accountId = accountId, name = "Income"))
            defaultPartnerId = partnerService.create(CreatePartnerCommand(name = "ACME Corp", notes = "Preferred partner")).id
            contractRepository.insert(ContractFixtures.contract(accountId = accountId, partnerId = defaultPartnerId))
        }
    }

    @Test
    fun `saves finds and filters`() = runBlocking {
        recurringTransactionRepository.save(
            RecurringTransactionFixtures.recurringTransaction(
                partnerId = defaultPartnerId,
                accountId = accountId,
                daysOfWeek = listOf("WEDNESDAY", "MONDAY", "MONDAY"),
                weeksOfMonth = listOf(2, -1, 2),
                daysOfMonth = listOf(10, -1, 10),
                monthsOfYear = listOf(6, 1, 6),
            ).toModel(),
        )
        recurringTransactionRepository.save(RecurringTransactionFixtures.recurringTransaction(id = "rtx_2", contractId = null, accountId = accountId, sourcePocketId = null, destinationPocketId = "poc_456", partnerId = null, transactionType = "INCOME", isArchived = true).toModel())

        val found = recurringTransactionRepository.findById(RecurringTransactionFixtures.DEFAULT_ID)
        assertEquals(RecurringTransactionFixtures.DEFAULT_ID, found?.id)
        assertEquals(listOf("MONDAY", "WEDNESDAY"), found?.daysOfWeek)
        assertEquals(listOf(-1, 2), found?.weeksOfMonth)
        assertEquals(listOf(-1, 10), found?.daysOfMonth)
        assertEquals(listOf(1, 6), found?.monthsOfYear)
        assertEquals(listOf(RecurringTransactionFixtures.DEFAULT_ID), recurringTransactionRepository.findByContractId(ContractFixtures.DEFAULT_ID).map { it.id })
        assertEquals(listOf(RecurringTransactionFixtures.DEFAULT_ID), recurringTransactionRepository.findAll(accountId = accountId, contractId = ContractFixtures.DEFAULT_ID).map { it.id })
        assertEquals(listOf("rtx_2"), recurringTransactionRepository.findAll(archived = true).map { it.id })
    }
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

private suspend fun ContractRepository.insert(contract: Contract) {
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
