package de.chennemann.plannr.server.contracts.domain

import de.chennemann.plannr.server.contracts.persistence.ContractModel
import de.chennemann.plannr.server.contracts.persistence.ContractPocketRow
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ContractRepository : CoroutineCrudRepository<ContractModel, Long> {
    @Modifying
    @Query(
        """
        INSERT INTO contracts (
            pocket_id,
            financial_profile_id,
            partner_id,
            signing_date,
            expiration_date,
            last_cancellation_date
        )
        VALUES (
            :pocketId,
            :financialProfileId,
            :partnerId,
            :signingDate,
            :expirationDate,
            :lastCancellationDate
        )
        ON CONFLICT (pocket_id) DO UPDATE
        SET financial_profile_id = EXCLUDED.financial_profile_id,
            partner_id = EXCLUDED.partner_id,
            signing_date = EXCLUDED.signing_date,
            expiration_date = EXCLUDED.expiration_date,
            last_cancellation_date = EXCLUDED.last_cancellation_date
        """,
    )
    suspend fun upsert(
        pocketId: Long,
        financialProfileId: Long,
        partnerId: Long?,
        signingDate: String?,
        expirationDate: String?,
        lastCancellationDate: String?,
    ): Int

    @Query(
        """
        SELECT
            p.id,
            p.account_id,
            p.name,
            p.description,
            p.color,
            p.is_default,
            p.is_contract_pocket,
            p.is_archived,
            p.created_at,
            c.financial_profile_id,
            c.partner_id,
            c.signing_date,
            c.expiration_date,
            c.last_cancellation_date
        FROM contracts c
        JOIN pockets p ON p.id = c.pocket_id
        WHERE (:accountId IS NULL OR p.account_id = :accountId)
          AND p.is_archived = :archived
        ORDER BY p.created_at ASC, p.id ASC
        """,
    )
    fun findAllWithPocketsByAccountIdAndArchived(accountId: Long?, archived: Boolean): Flow<ContractPocketRow>

    @Query(
        """
        SELECT
            p.id,
            p.account_id,
            p.name,
            p.description,
            p.color,
            p.is_default,
            p.is_contract_pocket,
            p.is_archived,
            p.created_at,
            c.financial_profile_id,
            c.partner_id,
            c.signing_date,
            c.expiration_date,
            c.last_cancellation_date
        FROM contracts c
        JOIN pockets p ON p.id = c.pocket_id
        WHERE c.pocket_id = :pocketId
        """,
    )
    suspend fun findWithPocketByPocketId(pocketId: Long): ContractPocketRow?
}

suspend fun ContractRepository.upsert(model: ContractModel) {
    upsert(
        pocketId = model.pocketId,
        financialProfileId = model.financialProfileId,
        partnerId = model.partnerId,
        signingDate = model.signingDate,
        expirationDate = model.expirationDate,
        lastCancellationDate = model.lastCancellationDate,
    )
}
