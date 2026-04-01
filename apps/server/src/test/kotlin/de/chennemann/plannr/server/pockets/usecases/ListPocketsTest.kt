package de.chennemann.plannr.server.pockets.usecases

import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ListPocketsTest {
    @Test
    fun `returns empty list when there are no pockets`() = runTest {
        val listPockets = ListPocketsUseCase(InMemoryPocketRepository())

        val result = listPockets()

        assertEquals(emptyList(), result)
    }

    @Test
    fun `filters by account and archived flag`() = runTest {
        val repository = InMemoryPocketRepository()
        val first = PocketFixtures.pocket(id = "poc_1", accountId = "acc_1", createdAt = 1)
        val second = PocketFixtures.pocket(id = "poc_2", accountId = "acc_1", isArchived = true, createdAt = 2)
        val third = PocketFixtures.pocket(id = "poc_3", accountId = "acc_2", createdAt = 3)
        repository.save(first)
        repository.save(second)
        repository.save(third)
        val listPockets = ListPocketsUseCase(repository)

        val result = listPockets(accountId = "acc_1", archived = false)

        assertEquals(listOf(first), result)
    }
}
