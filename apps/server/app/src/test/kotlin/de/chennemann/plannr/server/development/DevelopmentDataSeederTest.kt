package de.chennemann.plannr.server.development

import de.chennemann.plannr.server.accounts.support.AccountIdGenerator
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.accounts.usecases.CreateAccountUseCase
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.contracts.support.ContractIdGenerator
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.contracts.usecases.CreateContractUseCase
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketIdGenerator
import de.chennemann.plannr.server.pockets.usecases.CreatePocketUseCase
import de.chennemann.plannr.server.support.FakeCurrencyService
import de.chennemann.plannr.server.support.FakePartnerService
import de.chennemann.plannr.server.support.TestCurrencies
import de.chennemann.plannr.server.transactions.recurring.support.InMemoryRecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.support.RecurringTransactionIdGenerator
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

        assertEquals(1, fixture.accountRepository.findAll().size)
        assertEquals(4, fixture.pocketRepository.findAll().size)
        assertEquals(3, fixture.partnerService.list().size)
        assertEquals(3, fixture.contractRepository.findAll().size)
        assertEquals(3, fixture.recurringTransactionRepository.findAll().size)
    }

    private fun DevelopmentSeedResult.resources(): List<SeededResource> =
        accounts + pockets + partners + contracts + recurringTransactions

    private fun seederFixture(): SeederFixture {
        val accountRepository = InMemoryAccountRepository()
        val pocketRepository = InMemoryPocketRepository()
        val contractRepository = InMemoryContractRepository()
        val recurringTransactionRepository = InMemoryRecurringTransactionRepository()
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

        val createAccount = CreateAccountUseCase(
            accountRepository = accountRepository,
            currencyService = currencyService,
            accountIdGenerator = accountIdGenerator("acc"),
            timeProvider = timeProvider,
        )
        val createPocket = CreatePocketUseCase(
            pocketRepository = pocketRepository,
            accountRepository = accountRepository,
            pocketIdGenerator = pocketIdGenerator("poc"),
            timeProvider = timeProvider,
        )
        val createContract = CreateContractUseCase(
            contractRepository = contractRepository,
            pocketRepository = pocketRepository,
            partnerService = partnerService,
            contractIdGenerator = contractIdGenerator("con"),
            timeProvider = timeProvider,
        )
        val createRecurringTransaction = CreateRecurringTransactionUseCase(
            recurringTransactionRepository = recurringTransactionRepository,
            currencyService = currencyService,
            contextResolver = RecurringTransactionContextResolver(contractRepository, pocketRepository, partnerService),
            recurringTransactionIdGenerator = recurringTransactionIdGenerator("rtx"),
            timeProvider = timeProvider,
            normalization = RecurringTransactionNormalization(),
        )

        return SeederFixture(
            seeder = DevelopmentDataSeeder(
                accountRepository = accountRepository,
                pocketRepository = pocketRepository,
                contractRepository = contractRepository,
                recurringTransactionRepository = recurringTransactionRepository,
                createAccount = createAccount,
                createPocket = createPocket,
                partnerService = partnerService,
                createContract = createContract,
                createRecurringTransaction = createRecurringTransaction,
            ),
            accountRepository = accountRepository,
            pocketRepository = pocketRepository,
            partnerService = partnerService,
            contractRepository = contractRepository,
            recurringTransactionRepository = recurringTransactionRepository,
        )
    }

    private fun accountIdGenerator(prefix: String): AccountIdGenerator {
        var counter = 0
        return AccountIdGenerator { "${prefix}_${++counter}" }
    }

    private fun pocketIdGenerator(prefix: String): PocketIdGenerator {
        var counter = 0
        return PocketIdGenerator { "${prefix}_${++counter}" }
    }

    private fun idGenerator(prefix: String): () -> String {
        var counter = 0
        return { "${prefix}_${++counter}" }
    }

    private fun contractIdGenerator(prefix: String): ContractIdGenerator {
        var counter = 0
        return ContractIdGenerator { "${prefix}_${++counter}" }
    }

    private fun recurringTransactionIdGenerator(prefix: String): RecurringTransactionIdGenerator {
        var counter = 0
        return RecurringTransactionIdGenerator { "${prefix}_${++counter}" }
    }

    private data class SeederFixture(
        val seeder: DevelopmentDataSeeder,
        val accountRepository: InMemoryAccountRepository,
        val pocketRepository: InMemoryPocketRepository,
        val partnerService: FakePartnerService,
        val contractRepository: InMemoryContractRepository,
        val recurringTransactionRepository: InMemoryRecurringTransactionRepository,
    )
}
