package de.chennemann.plannr.server.transactions.recurring.persistence

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.persistence.toModel
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.currencies.service.CurrencyService
import de.chennemann.plannr.server.partners.service.CreatePartnerCommand
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.domain.Pocket
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
    @Autowired lateinit var currencyService: CurrencyService
    @Autowired lateinit var accountRepository: AccountRepository
    @Autowired lateinit var pocketRepository: PocketRepository
    @Autowired lateinit var partnerService: PartnerService
    @Autowired lateinit var contractRepository: ContractRepository
    private lateinit var defaultPartnerId: String

    @BeforeEach
    fun setUp() {
        runBlocking {
            cleanDatabase("recurring_transactions", "contracts", "partners", "pockets", "accounts", "currencies")
            currencyService.ensureExists("EUR")
            accountRepository.save(AccountFixtures.account().toPersistenceModel())
            pocketRepository.save(PocketFixtures.pocket().toPersistenceModel())
            pocketRepository.save(PocketFixtures.pocket(id = "poc_456", accountId = "acc_123", name = "Income").toPersistenceModel())
            defaultPartnerId = partnerService.create(CreatePartnerCommand(name = "ACME Corp", notes = "Preferred partner")).id
            contractRepository.save(ContractFixtures.contract(partnerId = defaultPartnerId).toModel())
        }
    }

    @Test
    fun `saves finds and filters`() = runBlocking {
        recurringTransactionRepository.save(
            RecurringTransactionFixtures.recurringTransaction(
                partnerId = defaultPartnerId,
                daysOfWeek = listOf("WEDNESDAY", "MONDAY", "MONDAY"),
                weeksOfMonth = listOf(2, -1, 2),
                daysOfMonth = listOf(10, -1, 10),
                monthsOfYear = listOf(6, 1, 6),
            ).toModel(),
        )
        recurringTransactionRepository.save(RecurringTransactionFixtures.recurringTransaction(id = "rtx_2", contractId = null, accountId = "acc_123", sourcePocketId = null, destinationPocketId = "poc_456", partnerId = null, transactionType = "INCOME", isArchived = true).toModel())

        val found = recurringTransactionRepository.findById(RecurringTransactionFixtures.DEFAULT_ID)
        assertEquals(RecurringTransactionFixtures.DEFAULT_ID, found?.id)
        assertEquals(listOf("MONDAY", "WEDNESDAY"), found?.daysOfWeek)
        assertEquals(listOf(-1, 2), found?.weeksOfMonth)
        assertEquals(listOf(-1, 10), found?.daysOfMonth)
        assertEquals(listOf(1, 6), found?.monthsOfYear)
        assertEquals(listOf(RecurringTransactionFixtures.DEFAULT_ID), recurringTransactionRepository.findByContractId(ContractFixtures.DEFAULT_ID).map { it.id })
        assertEquals(listOf(RecurringTransactionFixtures.DEFAULT_ID), recurringTransactionRepository.findAll(accountId = "acc_123", contractId = ContractFixtures.DEFAULT_ID).map { it.id })
        assertEquals(listOf("rtx_2"), recurringTransactionRepository.findAll(archived = true).map { it.id })
    }
}

private fun Account.toPersistenceModel(): de.chennemann.plannr.server.accounts.persistence.AccountModel =
    de.chennemann.plannr.server.accounts.persistence.AccountModel(
        id = id,
        name = name,
        institution = institution,
        currencyCode = currencyCode,
        weekendHandling = weekendHandling,
        isArchived = isArchived,
        createdAt = createdAt,
    )

private fun Pocket.toPersistenceModel(): de.chennemann.plannr.server.pockets.persistence.PocketModel =
    de.chennemann.plannr.server.pockets.persistence.PocketModel(
        id = id,
        accountId = accountId,
        name = name,
        description = description,
        color = color,
        isDefault = isDefault,
        isArchived = isArchived,
        createdAt = createdAt,
    )
