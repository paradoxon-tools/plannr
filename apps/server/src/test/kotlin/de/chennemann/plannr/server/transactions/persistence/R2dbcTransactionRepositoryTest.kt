package de.chennemann.plannr.server.transactions.persistence

import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.currencies.domain.CurrencyRepository
import de.chennemann.plannr.server.currencies.support.CurrencyFixtures
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.support.ApiIntegrationTest
import de.chennemann.plannr.server.transactions.domain.TransactionRecord
import de.chennemann.plannr.server.transactions.domain.TransactionRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals

class R2dbcTransactionRepositoryTest : ApiIntegrationTest() {
    @Autowired lateinit var transactionRepository: TransactionRepository
    @Autowired lateinit var currencyRepository: CurrencyRepository
    @Autowired lateinit var accountRepository: AccountRepository
    @Autowired lateinit var pocketRepository: PocketRepository

    @BeforeEach
    fun setUp() {
        runBlocking {
            cleanDatabase("transactions", "pocket_transaction_feed", "account_transaction_feed", "pocket_query", "account_query", "pockets", "accounts", "currencies")
            currencyRepository.save(CurrencyFixtures.currency())
            accountRepository.save(AccountFixtures.account())
            pocketRepository.save(PocketFixtures.pocket())
            pocketRepository.save(PocketFixtures.pocket(id = "poc_456", name = "Savings"))
        }
    }

    @Test
    fun `round trips canonical enums through persistence`() = runBlocking {
        val saved = transactionRepository.save(transaction())

        val found = transactionRepository.findById(saved.id)

        assertEquals("EXPENSE", found?.type)
        assertEquals("CLEARED", found?.status)
        assertEquals("EUR", found?.currencyCode)
    }

    @Test
    fun `visible queries exclude archived and hidden originals but include modifications`() = runBlocking {
        val original = transactionRepository.save(transaction(id = "txn_original", transactionDate = "2026-04-10"))
        val modification = transactionRepository.save(
            transaction(
                id = "txn_modification",
                transactionDate = "2026-04-10",
                amount = 120,
                parentTransactionId = original.id,
                recurringTransactionId = "rtx_123",
            ),
        )
        transactionRepository.update(
            transaction(
                id = original.id,
                transactionDate = original.transactionDate,
                amount = original.amount,
                parentTransactionId = original.parentTransactionId,
                recurringTransactionId = original.recurringTransactionId,
                modifiedById = modification.id,
            ),
        )
        transactionRepository.save(transaction(id = "txn_archived", transactionDate = "2026-04-11", isArchived = true))

        assertEquals(listOf(modification.id), transactionRepository.findVisibleByAccountId("acc_123").map { it.id })
        assertEquals(listOf(modification.id), transactionRepository.findVisibleByPocketId("poc_123").map { it.id })
    }

    private fun transaction(
        id: String = "txn_123",
        transactionDate: String = "2026-04-10",
        amount: Long = 100,
        parentTransactionId: String? = null,
        recurringTransactionId: String? = null,
        modifiedById: String? = null,
        isArchived: Boolean = false,
    ): TransactionRecord = TransactionRecord(
        id = id,
        accountId = "acc_123",
        type = "EXPENSE",
        status = "CLEARED",
        transactionDate = transactionDate,
        amount = amount,
        currencyCode = "EUR",
        exchangeRate = null,
        destinationAmount = null,
        description = "desc",
        partnerId = null,
        sourcePocketId = "poc_123",
        destinationPocketId = null,
        parentTransactionId = parentTransactionId,
        recurringTransactionId = recurringTransactionId,
        modifiedById = modifiedById,
        isArchived = isArchived,
        createdAt = 1L,
    )
}
