package de.chennemann.plannr.server.transactions.materialization.service

import de.chennemann.plannr.server.common.time.LocalDateProvider
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.transactions.materialization.domain.MaterializedTransactionRepository
import de.chennemann.plannr.server.transactions.materialization.domain.RecurrenceCalculator
import de.chennemann.plannr.server.transactions.materialization.persistence.MaterializedTransactionModel
import de.chennemann.plannr.server.transactions.materialization.persistence.toDomain
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate
import java.time.LocalDate
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TransactionMaterializerServiceImpl(
    private val materializedTransactionRepository: MaterializedTransactionRepository,
    private val localDateProvider: LocalDateProvider,
    private val timeProvider: TimeProvider,
    private val recurrenceCalculator: RecurrenceCalculator = RecurrenceCalculator(),
) : TransactionMaterializerService {
    override suspend fun materialize(operation: MaterializationOperation): List<MaterializedTransaction> =
        when (operation) {
            is MaterializationOperation.NewTransactionTemplate -> fullMaterialization(operation.transactionTemplate)
            is MaterializationOperation.EndDateChange -> reconcileMaterialization(operation.transactionTemplate)
            is MaterializationOperation.FullRefresh -> {
                materializedTransactionRepository.deleteAllByTransactionTemplateId(operation.transactionTemplate.id)
                fullMaterialization(operation.transactionTemplate)
            }
        }

    private suspend fun fullMaterialization(transactionTemplate: TransactionTemplate): List<MaterializedTransaction> {
        val occurrences = evaluate(transactionTemplate)
        occurrences.forEach { occurrenceDate ->
            materializedTransactionRepository.findByTransactionTemplateIdAndTransactionDate(
                transactionTemplateId = transactionTemplate.id,
                transactionDate = occurrenceDate,
            ) ?: materializedTransactionRepository.save(transactionTemplate.toModel(occurrenceDate))
        }
        return materializedTransactionRepository
            .findAllByTransactionTemplateIdOrderByTransactionDateAscIdAsc(transactionTemplate.id)
            .toList()
            .map(MaterializedTransactionModel::toDomain)
    }

    private suspend fun reconcileMaterialization(transactionTemplate: TransactionTemplate): List<MaterializedTransaction> {
        val expectedDates = evaluate(transactionTemplate)
        if (expectedDates.isEmpty()) {
            materializedTransactionRepository.deleteAllByTransactionTemplateId(transactionTemplate.id)
            return emptyList()
        }
        materializedTransactionRepository.deleteAllByTransactionTemplateIdAndTransactionDateNotIn(
            transactionTemplateId = transactionTemplate.id,
            transactionDates = expectedDates,
        )
        expectedDates.forEach { occurrenceDate ->
            materializedTransactionRepository.findByTransactionTemplateIdAndTransactionDate(
                transactionTemplateId = transactionTemplate.id,
                transactionDate = occurrenceDate,
            ) ?: materializedTransactionRepository.save(transactionTemplate.toModel(occurrenceDate))
        }
        return materializedTransactionRepository
            .findAllByTransactionTemplateIdOrderByTransactionDateAscIdAsc(transactionTemplate.id)
            .toList()
            .map(MaterializedTransactionModel::toDomain)
    }

    private fun evaluate(transactionTemplate: TransactionTemplate): List<String> =
        recurrenceCalculator
            .occurrences(
                pattern = transactionTemplate.recurrencePattern,
                endInclusive = materializationHorizon(transactionTemplate),
            )
            .map(LocalDate::toString)

    private fun materializationHorizon(transactionTemplate: TransactionTemplate): LocalDate {
        val horizon = localDateProvider()
        val explicitEnd = transactionTemplate.recurrencePattern.finalOccurrenceDate?.let(LocalDate::parse)
        return listOfNotNull(explicitEnd, horizon).minOrNull() ?: horizon
    }

    private fun TransactionTemplate.toModel(transactionDate: String): MaterializedTransactionModel =
        MaterializedTransactionModel(
            id = null,
            transactionTemplateId = id,
            transactionDate = transactionDate,
            sourcePocketId = sourcePocketId,
            destinationPocketId = destinationPocketId,
            partnerId = partnerId,
            title = title,
            description = description,
            amount = amount,
            currencyCode = currencyCode,
            transactionType = transactionType,
            createdAt = timeProvider(),
        )

}
