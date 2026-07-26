package de.chennemann.plannr.server.savinggoals.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.savinggoals.support.SavingGoalFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreateSavingGoalTest {
    @Test
    fun `creates goal and one dedicated pocket per account`() = runTest {
        val pockets = FakePocketService()
        val accounts = FakeAccountService(
            listOf(FakeAccountService.account(1L), FakeAccountService.account(2L)),
        )
        val service = savingGoalService(accounts = accounts, pockets = pockets)

        val created = service.create(
            SavingGoalFixtures.createCommand(
                currencyCode = " eur ",
                accountIds = setOf(2L, 1L),
                financialProfileId = null,
            ),
        )

        assertEquals("EUR", created.currencyCode)
        assertEquals(setOf(1L, 2L), created.accountIds)
        assertEquals(0L, created.currentAmount)
        assertEquals(false, created.isCompleted)
        assertEquals(listOf(1L, 2L), pockets.createCommands.map { it.accountId })
        assertEquals(listOf(created.id, created.id), pockets.createCommands.map { it.savingGoalId })
    }

    @Test
    fun `rejects invalid target data`() = runTest {
        val service = savingGoalService()

        assertFailsWith<ValidationException> {
            service.create(SavingGoalFixtures.createCommand(targetAmount = 0L))
        }
        assertFailsWith<ValidationException> {
            service.create(SavingGoalFixtures.createCommand(targetDate = "31-12-2027"))
        }
        assertFailsWith<ValidationException> {
            service.create(SavingGoalFixtures.createCommand(name = " "))
        }
    }

    @Test
    fun `requires at least one existing account in the same currency`() = runTest {
        assertFailsWith<ValidationException> {
            savingGoalService().create(SavingGoalFixtures.createCommand(accountIds = emptySet()))
        }
        assertFailsWith<NotFoundException> {
            savingGoalService(accounts = FakeAccountService(emptyList()))
                .create(SavingGoalFixtures.createCommand())
        }
        assertFailsWith<ValidationException> {
            savingGoalService(accounts = FakeAccountService(listOf(FakeAccountService.account(currencyCode = "USD"))))
                .create(SavingGoalFixtures.createCommand())
        }
    }
}
