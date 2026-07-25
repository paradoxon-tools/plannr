package de.chennemann.plannr.server.transactions.projection.service

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
internal class TransactionProjectionRebuilder(
    private val databaseClient: DatabaseClient,
) {
    @Transactional
    suspend fun rebuildAll() {
        databaseClient.sql("DELETE FROM contract_transaction_feed").fetch().rowsUpdated().awaitSingle()
        databaseClient.sql("DELETE FROM pocket_transaction_feed").fetch().rowsUpdated().awaitSingle()
        databaseClient.sql("DELETE FROM account_transaction_feed").fetch().rowsUpdated().awaitSingle()
        databaseClient.sql(INSERT_ACCOUNT_FEED).fetch().rowsUpdated().awaitSingle()
        databaseClient.sql(INSERT_POCKET_FEED).fetch().rowsUpdated().awaitSingle()
        databaseClient.sql(INSERT_CONTRACT_FEED).fetch().rowsUpdated().awaitSingle()
    }

    private companion object {
        val INSERT_ACCOUNT_FEED = """
            INSERT INTO account_transaction_feed (
                account_id,
                transaction_id,
                transaction_template_id,
                history_position,
                transaction_date,
                type,
                title,
                description,
                transaction_amount,
                signed_amount,
                balance_after,
                financial_profile_id,
                financial_profile_name,
                partner_id,
                partner_name,
                source_pocket_id,
                source_pocket_name,
                source_pocket_color,
                destination_pocket_id,
                destination_pocket_name,
                destination_pocket_color,
                is_archived
            )
            WITH account_entries AS (
                SELECT
                    account_id,
                    transaction_id,
                    transaction_template_id,
                    transaction_date,
                    type,
                    title,
                    description,
                    transaction_amount,
                    SUM(signed_amount) AS signed_amount,
                    financial_profile_id,
                    financial_profile_name,
                    partner_id,
                    partner_name,
                    source_pocket_id,
                    source_pocket_name,
                    source_pocket_color,
                    destination_pocket_id,
                    destination_pocket_name,
                    destination_pocket_color,
                    is_archived,
                    created_at
                FROM (
                    SELECT
                        sp.account_id,
                        m.id AS transaction_id,
                        m.transaction_template_id,
                        m.transaction_date,
                        m.transaction_type AS type,
                        m.title,
                        m.description,
                        m.amount AS transaction_amount,
                        -m.amount AS signed_amount,
                        m.financial_profile_id,
                        financial_profile.name AS financial_profile_name,
                        m.partner_id,
                        partner.name AS partner_name,
                        sp.id AS source_pocket_id,
                        COALESCE(sp.name, spc.name) AS source_pocket_name,
                        COALESCE(sp.color, spc.color) AS source_pocket_color,
                        dp.id AS destination_pocket_id,
                        COALESCE(dp.name, dpc.name) AS destination_pocket_name,
                        COALESCE(dp.color, dpc.color) AS destination_pocket_color,
                        template.is_archived,
                        m.created_at
                    FROM transaction_materializations m
                    JOIN transaction_templates template ON template.id = m.transaction_template_id
                    JOIN pockets sp ON sp.id = m.source_pocket_id
                    LEFT JOIN pockets dp ON dp.id = m.destination_pocket_id
                    LEFT JOIN contracts spc ON spc.id = sp.contract_id
                    LEFT JOIN contracts dpc ON dpc.id = dp.contract_id
                    JOIN financial_profiles financial_profile ON financial_profile.id = m.financial_profile_id
                    LEFT JOIN partners partner ON partner.id = m.partner_id
                    WHERE m.transaction_type IN ('EXPENSE', 'TRANSFER')
                    UNION ALL
                    SELECT
                        dp.account_id,
                        m.id AS transaction_id,
                        m.transaction_template_id,
                        m.transaction_date,
                        m.transaction_type AS type,
                        m.title,
                        m.description,
                        m.amount AS transaction_amount,
                        m.amount AS signed_amount,
                        m.financial_profile_id,
                        financial_profile.name AS financial_profile_name,
                        m.partner_id,
                        partner.name AS partner_name,
                        sp.id AS source_pocket_id,
                        COALESCE(sp.name, spc.name) AS source_pocket_name,
                        COALESCE(sp.color, spc.color) AS source_pocket_color,
                        dp.id AS destination_pocket_id,
                        COALESCE(dp.name, dpc.name) AS destination_pocket_name,
                        COALESCE(dp.color, dpc.color) AS destination_pocket_color,
                        template.is_archived,
                        m.created_at
                    FROM transaction_materializations m
                    JOIN transaction_templates template ON template.id = m.transaction_template_id
                    LEFT JOIN pockets sp ON sp.id = m.source_pocket_id
                    JOIN pockets dp ON dp.id = m.destination_pocket_id
                    LEFT JOIN contracts spc ON spc.id = sp.contract_id
                    LEFT JOIN contracts dpc ON dpc.id = dp.contract_id
                    JOIN financial_profiles financial_profile ON financial_profile.id = m.financial_profile_id
                    LEFT JOIN partners partner ON partner.id = m.partner_id
                    WHERE m.transaction_type IN ('INCOME', 'TRANSFER')
                ) scoped
                GROUP BY
                    account_id,
                    transaction_id,
                    transaction_template_id,
                    transaction_date,
                    type,
                    title,
                    description,
                    transaction_amount,
                    financial_profile_id,
                    financial_profile_name,
                    partner_id,
                    partner_name,
                    source_pocket_id,
                    source_pocket_name,
                    source_pocket_color,
                    destination_pocket_id,
                    destination_pocket_name,
                    destination_pocket_color,
                    is_archived,
                    created_at
            ),
            ordered AS (
                SELECT
                    account_entries.*,
                    ROW_NUMBER() OVER (
                        PARTITION BY account_id
                        ORDER BY transaction_date ASC, created_at ASC, transaction_id ASC
                    ) AS history_position,
                    SUM(signed_amount) OVER (
                        PARTITION BY account_id
                        ORDER BY transaction_date ASC, created_at ASC, transaction_id ASC
                        ROWS UNBOUNDED PRECEDING
                    ) AS balance_after
                FROM account_entries
            )
            SELECT
                account_id,
                transaction_id,
                transaction_template_id,
                history_position,
                transaction_date,
                type,
                title,
                description,
                transaction_amount,
                signed_amount,
                balance_after,
                financial_profile_id,
                financial_profile_name,
                partner_id,
                partner_name,
                source_pocket_id,
                source_pocket_name,
                source_pocket_color,
                destination_pocket_id,
                destination_pocket_name,
                destination_pocket_color,
                is_archived
            FROM ordered
        """.trimIndent()

        val INSERT_POCKET_FEED = """
            INSERT INTO pocket_transaction_feed (
                pocket_id,
                account_id,
                transaction_id,
                transaction_template_id,
                history_position,
                transaction_date,
                type,
                title,
                description,
                transaction_amount,
                signed_amount,
                balance_after,
                financial_profile_id,
                financial_profile_name,
                partner_id,
                partner_name,
                transfer_pocket_id,
                transfer_pocket_name,
                transfer_pocket_color,
                is_archived
            )
            WITH pocket_entries AS (
                SELECT
                    pocket_id,
                    account_id,
                    transaction_id,
                    transaction_template_id,
                    transaction_date,
                    type,
                    title,
                    description,
                    transaction_amount,
                    SUM(signed_amount) AS signed_amount,
                    financial_profile_id,
                    financial_profile_name,
                    partner_id,
                    partner_name,
                    transfer_pocket_id,
                    transfer_pocket_name,
                    transfer_pocket_color,
                    is_archived,
                    created_at
                FROM (
                    SELECT
                        sp.id AS pocket_id,
                        sp.account_id,
                        m.id AS transaction_id,
                        m.transaction_template_id,
                        m.transaction_date,
                        m.transaction_type AS type,
                        m.title,
                        m.description,
                        m.amount AS transaction_amount,
                        -m.amount AS signed_amount,
                        m.financial_profile_id,
                        financial_profile.name AS financial_profile_name,
                        m.partner_id,
                        partner.name AS partner_name,
                        dp.id AS transfer_pocket_id,
                        COALESCE(dp.name, dpc.name) AS transfer_pocket_name,
                        COALESCE(dp.color, dpc.color) AS transfer_pocket_color,
                        template.is_archived,
                        m.created_at
                    FROM transaction_materializations m
                    JOIN transaction_templates template ON template.id = m.transaction_template_id
                    JOIN pockets sp ON sp.id = m.source_pocket_id
                    LEFT JOIN pockets dp ON dp.id = m.destination_pocket_id
                    LEFT JOIN contracts dpc ON dpc.id = dp.contract_id
                    JOIN financial_profiles financial_profile ON financial_profile.id = m.financial_profile_id
                    LEFT JOIN partners partner ON partner.id = m.partner_id
                    WHERE m.transaction_type IN ('EXPENSE', 'TRANSFER')
                    UNION ALL
                    SELECT
                        dp.id AS pocket_id,
                        dp.account_id,
                        m.id AS transaction_id,
                        m.transaction_template_id,
                        m.transaction_date,
                        m.transaction_type AS type,
                        m.title,
                        m.description,
                        m.amount AS transaction_amount,
                        m.amount AS signed_amount,
                        m.financial_profile_id,
                        financial_profile.name AS financial_profile_name,
                        m.partner_id,
                        partner.name AS partner_name,
                        sp.id AS transfer_pocket_id,
                        COALESCE(sp.name, spc.name) AS transfer_pocket_name,
                        COALESCE(sp.color, spc.color) AS transfer_pocket_color,
                        template.is_archived,
                        m.created_at
                    FROM transaction_materializations m
                    JOIN transaction_templates template ON template.id = m.transaction_template_id
                    LEFT JOIN pockets sp ON sp.id = m.source_pocket_id
                    JOIN pockets dp ON dp.id = m.destination_pocket_id
                    LEFT JOIN contracts spc ON spc.id = sp.contract_id
                    JOIN financial_profiles financial_profile ON financial_profile.id = m.financial_profile_id
                    LEFT JOIN partners partner ON partner.id = m.partner_id
                    WHERE m.transaction_type IN ('INCOME', 'TRANSFER')
                ) scoped
                GROUP BY
                    pocket_id,
                    account_id,
                    transaction_id,
                    transaction_template_id,
                    transaction_date,
                    type,
                    title,
                    description,
                    transaction_amount,
                    financial_profile_id,
                    financial_profile_name,
                    partner_id,
                    partner_name,
                    transfer_pocket_id,
                    transfer_pocket_name,
                    transfer_pocket_color,
                    is_archived,
                    created_at
            ),
            ordered AS (
                SELECT
                    pocket_entries.*,
                    ROW_NUMBER() OVER (
                        PARTITION BY pocket_id
                        ORDER BY transaction_date ASC, created_at ASC, transaction_id ASC
                    ) AS history_position,
                    SUM(signed_amount) OVER (
                        PARTITION BY pocket_id
                        ORDER BY transaction_date ASC, created_at ASC, transaction_id ASC
                        ROWS UNBOUNDED PRECEDING
                    ) AS balance_after
                FROM pocket_entries
            )
            SELECT
                pocket_id,
                account_id,
                transaction_id,
                transaction_template_id,
                history_position,
                transaction_date,
                type,
                title,
                description,
                transaction_amount,
                signed_amount,
                balance_after,
                financial_profile_id,
                financial_profile_name,
                partner_id,
                partner_name,
                transfer_pocket_id,
                transfer_pocket_name,
                transfer_pocket_color,
                is_archived
            FROM ordered
        """.trimIndent()

        val INSERT_CONTRACT_FEED = """
            INSERT INTO contract_transaction_feed (
                contract_id,
                account_id,
                transaction_id,
                transaction_template_id,
                history_position,
                transaction_date,
                type,
                title,
                description,
                transaction_amount,
                signed_amount,
                balance_after,
                financial_profile_id,
                financial_profile_name,
                partner_id,
                partner_name,
                transfer_pocket_id,
                transfer_pocket_name,
                transfer_pocket_color,
                is_archived
            )
            WITH entries AS (
                SELECT
                    m.contract_id,
                    CASE
                        WHEN m.transaction_type = 'INCOME' THEN dp.account_id
                        ELSE COALESCE(sp.account_id, dp.account_id)
                    END AS account_id,
                    m.id AS transaction_id,
                    m.transaction_template_id,
                    m.transaction_date,
                    m.transaction_type AS type,
                    m.title,
                    m.description,
                    m.amount AS transaction_amount,
                    CASE
                        WHEN m.transaction_type = 'EXPENSE' THEN -m.amount
                        WHEN m.transaction_type = 'INCOME' THEN m.amount
                        WHEN sp.contract_id = m.contract_id AND dp.contract_id = m.contract_id THEN 0
                        WHEN dp.contract_id = m.contract_id THEN m.amount
                        WHEN sp.contract_id = m.contract_id THEN -m.amount
                        ELSE 0
                    END AS signed_amount,
                    m.financial_profile_id,
                    financial_profile.name AS financial_profile_name,
                    m.partner_id,
                    partner.name AS partner_name,
                    CASE WHEN sp.contract_id = m.contract_id THEN dp.id ELSE sp.id END AS transfer_pocket_id,
                    CASE
                        WHEN sp.contract_id = m.contract_id THEN COALESCE(dp.name, dpc.name)
                        ELSE COALESCE(sp.name, spc.name)
                    END AS transfer_pocket_name,
                    CASE
                        WHEN sp.contract_id = m.contract_id THEN COALESCE(dp.color, dpc.color)
                        ELSE COALESCE(sp.color, spc.color)
                    END AS transfer_pocket_color,
                    template.is_archived,
                    m.created_at
                FROM transaction_materializations m
                JOIN transaction_templates template ON template.id = m.transaction_template_id
                JOIN financial_profiles financial_profile ON financial_profile.id = m.financial_profile_id
                LEFT JOIN partners partner ON partner.id = m.partner_id
                LEFT JOIN pockets sp ON sp.id = m.source_pocket_id
                LEFT JOIN pockets dp ON dp.id = m.destination_pocket_id
                LEFT JOIN contracts spc ON spc.id = sp.contract_id
                LEFT JOIN contracts dpc ON dpc.id = dp.contract_id
                WHERE m.contract_id IS NOT NULL
            )
            SELECT
                contract_id,
                account_id,
                transaction_id,
                transaction_template_id,
                ROW_NUMBER() OVER (
                    PARTITION BY contract_id
                    ORDER BY transaction_date ASC, created_at ASC, transaction_id ASC
                ) AS history_position,
                transaction_date,
                type,
                title,
                description,
                transaction_amount,
                signed_amount,
                SUM(signed_amount) OVER (
                    PARTITION BY contract_id
                    ORDER BY transaction_date ASC, created_at ASC, transaction_id ASC
                    ROWS UNBOUNDED PRECEDING
                ) AS balance_after,
                financial_profile_id,
                financial_profile_name,
                partner_id,
                partner_name,
                transfer_pocket_id,
                transfer_pocket_name,
                transfer_pocket_color,
                is_archived
            FROM entries
        """.trimIndent()
    }
}
