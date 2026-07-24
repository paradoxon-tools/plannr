package de.chennemann.plannr.server.transactions.templates.domain

import de.chennemann.plannr.server.transactions.templates.persistence.TransactionTemplateModel
import de.chennemann.plannr.server.transactions.templates.persistence.toDTO
import de.chennemann.plannr.server.transactions.templates.persistence.toModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface TransactionTemplateRepository : CoroutineCrudRepository<TransactionTemplateModel, Long> {
    @Query(
        """
        SELECT
            id,
            source_pocket_id,
            destination_pocket_id,
            financial_profile_id,
            partner_id,
            title,
            description,
            amount,
            currency_code,
            transaction_type,
            first_occurrence_date,
            final_occurrence_date,
            recurrence_type,
            skip_count,
            days_of_week,
            weeks_of_month,
            days_of_month,
            months_of_year,
            previous_version_id,
            is_archived,
            created_at
        FROM transaction_templates
        WHERE source_pocket_id = :pocketId
           OR destination_pocket_id = :pocketId
        ORDER BY created_at ASC, id ASC
        """,
    )
    fun findAllByPocketId(pocketId: Long): Flow<TransactionTemplateModel>

    fun findAllByIsArchivedOrderByCreatedAtAscIdAsc(isArchived: Boolean): Flow<TransactionTemplateModel>
    fun findAllBySourcePocketIdAndIsArchivedOrDestinationPocketIdAndIsArchivedOrderByCreatedAtAscIdAsc(
        sourcePocketId: Long,
        sourceIsArchived: Boolean,
        destinationPocketId: Long,
        destinationIsArchived: Boolean,
    ): Flow<TransactionTemplateModel>
}

suspend fun TransactionTemplateRepository.save(transactionTemplate: TransactionTemplate): TransactionTemplate =
    save(transactionTemplate.toModel()).toDTO()
