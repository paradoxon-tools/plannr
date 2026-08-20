package de.chennemann.plannr.ui.screen.root

import de.chennemann.plannr.data.Transaction
import de.chennemann.plannr.database.repository.TransactionRepository
import de.chennemann.plannr.database.repository.TransactionTemplateRepository
import de.chennemann.plannr.datetime.atEndOfDay
import de.chennemann.plannr.datetime.isAfter
import de.chennemann.plannr.datetime.isBefore
import de.chennemann.plannr.datetime.isBeforeOrEqual
import de.chennemann.plannr.datetime.now
import de.chennemann.plannr.isNeitherNullNorEmpty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

interface RootComponent {
    class Factory(
        private val transactionTemplateRepository: TransactionTemplateRepository,
        private val transactionRepository: TransactionRepository,
    ) {
        operator fun invoke(): RootComponent =
            DefaultRootComponent(
                transactionTemplateRepository = transactionTemplateRepository,
                transactionRepository = transactionRepository,
            )
    }
}

private class DefaultRootComponent(
    private val transactionTemplateRepository: TransactionTemplateRepository,
    private val transactionRepository: TransactionRepository,
) : RootComponent {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val todayEod =
        LocalDate.now().atEndOfDay().toLocalDateTime(TimeZone.currentSystemDefault()).date

    init {
        val materializedTransactions = mutableMapOf<Long, Pair<LocalDate?, LocalDate?>>()

        transactionTemplateRepository.processableTransactionTemplates
            .onEach { processableTemplates ->
                processableTemplates.forEach { processableTemplate ->
                    if (materializedTransactions.containsKey(processableTemplate.template.id)) {
                        val lastMaterializedOccurrence =
                            materializedTransactions[processableTemplate.template.id]?.first
                        val nextOccurrence =
                            materializedTransactions[processableTemplate.template.id]?.second

                        if (
                            processableTemplate.nextOccurrence == nextOccurrence &&
                            processableTemplate.lastMaterializedOccurrence == lastMaterializedOccurrence
                        ) {
                            return@forEach
                        }
                    }

                    val template = processableTemplate.template
                    val processableDatePredicate: (LocalDate) -> Boolean = { date ->
                        val isAfterToday = date.isAfter(todayEod)
                        val isAlreadyMaterialized =
                            processableTemplate.lastMaterializedOccurrence != null &&
                                date.isBeforeOrEqual(processableTemplate.lastMaterializedOccurrence!!)
                        val notYetMaterializable =
                            processableTemplate.nextOccurrence != null &&
                                date.isBefore(processableTemplate.nextOccurrence!!)

                        !isAfterToday && !isAlreadyMaterialized && !notYetMaterializable
                    }

                    val (transactionDates, nextOccurrence) = template.getTransactionDates()
                        .run {
                            var calculatedNextOccurrence: LocalDate? = null
                            val dates = sequence {
                                val iterator = this@run.iterator()
                                while (iterator.hasNext()) {
                                    val item = iterator.next()
                                    if (processableDatePredicate(item)) {
                                        yield(item)
                                    } else {
                                        calculatedNextOccurrence = item
                                        break
                                    }
                                }
                            }.toList()
                            dates to calculatedNextOccurrence
                        }

                    val transactions = transactionDates.map { date ->
                        Transaction(
                            id = -1L,
                            title = template.title,
                            description = template.description,
                            templateId = template.id,
                            contractId = template.contractId,
                            date = date,
                            amount = template.amount,
                            sourcePocketId = template.sourcePocketId,
                            destinationPocketId = template.destinationPocketId,
                            partnerId = template.partnerId,
                        )
                    }

                    if (transactions.isNeitherNullNorEmpty()) {
                        materializedTransactions[processableTemplate.template.id] =
                            processableTemplate.nextOccurrence to processableTemplate.lastMaterializedOccurrence
                        transactionRepository.materializeTransactions(
                            transactions = transactions,
                            templateId = processableTemplate.template.id,
                            latestTransactionDate = transactionDates.max(),
                            nextOccurrence = nextOccurrence,
                        )
                    }
                }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(scope)
    }
}
