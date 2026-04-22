package de.chennemann.plannr.server.transactions.usecases

import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.support.FakeCurrencyService
import de.chennemann.plannr.server.support.FakePartnerService
import de.chennemann.plannr.server.support.FakePocketService
import de.chennemann.plannr.server.transactions.domain.TransactionRecord
import de.chennemann.plannr.server.transactions.support.InMemoryTransactionRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ModifyRecurringOccurrenceTest {
    @Test
    fun `modifying recurring occurrence hides original and creates visible child`() = runTest {
        val transactionRepository = InMemoryTransactionRepository().apply {
            save(recurringRoot())
        }
        val useCase = useCase(transactionRepository)

        val modified = useCase(
            ModifyRecurringOccurrence.Command(
                transactionId = "txn_root",
                type = "EXPENSE",
                status = "CLEARED",
                transactionDate = "2024-04-12",
                amount = 7000,
                currencyCode = "EUR",
                exchangeRate = null,
                destinationAmount = null,
                description = "Updated internet",
                partnerId = null,
                sourcePocketId = "poc_123",
                destinationPocketId = null,
            ),
        )

        assertEquals("txn_mod", modified.id)
        assertEquals("txn_root", modified.parentTransactionId)
        assertEquals("rtx_123", modified.recurringTransactionId)
        assertEquals("RECURRING_MODIFICATION", modified.transactionOrigin)
        assertEquals(listOf("txn_mod"), transactionRepository.findVisibleByAccountId("acc_123").map { it.id })
        assertEquals("txn_mod", transactionRepository.findById("txn_root")?.modifiedById)
    }

    @Test
    fun `rejects modifying non recurring materialized rows or repeated chains`() = runTest {
        val transactionRepository = InMemoryTransactionRepository().apply {
            save(recurringRoot(id = "txn_already_modified", modifiedById = "txn_child"))
            save(recurringRoot(id = "txn_manual", recurringTransactionId = null, transactionOrigin = "MANUAL"))
        }
        val useCase = useCase(transactionRepository)

        assertFailsWith<ValidationException> {
            useCase(command("txn_already_modified"))
        }
        assertFailsWith<ValidationException> {
            useCase(command("txn_manual"))
        }
    }

    private suspend fun useCase(transactionRepository: InMemoryTransactionRepository): ModifyRecurringOccurrenceUseCase {
        val accountRepository = InMemoryAccountRepository().apply { save(AccountFixtures.account()) }
        val pocketService = FakePocketService(listOf(PocketFixtures.pocket()))
        return ModifyRecurringOccurrenceUseCase(
            transactionRepository = transactionRepository,
            currencyService = FakeCurrencyService(),
            contextResolver = TransactionContextResolver(accountRepository, pocketService, FakePartnerService(emptyList())),
            transactionIdGenerator = { "txn_mod" },
        )
    }

    private fun command(id: String) = ModifyRecurringOccurrence.Command(
        transactionId = id,
        type = "EXPENSE",
        status = "CLEARED",
        transactionDate = "2024-04-12",
        amount = 7000,
        currencyCode = "EUR",
        exchangeRate = null,
        destinationAmount = null,
        description = "Updated internet",
        partnerId = null,
        sourcePocketId = "poc_123",
        destinationPocketId = null,
    )

    private fun recurringRoot(
        id: String = "txn_root",
        recurringTransactionId: String? = "rtx_123",
        modifiedById: String? = null,
        transactionOrigin: String = "RECURRING_MATERIALIZED",
    ): TransactionRecord = TransactionRecord(
        id = id,
        accountId = "acc_123",
        type = "EXPENSE",
        status = "PENDING",
        transactionDate = "2024-04-10",
        amount = 4999,
        currencyCode = "EUR",
        exchangeRate = null,
        destinationAmount = null,
        description = "Monthly internet",
        partnerId = null,
        pocketId = "poc_123",
        sourcePocketId = "poc_123",
        destinationPocketId = null,
        parentTransactionId = null,
        recurringTransactionId = recurringTransactionId,
        modifiedById = modifiedById,
        transactionOrigin = transactionOrigin,
        isArchived = false,
        createdAt = 1L,
    )
}
