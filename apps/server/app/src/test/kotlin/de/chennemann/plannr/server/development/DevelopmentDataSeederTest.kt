package de.chennemann.plannr.server.development

import de.chennemann.plannr.server.accounts.service.AccountService
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.contracts.usecases.CreateContractUseCase
import de.chennemann.plannr.server.support.FakeCurrencyService
import de.chennemann.plannr.server.support.FakeAccountService
import de.chennemann.plannr.server.support.FakePartnerService
import de.chennemann.plannr.server.support.FakePocketService
import de.chennemann.plannr.server.support.TestCurrencies
import de.chennemann.plannr.server.transactions.recurring.support.InMemoryRecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.usecases.CreateRecurringTransactionUseCase
import de.chennemann.plannr.server.transactions.recurring.usecases.RecurringTransactionContextResolver
import de.chennemann.plannr.server.transactions.recurring.usecases.RecurringTransactionNormalization
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DevelopmentDataSeederTest {
    @Test
    fun `default scenario can be seeded repeatedly without duplicates`() = runTest {
        val fixture = seederFixture()

        val first = fixture.seeder.seedDefaultScenario()
        val second = fixture.seeder.seedDefaultScenario()

        assertEquals(1, first.accounts.size)
        assertEquals(4, first.pockets.size)
        assertEquals(3, first.partners.size)
        assertEquals(3, first.contracts.size)
        assertEquals(3, first.recurringTransactions.size)
        assertTrue(first.resources().all { it.status == SeededResourceStatus.CREATED })
        assertTrue(second.resources().all { it.status == SeededResourceStatus.EXISTING })

        assertEquals(1, fixture.accountService.list().size)
        assertEquals(4, fixture.pocketService.list().size)
        assertEquals(3, fixture.partnerService.list().size)
        assertEquals(3, fixture.contractRepository.findAll().size)
        assertEquals(3, fixture.recurringTransactionRepository.findAll().size)
    }

    private fun DevelopmentSeedResult.resources(): List<SeededResource> =
        accounts + pockets + partners + contracts + recurringTransactions

    private fun seederFixture(): SeederFixture {
        val currencyService = FakeCurrencyService(
            initialCurrencies = emptyList(),
            templates = mapOf("EUR" to TestCurrencies.eur()),
        )
        val timeProvider = TimeProvider { 1L }
        val partnerService = FakePartnerService(
            initialPartners = emptyList(),
            idGenerator = idGenerator("par"),
            timeProvider = { timeProvider() },
        )
        val pocketService = FakePocketService(
            initialPockets = emptyList(),
            idGenerator = idGenerator("poc"),
            timeProvider = { timeProvider() },
        )
        val contractRepository = InMemoryContractRepository(
            accountIdResolver = { pocketId -> pocketService.findByIdNow(pocketId)?.accountId ?: error("Pocket not found: $pocketId") },
        )
        val recurringTransactionRepository = InMemoryRecurringTransactionRepository(
            contractIdResolver = { model -> contractRepository.peekByPocketId(model.sourcePocketId ?: model.destinationPocketId ?: "")?.id },
            accountIdResolver = { model ->
                val pocketId = model.sourcePocketId ?: model.destinationPocketId ?: error("Pocket id required")
                pocketService.findByIdNow(pocketId)?.accountId ?: error("Pocket not found: $pocketId")
            },
        )
        val accountService = FakeAccountService(
            initialAccounts = emptyList(),
            idGenerator = idGenerator("acc"),
            timeProvider = { timeProvider() },
        )
        val createContract = CreateContractUseCase(
            contractRepository = contractRepository,
            pocketService = pocketService,
            partnerService = partnerService,
            timeProvider = timeProvider,
        )
        val createRecurringTransaction = CreateRecurringTransactionUseCase(
            recurringTransactionRepository = recurringTransactionRepository,
            currencyService = currencyService,
            contextResolver = RecurringTransactionContextResolver(contractRepository, pocketService, partnerService),
            timeProvider = timeProvider,
            normalization = RecurringTransactionNormalization(),
        )

        return SeederFixture(
            seeder = DevelopmentDataSeeder(
                contractRepository = contractRepository,
                recurringTransactionRepository = recurringTransactionRepository,
                accountService = accountService,
                pocketService = pocketService,
                partnerService = partnerService,
                createContract = createContract,
                createRecurringTransaction = createRecurringTransaction,
            ),
            accountService = accountService,
            pocketService = pocketService,
            partnerService = partnerService,
            contractRepository = contractRepository,
            recurringTransactionRepository = recurringTransactionRepository,
        )
    }

    private fun idGenerator(prefix: String): () -> String {
        var counter = 0
        return { "${prefix}_${++counter}" }
    }

    private data class SeederFixture(
        val seeder: DevelopmentDataSeeder,
        val accountService: AccountService,
        val pocketService: FakePocketService,
        val partnerService: FakePartnerService,
        val contractRepository: InMemoryContractRepository,
        val recurringTransactionRepository: InMemoryRecurringTransactionRepository,
    )
}
