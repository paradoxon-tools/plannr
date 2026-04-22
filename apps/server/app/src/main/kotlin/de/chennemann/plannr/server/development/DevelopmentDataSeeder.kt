package de.chennemann.plannr.server.development

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.usecases.CreateAccount
import de.chennemann.plannr.server.common.domain.RecurrenceType
import de.chennemann.plannr.server.common.domain.TransactionType
import de.chennemann.plannr.server.common.domain.WeekendHandling
import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.usecases.CreateContract
import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.service.CreatePartnerCommand
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.service.CreatePocketCommand
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransaction
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.usecases.CreateRecurringTransaction
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DevelopmentDataSeeder(
    private val accountRepository: AccountRepository,
    private val contractRepository: ContractRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val createAccount: CreateAccount,
    private val pocketService: PocketService,
    private val partnerService: PartnerService,
    private val createContract: CreateContract,
    private val createRecurringTransaction: CreateRecurringTransaction,
) {
    @Transactional
    suspend fun seedDefaultScenario(): DevelopmentSeedResult {
        val collector = DevelopmentSeedCollector()
        val account = ensureAccount(
            name = "Demo Checking",
            institution = "Plannr Demo Bank",
            currencyCode = "EUR",
            weekendHandling = WeekendHandling.MOVE_BEFORE.name,
            collector = collector,
        )
        val context = DevelopmentSeedContext(
            account = account,
            defaultPocket = ensurePocket(
                accountId = account.id,
                name = "Daily Spending",
                description = "General day-to-day spending",
                color = 0x2563EB,
                isDefault = true,
                collector = collector,
            ),
        )

        addRentContract(context, collector)
        addInternetContract(context, collector)
        addElectricityContract(context, collector)

        return collector.toResult("default-development")
    }

    private suspend fun addRentContract(
        context: DevelopmentSeedContext,
        collector: DevelopmentSeedCollector,
    ): ContractSeed {
        val partner = ensurePartner(
            name = "City Apartments GmbH",
            notes = "Demo landlord",
            collector = collector,
        )
        return ensureMonthlyExpenseContract(
            context = context,
            partner = partner,
            pocketName = "Rent",
            pocketDescription = "Monthly housing payment",
            pocketColor = 0xDC2626,
            contractName = "Apartment Rent",
            contractNotes = "Seed contract for recurring rent payments",
            amount = 120_000,
            firstOccurrenceDate = "2026-01-01",
            dayOfMonth = 1,
            collector = collector,
        )
    }

    private suspend fun addInternetContract(
        context: DevelopmentSeedContext,
        collector: DevelopmentSeedCollector,
    ): ContractSeed {
        val partner = ensurePartner(
            name = "FiberNet",
            notes = "Demo internet provider",
            collector = collector,
        )
        return ensureMonthlyExpenseContract(
            context = context,
            partner = partner,
            pocketName = "Internet",
            pocketDescription = "Home internet subscription",
            pocketColor = 0x7C3AED,
            contractName = "Fiber Internet",
            contractNotes = "Seed contract for recurring internet payments",
            amount = 4_999,
            firstOccurrenceDate = "2026-01-05",
            dayOfMonth = 5,
            collector = collector,
        )
    }

    private suspend fun addElectricityContract(
        context: DevelopmentSeedContext,
        collector: DevelopmentSeedCollector,
    ): ContractSeed {
        val partner = ensurePartner(
            name = "Bright Grid Energy",
            notes = "Demo electricity provider",
            collector = collector,
        )
        return ensureMonthlyExpenseContract(
            context = context,
            partner = partner,
            pocketName = "Electricity",
            pocketDescription = "Monthly electricity payment",
            pocketColor = 0xF59E0B,
            contractName = "Electricity Supply",
            contractNotes = "Seed contract for recurring electricity payments",
            amount = 8_500,
            firstOccurrenceDate = "2026-01-03",
            dayOfMonth = 3,
            collector = collector,
        )
    }

    private suspend fun ensureMonthlyExpenseContract(
        context: DevelopmentSeedContext,
        partner: Partner,
        pocketName: String,
        pocketDescription: String,
        pocketColor: Int,
        contractName: String,
        contractNotes: String,
        amount: Long,
        firstOccurrenceDate: String,
        dayOfMonth: Int,
        collector: DevelopmentSeedCollector,
    ): ContractSeed {
        val pocket = ensurePocket(
            accountId = context.account.id,
            name = pocketName,
            description = pocketDescription,
            color = pocketColor,
            isDefault = false,
            collector = collector,
        )
        val contract = ensureContract(
            pocketId = pocket.id,
            partnerId = partner.id,
            name = contractName,
            startDate = firstOccurrenceDate,
            notes = contractNotes,
            collector = collector,
        )
        val recurringTransaction = ensureRecurringTransaction(
            contractId = contract.id,
            sourcePocketId = pocket.id,
            partnerId = partner.id,
            title = contractName,
            description = contractNotes,
            amount = amount,
            currencyCode = context.account.currencyCode,
            firstOccurrenceDate = firstOccurrenceDate,
            daysOfMonth = listOf(dayOfMonth),
            collector = collector,
        )
        return ContractSeed(contract, recurringTransaction)
    }

    private suspend fun ensureAccount(
        name: String,
        institution: String,
        currencyCode: String,
        weekendHandling: String,
        collector: DevelopmentSeedCollector,
    ): Account {
        val existing = accountRepository.findAll()
            .firstOrNull {
                it.name == name &&
                    it.institution == institution &&
                    it.currencyCode == currencyCode
            }
        if (existing == null) {
            return createAccount(
                CreateAccount.Command(
                    name = name,
                    institution = institution,
                    currencyCode = currencyCode,
                    weekendHandling = weekendHandling,
                ),
            ).also { collector.account(it, SeededResourceStatus.CREATED) }
        }
        if (existing.isArchived) {
            return accountRepository.update(existing.unarchive())
                .also { collector.account(it, SeededResourceStatus.UPDATED) }
        }
        collector.account(existing, SeededResourceStatus.EXISTING)
        return existing
    }

    private suspend fun ensurePocket(
        accountId: String,
        name: String,
        description: String?,
        color: Int,
        isDefault: Boolean,
        collector: DevelopmentSeedCollector,
    ): Pocket {
        val existing = pocketService.list(accountId = accountId)
            .firstOrNull { it.name == name }
        if (existing == null) {
            return pocketService.create(
                CreatePocketCommand(
                    accountId = accountId,
                    name = name,
                    description = description,
                    color = color,
                    isDefault = isDefault,
                ),
            ).also { collector.pocket(it, SeededResourceStatus.CREATED) }
        }
        if (existing.isArchived) {
            return pocketService.unarchive(existing.id)
                .also { collector.pocket(it, SeededResourceStatus.UPDATED) }
        }
        collector.pocket(existing, SeededResourceStatus.EXISTING)
        return existing
    }

    private suspend fun ensurePartner(
        name: String,
        notes: String?,
        collector: DevelopmentSeedCollector,
    ): Partner {
        val existing = partnerService.list(query = name, archived = false)
            .firstOrNull { it.name == name }
        if (existing != null) {
            collector.partner(existing, SeededResourceStatus.EXISTING)
            return existing
        }

        val archived = partnerService.list(query = name, archived = true)
            .firstOrNull { it.name == name }
        if (archived != null) {
            return partnerService.unarchive(archived.id)
                .also { collector.partner(it, SeededResourceStatus.UPDATED) }
        }

        return partnerService.create(CreatePartnerCommand(name = name, notes = notes))
            .also { collector.partner(it, SeededResourceStatus.CREATED) }
    }

    private suspend fun ensureContract(
        pocketId: String,
        partnerId: String,
        name: String,
        startDate: String,
        notes: String,
        collector: DevelopmentSeedCollector,
    ): Contract {
        val existing = contractRepository.findByPocketId(pocketId)
        if (existing == null) {
            return createContract(
                CreateContract.Command(
                    pocketId = pocketId,
                    partnerId = partnerId,
                    name = name,
                    startDate = startDate,
                    endDate = null,
                    notes = notes,
                ),
            ).also { collector.contract(it, SeededResourceStatus.CREATED) }
        }
        if (existing.isArchived) {
            return contractRepository.update(existing.unarchive())
                .also { collector.contract(it, SeededResourceStatus.UPDATED) }
        }
        collector.contract(existing, SeededResourceStatus.EXISTING)
        return existing
    }

    private suspend fun ensureRecurringTransaction(
        contractId: String,
        sourcePocketId: String,
        partnerId: String,
        title: String,
        description: String,
        amount: Long,
        currencyCode: String,
        firstOccurrenceDate: String,
        daysOfMonth: List<Int>,
        collector: DevelopmentSeedCollector,
    ): RecurringTransaction {
        val existing = recurringTransactionRepository.findByContractId(contractId)
            .firstOrNull { it.title == title }
        if (existing == null) {
            return createRecurringTransaction(
                CreateRecurringTransaction.Command(
                    contractId = contractId,
                    sourcePocketId = sourcePocketId,
                    destinationPocketId = null,
                    partnerId = partnerId,
                    title = title,
                    description = description,
                    amount = amount,
                    currencyCode = currencyCode,
                    transactionType = TransactionType.EXPENSE.name,
                    firstOccurrenceDate = firstOccurrenceDate,
                    finalOccurrenceDate = null,
                    recurrenceType = RecurrenceType.MONTHLY.name,
                    skipCount = 0,
                    daysOfWeek = null,
                    weeksOfMonth = null,
                    daysOfMonth = daysOfMonth,
                    monthsOfYear = null,
                    maxRecurrenceCount = null,
                ),
            ).also { collector.recurringTransaction(it, SeededResourceStatus.CREATED) }
        }
        if (existing.isArchived) {
            return recurringTransactionRepository.update(existing.unarchive())
                .also { collector.recurringTransaction(it, SeededResourceStatus.UPDATED) }
        }
        collector.recurringTransaction(existing, SeededResourceStatus.EXISTING)
        return existing
    }

    private data class DevelopmentSeedContext(
        val account: Account,
        val defaultPocket: Pocket,
    )

    private data class ContractSeed(
        val contract: Contract,
        val recurringTransaction: RecurringTransaction,
    )
}

private class DevelopmentSeedCollector {
    private val accounts = linkedMapOf<String, SeededResource>()
    private val pockets = linkedMapOf<String, SeededResource>()
    private val partners = linkedMapOf<String, SeededResource>()
    private val contracts = linkedMapOf<String, SeededResource>()
    private val recurringTransactions = linkedMapOf<String, SeededResource>()

    fun account(account: Account, status: SeededResourceStatus) {
        accounts[account.id] = SeededResource(account.id, account.name, status)
    }

    fun pocket(pocket: Pocket, status: SeededResourceStatus) {
        pockets[pocket.id] = SeededResource(pocket.id, pocket.name, status)
    }

    fun partner(partner: Partner, status: SeededResourceStatus) {
        partners[partner.id] = SeededResource(partner.id, partner.name, status)
    }

    fun contract(contract: Contract, status: SeededResourceStatus) {
        contracts[contract.id] = SeededResource(contract.id, contract.name, status)
    }

    fun recurringTransaction(recurringTransaction: RecurringTransaction, status: SeededResourceStatus) {
        recurringTransactions[recurringTransaction.id] = SeededResource(recurringTransaction.id, recurringTransaction.title, status)
    }

    fun toResult(scenario: String): DevelopmentSeedResult =
        DevelopmentSeedResult(
            scenario = scenario,
            accounts = accounts.values.toList(),
            pockets = pockets.values.toList(),
            partners = partners.values.toList(),
            contracts = contracts.values.toList(),
            recurringTransactions = recurringTransactions.values.toList(),
        )
}
