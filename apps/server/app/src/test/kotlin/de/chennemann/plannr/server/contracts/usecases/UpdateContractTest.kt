package de.chennemann.plannr.server.contracts.usecases

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.support.FakePartnerService
import de.chennemann.plannr.server.support.TestPartners
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateContractTest {
    @Test
    fun `updates existing contract`() = runTest {
        val contractRepository = InMemoryContractRepository()
        val pocketRepository = InMemoryPocketRepository()
        pocketRepository.save(PocketFixtures.pocket())
        pocketRepository.save(PocketFixtures.pocket(id = "poc_456", accountId = "acc_456", name = "Rent"))
        contractRepository.save(ContractFixtures.contract())
        val updateContract = UpdateContractUseCase(
            contractRepository = contractRepository,
            pocketRepository = pocketRepository,
            partnerService = FakePartnerService(
                listOf(
                    TestPartners.partner(),
                    TestPartners.partner(id = "par_456", name = "Telecom GmbH"),
                ),
            ),
        )

        val updated = updateContract(
            ContractFixtures.updateContractCommand(
                pocketId = "poc_456",
                partnerId = "par_456",
                name = "Updated Contract",
                startDate = "2024-02-01",
                endDate = null,
                notes = null,
            ),
        )

        assertEquals("acc_456", updated.accountId)
        assertEquals("poc_456", updated.pocketId)
        assertEquals("par_456", updated.partnerId)
        assertEquals("Updated Contract", updated.name)
        assertEquals(null, updated.endDate)
        assertEquals(null, updated.notes)
    }

    @Test
    fun `fails when contract does not exist`() = runTest {
        val updateContract = UpdateContractUseCase(
            contractRepository = InMemoryContractRepository(),
            pocketRepository = InMemoryPocketRepository(),
            partnerService = FakePartnerService(emptyList()),
        )

        assertFailsWith<NotFoundException> {
            updateContract(ContractFixtures.updateContractCommand())
        }
    }

    @Test
    fun `fails when updated pocket already has another contract`() = runTest {
        val contractRepository = InMemoryContractRepository()
        val pocketRepository = InMemoryPocketRepository()
        pocketRepository.save(PocketFixtures.pocket())
        pocketRepository.save(PocketFixtures.pocket(id = "poc_456", accountId = "acc_456", name = "Rent"))
        contractRepository.save(ContractFixtures.contract())
        contractRepository.save(ContractFixtures.contract(id = "con_456", accountId = "acc_456", pocketId = "poc_456"))
        val updateContract = UpdateContractUseCase(
            contractRepository = contractRepository,
            pocketRepository = pocketRepository,
            partnerService = FakePartnerService(emptyList()),
        )

        assertFailsWith<ConflictException> {
            updateContract(ContractFixtures.updateContractCommand(pocketId = "poc_456", partnerId = null))
        }
    }
}
