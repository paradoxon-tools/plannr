package de.chennemann.plannr.server.query.projection

import de.chennemann.plannr.server.accounts.events.AccountCreated
import de.chennemann.plannr.server.accounts.events.AccountUpdated
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.pockets.events.PocketCreated
import de.chennemann.plannr.server.pockets.events.PocketUpdated
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.query.accounts.domain.AccountQuery
import de.chennemann.plannr.server.query.accounts.domain.AccountQueryRepository
import de.chennemann.plannr.server.query.pockets.domain.PocketQuery
import de.chennemann.plannr.server.query.pockets.domain.PocketQueryRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SummaryQueryProjectorsTest {
    @Test
    fun `account summary projector creates account query with zero balance`() = runTest {
        val repository = InMemoryAccountQueryRepository()
        val projector = AccountSummaryProjector(repository)
        val account = AccountFixtures.account()

        projector.handle(AccountCreated(account))

        assertEquals(
            AccountQuery(
                accountId = account.id,
                name = account.name,
                institution = account.institution,
                currencyCode = account.currencyCode,
                weekendHandling = account.weekendHandling,
                isArchived = account.isArchived,
                createdAt = account.createdAt,
                currentBalance = 0,
            ),
            repository.findById(account.id),
        )
    }

    @Test
    fun `account summary update projector stores updated metadata and archive state`() = runTest {
        val repository = InMemoryAccountQueryRepository()
        val projector = AccountSummaryUpdateProjector(repository)
        val before = AccountFixtures.account()
        val after = AccountFixtures.account(
            name = "Updated account",
            institution = "Updated bank",
            weekendHandling = "same_day",
            isArchived = true,
        )

        projector.handle(AccountUpdated(before, after))

        assertEquals("Updated account", repository.findById(after.id)?.name)
        assertEquals("Updated bank", repository.findById(after.id)?.institution)
        assertEquals("same_day", repository.findById(after.id)?.weekendHandling)
        assertEquals(true, repository.findById(after.id)?.isArchived)
        assertEquals(0L, repository.findById(after.id)?.currentBalance)
    }

    @Test
    fun `pocket summary projector creates pocket query with zero balance`() = runTest {
        val repository = InMemoryPocketQueryRepository()
        val projector = PocketSummaryProjector(repository)
        val pocket = PocketFixtures.pocket()

        projector.handle(PocketCreated(pocket))

        assertEquals(
            PocketQuery(
                pocketId = pocket.id,
                accountId = pocket.accountId,
                name = pocket.name,
                description = pocket.description,
                color = pocket.color,
                isDefault = pocket.isDefault,
                isArchived = pocket.isArchived,
                createdAt = pocket.createdAt,
                currentBalance = 0,
            ),
            repository.findById(pocket.id),
        )
    }

    @Test
    fun `pocket summary update projector stores updated metadata and archive state`() = runTest {
        val repository = InMemoryPocketQueryRepository()
        val projector = PocketSummaryUpdateProjector(repository)
        val before = PocketFixtures.pocket()
        val after = PocketFixtures.pocket(
            name = "Updated pocket",
            description = "Updated description",
            color = 999,
            isDefault = true,
            isArchived = true,
        )

        projector.handle(PocketUpdated(before, after))

        assertEquals("Updated pocket", repository.findById(after.id)?.name)
        assertEquals("Updated description", repository.findById(after.id)?.description)
        assertEquals(999, repository.findById(after.id)?.color)
        assertEquals(true, repository.findById(after.id)?.isDefault)
        assertEquals(true, repository.findById(after.id)?.isArchived)
        assertEquals(0L, repository.findById(after.id)?.currentBalance)
    }

    private class InMemoryAccountQueryRepository : AccountQueryRepository {
        private val values = linkedMapOf<String, AccountQuery>()

        override suspend fun saveOrUpdate(accountQuery: AccountQuery): AccountQuery {
            values[accountQuery.accountId] = accountQuery
            return accountQuery
        }

        override suspend fun findById(accountId: String): AccountQuery? = values[accountId]
    }

    private class InMemoryPocketQueryRepository : PocketQueryRepository {
        private val values = linkedMapOf<String, PocketQuery>()

        override suspend fun saveOrUpdate(pocketQuery: PocketQuery): PocketQuery {
            values[pocketQuery.pocketId] = pocketQuery
            return pocketQuery
        }

        override suspend fun findById(pocketId: String): PocketQuery? = values[pocketId]
    }
}
