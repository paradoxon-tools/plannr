package de.chennemann.plannr.server.financialprofiles.domain

import de.chennemann.plannr.server.financialprofiles.api.dto.FinancialProfile
import de.chennemann.plannr.server.financialprofiles.persistence.FinancialProfileModel
import de.chennemann.plannr.server.financialprofiles.persistence.toDTO
import de.chennemann.plannr.server.financialprofiles.persistence.toModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FinancialProfileRepository : CoroutineCrudRepository<FinancialProfileModel, Long> {
    @Query(
        """
        SELECT id, name, description, is_default, is_fallback, is_archived, created_at
        FROM financial_profiles
        WHERE (:query IS NULL OR LOWER(name) LIKE LOWER(CONCAT('%', :query, '%')))
          AND is_archived = :archived
        ORDER BY created_at ASC, id ASC
        """,
    )
    fun findAllByQueryAndArchived(query: String?, archived: Boolean): Flow<FinancialProfileModel>

    @Query(
        """
        SELECT id, name, description, is_default, is_fallback, is_archived, created_at
        FROM financial_profiles
        WHERE LOWER(BTRIM(name)) = LOWER(BTRIM(:name))
        LIMIT 1
        """,
    )
    suspend fun findByNormalizedName(name: String): FinancialProfileModel?

    @Query(
        """
        SELECT id, name, description, is_default, is_fallback, is_archived, created_at
        FROM financial_profiles
        WHERE is_default = TRUE
        LIMIT 1
        """,
    )
    suspend fun findDefault(): FinancialProfileModel?

    @Query(
        """
        SELECT id, name, description, is_default, is_fallback, is_archived, created_at
        FROM financial_profiles
        WHERE is_fallback = TRUE
        LIMIT 1
        """,
    )
    suspend fun findFallback(): FinancialProfileModel?

    @Modifying
    @Query("UPDATE financial_profiles SET is_default = FALSE WHERE is_default = TRUE")
    suspend fun clearDefault(): Int
}

suspend fun FinancialProfileRepository.save(profile: FinancialProfile): FinancialProfile =
    save(profile.toModel()).toDTO()
