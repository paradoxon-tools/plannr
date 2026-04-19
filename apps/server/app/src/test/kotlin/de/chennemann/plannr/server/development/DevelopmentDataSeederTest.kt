package de.chennemann.plannr.server.development

import de.chennemann.plannr.server.accounts.support.AccountIdGenerator
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.accounts.usecases.CreateAccountUseCase
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.contracts.support.ContractIdGenerator
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.contracts.usecases.CreateContractUseCase
import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.support.InMemoryCurrencyRepository
import de.chennemann.plannr.server.currencies.support.InMemoryCurrencyTemplateCatalog
import de.chennemann.plannr.server.currencies.usecases.EnsureCurrencyExistsUseCase
import de.chennemann.plannr.server.partners.support.InMemoryPartnerRepository
import de.chennemann.plannr.server.partners.support.PartnerIdGenerator
import de.chennemann.plannr.server.partners.usecases.CreatePartnerUseCase
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketIdGenerator
import de.chennemann.plannr.server.pockets.usecases.CreatePocketUseCase
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
        assertEquals(3, fixture.partnerRepository.findAll().size)
        assertEquals(3, fixture.contractRepository.findAll().size)
        assertEquals(3, fixture.recurringTransactionRepository.findAll().size)
    }

    private fun DevelopmentSeedResult.resources(): List<SeededResource> =
        accounts + pockets + partners + contracts + recurringTransactions

    private fun seederFixture(): SeederFixture {
        val accountRepository = InMemoryAccountRepository()
        val pocketRepository = InMemoryPocketRepository()
        val partnerRepository = InMemoryPartnerRepository()
        val contractRepository = InMemoryContractRepository()
        val recurringTransactionRepository = InMemoryRecurringTransactionRepository()
        val currencyRepository = InMemoryCurrencyRepository()
        val currencyCatalog = InMemoryCurrencyTemplateCatalog(
            mapOf("EUR" to Currency("EUR", "Euro", "EUR", 2, "before")),
        )
        val ensureCurrencyExists = EnsureCurrencyExistsUseCase(currencyRepository, currencyCatalog)
        val timeProvider = TimeProvider { 1L }

        val createAccount = CreateAccountUseCase(
            accountRepository = accountRepository,
            ensureCurrencyExists = ensureCurrencyExists,
            accountIdGenerator = accountIdGenerator("acc"),
            timeProvider = timeProvider,
        )
        val createPocket = CreatePocketUseCase(
            pocketRepository = pocketRepository,
            accountRepository = accountRepository,
            pocketIdGenerator = pocketIdGenerator("poc"),
            timeProvider = timeProvider,
        )
        val createPartner = CreatePartnerUseCase(
            partnerRepository = partnerRepository,
            partnerIdGenerator = partnerIdGenerator("par"),
            timeProvider = timeProvider,
        )
        val createContract = CreateContractUseCase(
            contractRepository = contractRepository,
            pocketRepository = pocketRepository,
            partnerRepository = partnerRepository,
            contractIdGenerator = contractIdGenerator("con"),
            timeProvider = timeProvider,
        )
        val createRecurringTransaction = CreateRecurringTransactionUseCase(
            recurringTransactionRepository = recurringTransactionRepository,
            ensureCurrencyExists = ensureCurrencyExists,
            contextResolver = RecurringTransactionContextResolver(contractRepository, pocketRepository, partnerRepository),
            recurringTransactionIdGenerator = recurringTransactionIdGenerator("rtx"),
            timeProvider = timeProvider,
            normalization = RecurringTransactionNormalization(),
        )

        return SeederFixture(
            seeder = DevelopmentDataSeeder(
                accountRepository = accountRepository,
                pocketRepository = pocketRepository,
                partnerRepository = partnerRepository,
                contractRepository = contractRepository,
                recurringTransactionRepository = recurringTransactionRepository,
                createAccount = createAccount,
                createPocket = createPocket,
                createPartner = createPartner,
                createContract = createContract,
                createRecurringTransaction = createRecurringTransaction,
            ),
            accountRepository = accountRepository,
            pocketRepository = pocketRepository,
            partnerRepository = partnerRepository,
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

    private fun partnerIdGenerator(prefix: String): PartnerIdGenerator {
        var counter = 0
        return PartnerIdGenerator { "${prefix}_${++counter}" }
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
        val partnerRepository: InMemoryPartnerRepository,
        val contractRepository: InMemoryContractRepository,
        val recurringTransactionRepository: InMemoryRecurringTransactionRepository,
    )
}
