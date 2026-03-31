package de.chennemann.plannr.server.contracts.usecases

import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ListContractsTest {
    @Test
    fun `returns empty list when there are no contracts`() = runTest {
        val listContracts = ListContractsUseCase(InMemoryContractRepository())

        val result = listContracts()

        assertEquals(emptyList(), result)
    }

    @Test
    fun `filters by account and archived flag`() = runTest {
        val repository = InMemoryContractRepository()
        val first = ContractFixtures.contract(id = "con_1", accountId = "acc_1", pocketId = "poc_1", createdAt = 1)
        val second = ContractFixtures.contract(id = "con_2", accountId = "acc_1", pocketId = "poc_2", createdAt = 2, isArchived = true)
        val third = ContractFixtures.contract(id = "con_3", accountId = "acc_2", pocketId = "poc_3", createdAt = 3)
        repository.save(first)
        repository.save(second)
        repository.save(third)
        val listContracts = ListContractsUseCase(repository)

        val result = listContracts(accountId = "acc_1", archived = false)

        assertEquals(listOf(first), result)
    }
}
