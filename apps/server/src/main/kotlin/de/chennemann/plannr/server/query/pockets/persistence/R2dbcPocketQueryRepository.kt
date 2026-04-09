package de.chennemann.plannr.server.query.pockets.persistence

import de.chennemann.plannr.server.query.pockets.domain.PocketQuery
import de.chennemann.plannr.server.query.pockets.domain.PocketQueryRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class R2dbcPocketQueryRepository(
    private val databaseClient: DatabaseClient,
) : PocketQueryRepository {
    override suspend fun saveOrUpdate(pocketQuery: PocketQuery): PocketQuery {
        val updatedRows = bindAll(
            databaseClient.sql(
                """
                UPDATE pocket_query
                SET account_id = :accountId,
                    name = :name,
                    description = :description,
                    color = :color,
                    is_default = :isDefault,
                    is_archived = :isArchived,
                    created_at = :createdAt
                WHERE pocket_id = :pocketId
                """.trimIndent(),
            ),
            pocketQuery,
        )
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        if (updatedRows == 0L) {
            bindAll(
                databaseClient.sql(
                    """
                    INSERT INTO pocket_query (
                        pocket_id,
                        account_id,
                        name,
                        description,
                        color,
                        is_default,
                        is_archived,
                        created_at,
                        current_balance
                    )
                    VALUES (
                        :pocketId,
                        :accountId,
                        :name,
                        :description,
                        :color,
                        :isDefault,
                        :isArchived,
                        :createdAt,
                        :currentBalance
                    )
                    """.trimIndent(),
                ),
                pocketQuery,
            )
                .bind("currentBalance", pocketQuery.currentBalance)
                .fetch()
                .rowsUpdated()
                .awaitSingle()
        }

        return findById(pocketQuery.pocketId) ?: pocketQuery
    }

    override suspend fun findById(pocketId: String): PocketQuery? =
        databaseClient.sql(
            """
            SELECT pocket_id, account_id, name, description, color, is_default, is_archived, created_at, current_balance
            FROM pocket_query
            WHERE pocket_id = :pocketId
            """.trimIndent(),
        )
            .bind("pocketId", pocketId)
            .fetch()
            .one()
            .map(::toPocketQuery)
            .awaitSingleOrNull()

    private fun bindAll(spec: DatabaseClient.GenericExecuteSpec, pocketQuery: PocketQuery): DatabaseClient.GenericExecuteSpec {
        var current = spec
            .bind("pocketId", pocketQuery.pocketId)
            .bind("accountId", pocketQuery.accountId)
            .bind("name", pocketQuery.name)
            .bind("color", pocketQuery.color)
            .bind("isDefault", pocketQuery.isDefault)
            .bind("isArchived", pocketQuery.isArchived)
            .bind("createdAt", pocketQuery.createdAt)
        current = if (pocketQuery.description == null) {
            current.bindNull("description", String::class.java)
        } else {
            current.bind("description", pocketQuery.description)
        }
        return current
    }

    private fun toPocketQuery(row: Map<String, Any?>): PocketQuery = PocketQuery(
        pocketId = row.getValue("pocket_id") as String,
        accountId = row.getValue("account_id") as String,
        name = row.getValue("name") as String,
        description = row["description"] as String?,
        color = (row.getValue("color") as Number).toInt(),
        isDefault = row.getValue("is_default") as Boolean,
        isArchived = row.getValue("is_archived") as Boolean,
        createdAt = (row.getValue("created_at") as Number).toLong(),
        currentBalance = (row.getValue("current_balance") as Number).toLong(),
    )
}
