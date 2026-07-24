package de.chennemann.plannr.server.financialprofiles.persistence

import de.chennemann.plannr.server.financialprofiles.domain.FinancialProfileUsageRepository
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component

@Component
internal class R2dbcFinancialProfileUsageRepository(
    private val databaseClient: DatabaseClient,
) : FinancialProfileUsageRepository {
    override suspend fun reassignReferences(
        sourceProfileId: Long,
        fallbackProfileId: Long,
        fallbackProfileName: String,
        fallbackProfileKind: String,
    ) {
        listOf("contracts", "transaction_templates", "transaction_materializations").forEach { table ->
            databaseClient.sql(
                """
                UPDATE $table
                SET financial_profile_id = :fallbackProfileId
                WHERE financial_profile_id = :sourceProfileId
                """.trimIndent(),
            )
                .bind("fallbackProfileId", fallbackProfileId)
                .bind("sourceProfileId", sourceProfileId)
                .fetch()
                .rowsUpdated()
                .awaitSingle()
        }

        listOf("account_transaction_feed", "pocket_transaction_feed", "contract_transaction_feed").forEach { table ->
            databaseClient.sql(
                """
                UPDATE $table
                SET financial_profile_id = :fallbackProfileId,
                    financial_profile_name = :fallbackProfileName,
                    financial_profile_kind = :fallbackProfileKind
                WHERE financial_profile_id = :sourceProfileId
                """.trimIndent(),
            )
                .bind("fallbackProfileId", fallbackProfileId)
                .bind("fallbackProfileName", fallbackProfileName)
                .bind("fallbackProfileKind", fallbackProfileKind)
                .bind("sourceProfileId", sourceProfileId)
                .fetch()
                .rowsUpdated()
                .awaitSingle()
        }
    }
}
