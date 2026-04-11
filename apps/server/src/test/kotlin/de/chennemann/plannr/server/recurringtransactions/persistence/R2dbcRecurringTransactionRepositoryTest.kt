package de.chennemann.plannr.server.recurringtransactions.persistence

import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.currencies.domain.CurrencyRepository
import de.chennemann.plannr.server.currencies.support.CurrencyFixtures
import de.chennemann.plannr.server.partners.domain.PartnerRepository
import de.chennemann.plannr.server.partners.support.PartnerFixtures
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransactionRepository
import de.chennemann.plannr.server.recurringtransactions.support.RecurringTransactionFixtures
import de.chennemann.plannr.server.support.ApiIntegrationTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals

class R2dbcRecurringTransactionRepositoryTest : ApiIntegrationTest() {
    @Autowired lateinit var recurringTransactionRepository: RecurringTransactionRepository
    @Autowired lateinit var currencyRepository: CurrencyRepository
    @Autowired lateinit var accountRepository: AccountRepository
    @Autowired lateinit var pocketRepository: PocketRepository
    @Autowired lateinit var partnerRepository: PartnerRepository
    @Autowired lateinit var contractRepository: ContractRepository

    @BeforeEach
    fun setUp() {
        runBlocking {
            cleanDatabase("recurring_transactions", "contracts", "partners", "pockets", "accounts", "currencies")
            currencyRepository.save(CurrencyFixtures.currency())
            accountRepository.save(AccountFixtures.account())
            pocketRepository.save(PocketFixtures.pocket())
            pocketRepository.save(PocketFixtures.pocket(id = "poc_456", accountId = "acc_123", name = "Income"))
            partnerRepository.save(PartnerFixtures.partner())
            contractRepository.save(ContractFixtures.contract(partnerId = PartnerFixtures.DEFAULT_ID))
        }
    }

    @Test
    fun `saves finds and filters`() = runBlocking {
        recurringTransactionRepository.save(RecurringTransactionFixtures.recurringTransaction())
        recurringTransactionRepository.save(RecurringTransactionFixtures.recurringTransaction(id = "rtx_2", contractId = null, accountId = "acc_123", sourcePocketId = null, destinationPocketId = "poc_456", partnerId = null, transactionType = "INCOME", isArchived = true))

        assertEquals(RecurringTransactionFixtures.DEFAULT_ID, recurringTransactionRepository.findById(RecurringTransactionFixtures.DEFAULT_ID)?.id)
        assertEquals(listOf(RecurringTransactionFixtures.DEFAULT_ID), recurringTransactionRepository.findByContractId(ContractFixtures.DEFAULT_ID).map { it.id })
        assertEquals(listOf(RecurringTransactionFixtures.DEFAULT_ID), recurringTransactionRepository.findAll(accountId = "acc_123", contractId = ContractFixtures.DEFAULT_ID).map { it.id })
        assertEquals(listOf("rtx_2"), recurringTransactionRepository.findAll(archived = true).map { it.id })
    }
}
