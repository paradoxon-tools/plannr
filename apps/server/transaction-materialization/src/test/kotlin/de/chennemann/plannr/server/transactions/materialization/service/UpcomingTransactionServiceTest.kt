package de.chennemann.plannr.server.transactions.materialization.service

import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateCommand
import de.chennemann.plannr.server.transactions.templates.api.dto.UpdateTransactionTemplateCommand
import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand
import de.chennemann.plannr.server.pockets.service.CreatePocketForContractCommand
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.transactions.templates.domain.RecurrencePattern
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate
import de.chennemann.plannr.server.transactions.templates.service.TransactionTemplateService
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UpcomingTransactionServiceTest {
    @Test
    fun `cache contains the complete next weekly expansion`() = runTest {
        val template = weeklyTemplate(id = 1L)
        val calculator = UpcomingOccurrenceCalculator()
        val cache = InMemoryUpcomingTransactionCache(
            localDateProvider = { LocalDate.parse("2026-07-19") },
            upcomingOccurrenceCalculator = calculator,
        )

        val dates = cache.getOrRefresh(template)

        assertEquals(listOf("2026-07-20", "2026-07-22"), dates)
    }

    @Test
    fun `upcoming API calculates occurrences after the supplied date`() = runTest {
        val template = weeklyTemplate(id = 1L)
        val calculator = UpcomingOccurrenceCalculator()
        val localDateProvider = LocalDateProviderFixture("2026-07-19")
        val cache = InMemoryUpcomingTransactionCache(localDateProvider, calculator)
        val service = UpcomingTransactionServiceImpl(
            transactionTemplateService = StubTransactionTemplateService(listOf(template)),
            pocketService = UnusedPocketService,
            upcomingTransactionCache = cache,
            upcomingOccurrenceCalculator = calculator,
            localDateProvider = localDateProvider,
        )

        val firstPage = service.getForPocket(pocketId = 10L, after = null, count = 2)
        val secondPage = service.getForPocket(
            pocketId = 10L,
            after = LocalDate.parse(assertNotNull(firstPage.transactions.lastOrNull()).occurrenceDate),
            count = 2,
        )

        assertEquals(listOf("2026-07-20", "2026-07-22"), firstPage.transactions.map { it.occurrenceDate })
        assertTrue(firstPage.hasMore)
        assertEquals(listOf("2026-07-27", "2026-07-29"), secondPage.transactions.map { it.occurrenceDate })
        assertTrue(secondPage.hasMore)
    }

    @Test
    fun `upcoming API does not split transactions sharing the final occurrence date`() = runTest {
        val calculator = UpcomingOccurrenceCalculator()
        val localDateProvider = LocalDateProviderFixture("2026-07-19")
        val cache = InMemoryUpcomingTransactionCache(localDateProvider, calculator)
        val service = UpcomingTransactionServiceImpl(
            transactionTemplateService = StubTransactionTemplateService(
                listOf(weeklyTemplate(id = 1L), weeklyTemplate(id = 2L)),
            ),
            pocketService = UnusedPocketService,
            upcomingTransactionCache = cache,
            upcomingOccurrenceCalculator = calculator,
            localDateProvider = localDateProvider,
        )

        val page = service.getForPocket(pocketId = 10L, after = null, count = 1)

        assertEquals(listOf(1L, 2L), page.transactions.map { it.transactionTemplateId })
        assertEquals(listOf("2026-07-20", "2026-07-20"), page.transactions.map { it.occurrenceDate })
        assertTrue(page.hasMore)
    }

    private fun weeklyTemplate(id: Long) = TransactionTemplate(
        id = id,
        sourcePocketId = 10L,
        destinationPocketId = null,
        partnerId = null,
        title = "Weekly",
        description = null,
        amount = 100L,
        currencyCode = "EUR",
        transactionType = "EXPENSE",
        recurrencePattern = RecurrencePattern(
            firstOccurrenceDate = "2026-07-20",
            finalOccurrenceDate = null,
            recurrenceType = "WEEKLY",
            skipCount = 0,
            daysOfWeek = listOf("MONDAY", "WEDNESDAY"),
            weeksOfMonth = null,
            daysOfMonth = null,
            monthsOfYear = null,
        ),
        previousVersionId = null,
        isArchived = false,
        createdAt = 1L,
    )
}

private class LocalDateProviderFixture(
    date: String,
) : de.chennemann.plannr.server.common.time.LocalDateProvider {
    private val date = LocalDate.parse(date)

    override fun invoke(): LocalDate = date
}

private class StubTransactionTemplateService(
    private val templates: List<TransactionTemplate>,
) : TransactionTemplateService {
    override suspend fun list(archived: Boolean?): List<TransactionTemplate> =
        templates.filter { archived == null || it.isArchived == archived }

    override suspend fun getById(id: Long): TransactionTemplate? = templates.find { it.id == id }

    override suspend fun create(command: CreateTransactionTemplateCommand): TransactionTemplate =
        unsupported()

    override suspend fun createBatch(commands: List<CreateTransactionTemplateCommand>): List<TransactionTemplate> =
        unsupported()

    override suspend fun update(command: UpdateTransactionTemplateCommand): TransactionTemplate =
        unsupported()

    override suspend fun archive(id: Long): TransactionTemplate = unsupported()

    override suspend fun unarchive(id: Long): TransactionTemplate = unsupported()

    override suspend fun archiveForPocket(pocketId: Long) = unsupported<Unit>()

    override suspend fun unarchiveForPocket(pocketId: Long) = unsupported<Unit>()

    override suspend fun delete(id: Long) = unsupported<Unit>()

    private fun <T> unsupported(): T = error("Not used by this test")
}

private data object UnusedPocketService : PocketService {
    override suspend fun create(command: CreatePocketCommand): Pocket = unsupported()

    override suspend fun createForContract(command: CreatePocketForContractCommand): Pocket = unsupported()

    override suspend fun update(command: UpdatePocketCommand): Pocket = unsupported()

    override suspend fun archive(id: Long): Pocket = unsupported()

    override suspend fun unarchive(id: Long): Pocket = unsupported()

    override suspend fun archiveForAccount(accountId: Long) = unsupported<Unit>()

    override suspend fun unarchiveForAccount(accountId: Long) = unsupported<Unit>()

    override suspend fun delete(id: Long) = unsupported<Unit>()

    override suspend fun list(
        accountId: Long?,
        archived: Boolean?,
    ): List<Pocket> = unsupported()

    override suspend fun getById(id: Long): Pocket? = unsupported()

    private fun <T> unsupported(): T = error("Not used by this test")
}
