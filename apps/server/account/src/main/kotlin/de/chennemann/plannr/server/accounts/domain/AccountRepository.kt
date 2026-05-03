package de.chennemann.plannr.server.accounts.domain

import de.chennemann.plannr.server.accounts.persistence.AccountModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface AccountRepository :
    CoroutineCrudRepository<AccountModel, String>,
    CoroutineSortingRepository<AccountModel, String> {
    @Query(
        """
        INSERT INTO accounts (id, name, institution, currency_code, weekend_handling, is_archived, created_at)
        VALUES (
            COALESCE(:id, CONCAT('acc_', REPLACE(gen_random_uuid()::text, '-', ''))),
            :name, :institution, :currencyCode, :weekendHandling, :isArchived, :createdAt
        )
        RETURNING id, name, institution, currency_code, weekend_handling, is_archived, created_at
        """,
    )
    suspend fun insert(
        id: String?,
        name: String,
        institution: String,
        currencyCode: String,
        weekendHandling: String,
        isArchived: Boolean,
        createdAt: Long,
    ): AccountModel

    @Query(
        """
        UPDATE accounts
        SET name = :name,
            institution = :institution,
            currency_code = :currencyCode,
            weekend_handling = :weekendHandling,
            is_archived = :isArchived
        WHERE id = :id
        RETURNING id, name, institution, currency_code, weekend_handling, is_archived, created_at
        """,
    )
    suspend fun update(
        id: String,
        name: String,
        institution: String,
        currencyCode: String,
        weekendHandling: String,
        isArchived: Boolean,
    ): AccountModel

    fun findAllByOrderByCreatedAtAscIdAsc(): Flow<AccountModel>
}
