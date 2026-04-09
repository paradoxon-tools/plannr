package de.chennemann.plannr.server.query.projection

import de.chennemann.plannr.server.partners.support.InMemoryPartnerRepository
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.transactions.domain.TransactionRecord
import de.chennemann.plannr.server.transactions.domain.TransactionRepository
import de.chennemann.plannr.server.transactions.events.TransactionArchived
import de.chennemann.plannr.server.transactions.events.TransactionCreated
import de.chennemann.plannr.server.transactions.events.TransactionUnarchived
import de.chennemann.plannr.server.transactions.events.TransactionUpdated
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryMetadata
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.r2dbc.core.DatabaseClient

class TransactionQueryProjectorHandlersTest {
    @Test
    fun `transaction created projector rebuilds using created transaction`() = runTest {
        val projectionService = RecordingTransactionQueryProjectionService()
        val projector = TransactionCreatedQueryProjector(projectionService)
        val transaction = transaction()

        projector.handle(TransactionCreated(transaction))

        assertEquals(listOf(transaction), projectionService.createdTransactions)
    }

    @Test
    fun `transaction updated projector rebuilds using before and after transactions`() = runTest {
        val projectionService = RecordingTransactionQueryProjectionService()
        val projector = TransactionUpdatedQueryProjector(projectionService)
        val before = transaction(id = "txn_before", transactionDate = "2026-04-10")
        val after = transaction(id = before.id, transactionDate = "2026-04-11")

        projector.handle(TransactionUpdated(before, after))

        assertEquals(1, projectionService.updatedTransactions.size)
        assertEquals(before to after, projectionService.updatedTransactions.single())
    }

    @Test
    fun `transaction archived projector rebuilds using before and after transactions`() = runTest {
        val projectionService = RecordingTransactionQueryProjectionService()
        val projector = TransactionArchivedQueryProjector(projectionService)
        val before = transaction()
        val after = before.archive()

        projector.handle(TransactionArchived(before, after))

        assertEquals(1, projectionService.updatedTransactions.size)
        assertEquals(before to after, projectionService.updatedTransactions.single())
    }

    @Test
    fun `transaction unarchived projector rebuilds using before and after transactions`() = runTest {
        val projectionService = RecordingTransactionQueryProjectionService()
        val projector = TransactionUnarchivedQueryProjector(projectionService)
        val before = transaction(isArchived = true)
        val after = before.unarchive()

        projector.handle(TransactionUnarchived(before, after))

        assertEquals(1, projectionService.updatedTransactions.size)
        assertEquals(before to after, projectionService.updatedTransactions.single())
    }

    private class RecordingTransactionQueryProjectionService : TransactionQueryProjectionService(
        transactionRepository = InMemoryTransactionRepository(),
        pocketRepository = InMemoryPocketRepository(),
        partnerRepository = InMemoryPartnerRepository(),
        databaseClient = DatabaseClient.create(NoOpConnectionFactory),
    ) {
        val createdTransactions = mutableListOf<TransactionRecord>()
        val updatedTransactions = mutableListOf<Pair<TransactionRecord?, TransactionRecord?>>()

        override suspend fun rebuildFor(transaction: TransactionRecord) {
            createdTransactions += transaction
        }

        override suspend fun rebuildFor(before: TransactionRecord?, after: TransactionRecord?) {
            updatedTransactions += before to after
        }
    }

    private class InMemoryTransactionRepository : TransactionRepository {
        override suspend fun save(transaction: TransactionRecord): TransactionRecord = transaction

        override suspend fun update(transaction: TransactionRecord): TransactionRecord = transaction

        override suspend fun findById(id: String): TransactionRecord? = null

        override suspend fun findVisibleByAccountId(accountId: String): List<TransactionRecord> = emptyList()

        override suspend fun findVisibleByPocketId(pocketId: String): List<TransactionRecord> = emptyList()
    }

    private fun transaction(
        id: String = "txn_123",
        transactionDate: String = "2026-04-10",
        isArchived: Boolean = false,
    ): TransactionRecord = TransactionRecord(
        id = id,
        accountId = "acc_123",
        type = "expense",
        status = "booked",
        transactionDate = transactionDate,
        amount = 100,
        currencyCode = "EUR",
        exchangeRate = null,
        destinationAmount = null,
        description = "desc",
        partnerId = null,
        sourcePocketId = "poc_123",
        destinationPocketId = null,
        parentTransactionId = null,
        recurringTransactionId = null,
        modifiedById = null,
        isArchived = isArchived,
        createdAt = 1L,
    )

    private object NoOpConnectionFactory : ConnectionFactory {
        override fun create() = throw UnsupportedOperationException("Not used in unit tests")

        override fun getMetadata(): ConnectionFactoryMetadata = object : ConnectionFactoryMetadata {
            override fun getName(): String = "PostgreSQL"
        }
    }
}
