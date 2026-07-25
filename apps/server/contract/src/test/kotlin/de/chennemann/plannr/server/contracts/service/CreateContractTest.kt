package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.contracts.api.dto.ContractType
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreateContractTest {
    @Test
    fun `creates independent accumulating contract and one pocket per account`() = runTest {
        val pockets = FakePocketService()
        val repository = InMemoryContractRepository()
        val service = contractService(repository, pockets)

        val created = service.create(
            ContractFixtures.createContractCommand(financialProfileId = null, accountIds = setOf(1L, 2L)),
        )

        assertEquals(ContractFixtures.DEFAULT_FINANCIAL_PROFILE_ID, created.financialProfileId)
        assertEquals(ContractType.ACCUMULATING, created.type)
        assertEquals(listOf(1L, 2L), pockets.contractCreateCommands.map { it.accountId })
        assertEquals(listOf(created.id, created.id), pockets.contractCreateCommands.map { it.contractId })
    }

    @Test
    fun `creates non accumulating contract without pockets`() = runTest {
        val pockets = FakePocketService()
        val service = contractService(InMemoryContractRepository(), pockets)

        val created = service.create(
            ContractFixtures.createContractCommand(type = ContractType.NON_ACCUMULATING),
        )

        assertEquals(ContractType.NON_ACCUMULATING, created.type)
        assertEquals(emptyList(), pockets.contractCreateCommands)
    }

    @Test
    fun `rejects accumulating contract without accounts`() = runTest {
        val service = contractService(InMemoryContractRepository(), FakePocketService())
        assertFailsWith<ValidationException> {
            service.create(ContractFixtures.createContractCommand(accountIds = emptySet()))
        }
    }

    @Test
    fun `fails before creating pockets when partner does not exist`() = runTest {
        val pockets = FakePocketService()
        val service = contractService(InMemoryContractRepository(), pockets, partners = emptyList())
        assertFailsWith<NotFoundException> { service.create(ContractFixtures.createContractCommand()) }
        assertEquals(0, pockets.contractCreateCommands.size)
    }

    private fun contractService(
        repository: InMemoryContractRepository,
        pockets: FakePocketService,
        partners: List<de.chennemann.plannr.server.partners.api.dto.Partner> = listOf(ContractTestPartners.partner()),
    ) = ContractServiceImpl(
        contractRepository = repository,
        financialProfileService = FakeFinancialProfileService(),
        partnerService = FakePartnerService(partners),
        pocketService = pockets,
        timeProvider = { ContractFixtures.DEFAULT_CREATED_AT },
        transactionTemplateService = FakeTransactionTemplateService(),
    )
}
