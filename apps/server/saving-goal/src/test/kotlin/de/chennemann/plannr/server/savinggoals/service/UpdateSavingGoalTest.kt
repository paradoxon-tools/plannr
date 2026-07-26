package de.chennemann.plannr.server.savinggoals.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.savinggoals.support.InMemorySavingGoalRepository
import de.chennemann.plannr.server.savinggoals.support.SavingGoalFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateSavingGoalTest {
    @Test
    fun `updates goal and synchronized pocket presentation`() = runTest {
        val repository = InMemorySavingGoalRepository()
        val pockets = FakePocketService()
        val service = savingGoalService(repository = repository, pockets = pockets)
        val created = service.create(SavingGoalFixtures.createCommand())

        val updated = service.update(SavingGoalFixtures.updateCommand(id = created.id))

        assertEquals("Updated emergency fund", updated.name)
        assertEquals(600_000L, updated.targetAmount)
        assertEquals("Updated emergency fund", pockets.listForSavingGoal(created.id).single().name)
        assertEquals(listOf(created.id), pockets.updateCommands.map { it.savingGoalId })
    }

    @Test
    fun `reports aggregated progress and completion`() = runTest {
        val pockets = FakePocketService()
        val feeds = FakeTransactionFeedService()
        val service = savingGoalService(pockets = pockets, feeds = feeds)
        val created = service.create(SavingGoalFixtures.createCommand(targetAmount = 100L))
        val pocketId = pockets.listForSavingGoal(created.id).single().id
        feeds.balances[pocketId] = 125L

        val result = service.getById(created.id)

        assertEquals(125L, result?.currentAmount)
        assertEquals(true, result?.isCompleted)
    }

    @Test
    fun `archives and unarchives goal pockets together`() = runTest {
        val pockets = FakePocketService()
        val service = savingGoalService(pockets = pockets)
        val created = service.create(SavingGoalFixtures.createCommand())

        assertEquals(true, service.archive(created.id).isArchived)
        assertEquals(true, pockets.listForSavingGoal(created.id).single().isArchived)
        assertEquals(false, service.unarchive(created.id).isArchived)
        assertEquals(false, pockets.listForSavingGoal(created.id).single().isArchived)
    }

    @Test
    fun `fails when goal does not exist`() = runTest {
        assertFailsWith<NotFoundException> {
            savingGoalService().update(SavingGoalFixtures.updateCommand())
        }
    }
}
