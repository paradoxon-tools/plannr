package de.chennemann.plannr.server.transactions.materialization.service

import de.chennemann.plannr.server.common.time.LocalDateProvider
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.transactions.materialization.domain.MaterializedTransactionRepository
import de.chennemann.plannr.server.transactions.materialization.domain.RecurrenceCalculator
import de.chennemann.plannr.server.transactions.materialization.persistence.MaterializedTransactionModel
import de.chennemann.plannr.server.transactions.materialization.persistence.toDomain
import de.chennemann.plannr.server.transactions.templates.domain.EffectiveTransactionTemplate
import java.time.LocalDate
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
internal class TransactionMaterializerServiceImpl(
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
                materializedTransactionRepository.deleteAllByTransactionTemplateVersionId(operation.transactionTemplate.versionId)
                fullMaterialization(operation.transactionTemplate)
            }
        }

    private suspend fun fullMaterialization(transactionTemplate: EffectiveTransactionTemplate): List<MaterializedTransaction> {
        val occurrences = evaluate(transactionTemplate)
        occurrences.forEach { occurrenceDate ->
            materializedTransactionRepository.findByTransactionTemplateVersionIdAndTransactionDate(
                transactionTemplateVersionId = transactionTemplate.versionId,
                transactionDate = occurrenceDate,
            ) ?: materializedTransactionRepository.save(transactionTemplate.toModel(occurrenceDate))
        }
        return materializedTransactionRepository
            .findAllByTransactionTemplateVersionIdOrderByTransactionDateAscIdAsc(transactionTemplate.versionId)
            .toList()
            .map(MaterializedTransactionModel::toDomain)
    }

    private suspend fun reconcileMaterialization(transactionTemplate: EffectiveTransactionTemplate): List<MaterializedTransaction> {
        val expectedDates = evaluate(transactionTemplate)
        if (expectedDates.isEmpty()) {
            materializedTransactionRepository.deleteAllByTransactionTemplateVersionId(transactionTemplate.versionId)
            return emptyList()
        }
        materializedTransactionRepository.deleteAllByTransactionTemplateVersionIdAndTransactionDateNotIn(
            transactionTemplateVersionId = transactionTemplate.versionId,
            transactionDates = expectedDates,
        )
        expectedDates.forEach { occurrenceDate ->
            materializedTransactionRepository.findByTransactionTemplateVersionIdAndTransactionDate(
                transactionTemplateVersionId = transactionTemplate.versionId,
                transactionDate = occurrenceDate,
            ) ?: materializedTransactionRepository.save(transactionTemplate.toModel(occurrenceDate))
        }
        return materializedTransactionRepository
            .findAllByTransactionTemplateVersionIdOrderByTransactionDateAscIdAsc(transactionTemplate.versionId)
            .toList()
            .map(MaterializedTransactionModel::toDomain)
    }

    private fun evaluate(transactionTemplate: EffectiveTransactionTemplate): List<String> =
        recurrenceCalculator
            .occurrences(
                pattern = transactionTemplate.recurrencePattern,
                endInclusive = materializationHorizon(transactionTemplate),
            )
            .filter { !it.isBefore(LocalDate.parse(transactionTemplate.validFrom)) }
            .map(LocalDate::toString)

    private fun materializationHorizon(transactionTemplate: EffectiveTransactionTemplate): LocalDate {
        val horizon = localDateProvider()
        val explicitEnd = transactionTemplate.recurrencePattern.finalOccurrenceDate?.let(LocalDate::parse)
        val validityEnd = transactionTemplate.validUntil?.let(LocalDate::parse)
        return listOfNotNull(explicitEnd, validityEnd, horizon).minOrNull() ?: horizon
    }

    private fun EffectiveTransactionTemplate.toModel(transactionDate: String): MaterializedTransactionModel =
        MaterializedTransactionModel(
            id = null,
            transactionTemplateId = id,
            transactionTemplateVersionId = versionId,
            contractId = contractId,
            transactionDate = transactionDate,
            sourcePocketId = sourcePocketId,
            destinationPocketId = destinationPocketId,
            financialProfileId = financialProfileId,
            partnerId = partnerId,
            title = title,
            description = description,
            amount = amount,
            currencyCode = currencyCode,
            transactionType = transactionType,
            createdAt = timeProvider(),
        )

}
