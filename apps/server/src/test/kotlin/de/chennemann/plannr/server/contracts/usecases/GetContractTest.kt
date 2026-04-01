package de.chennemann.plannr.server.contracts.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetContractTest {
    @Test
    fun `returns contract by id`() = runTest {
        val repository = InMemoryContractRepository()
        val contract = ContractFixtures.contract()
        repository.save(contract)
        val getContract = GetContractUseCase(repository)

        val result = getContract(ContractFixtures.DEFAULT_ID)

        assertEquals(contract, result)
    }

    @Test
    fun `fails for unknown contract`() = runTest {
        val getContract = GetContractUseCase(InMemoryContractRepository())

        assertFailsWith<NotFoundException> {
            getContract(ContractFixtures.DEFAULT_ID)
        }
    }
}
