package de.chennemann.plannr.server.pockets.persistence

import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class R2dbcPocketRepository(
    private val databaseClient: DatabaseClient,
) : PocketRepository {
    override suspend fun save(pocket: Pocket): Pocket {
        var spec = databaseClient.sql(
            """
            INSERT INTO pockets (id, account_id, name, description, color, is_default, is_archived, created_at)
            VALUES (:id, :accountId, :name, :description, :color, :isDefault, :isArchived, :createdAt)
            """.trimIndent(),
        )
            .bind("id", pocket.id)
            .bind("accountId", pocket.accountId)
            .bind("name", pocket.name)
            .bind("color", pocket.color)
            .bind("isDefault", pocket.isDefault)
            .bind("isArchived", pocket.isArchived)
            .bind("createdAt", pocket.createdAt)

        spec = if (pocket.description != null) {
            spec.bind("description", pocket.description)
        } else {
            spec.bindNull("description", String::class.java)
        }

        spec.fetch()
            .rowsUpdated()
            .awaitSingle()

        return pocket
    }

    override suspend fun update(pocket: Pocket): Pocket {
        var spec = databaseClient.sql(
            """
            UPDATE pockets
            SET account_id = :accountId,
                name = :name,
                description = :description,
                color = :color,
                is_default = :isDefault,
                is_archived = :isArchived
            WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", pocket.id)
            .bind("accountId", pocket.accountId)
            .bind("name", pocket.name)
            .bind("color", pocket.color)
            .bind("isDefault", pocket.isDefault)
            .bind("isArchived", pocket.isArchived)

        spec = if (pocket.description != null) {
            spec.bind("description", pocket.description)
        } else {
            spec.bindNull("description", String::class.java)
        }

        spec.fetch()
            .rowsUpdated()
            .awaitSingle()

        return pocket
    }

    override suspend fun findById(id: String): Pocket? =
        databaseClient.sql(
            """
            SELECT id, account_id, name, description, color, is_default, is_archived, created_at
            FROM pockets
            WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", id)
            .fetch()
            .one()
            .map(::toPocket)
            .awaitSingleOrNull()

    override suspend fun findAll(accountId: String?, archived: Boolean?): List<Pocket> {
        val conditions = mutableListOf<String>()
        if (accountId != null) {
            conditions += "account_id = :accountId"
        }
        if (archived != null) {
            conditions += "is_archived = :archived"
        }
        val whereClause = if (conditions.isEmpty()) "" else "WHERE ${conditions.joinToString(" AND ")}"

        var spec = databaseClient.sql(
            """
            SELECT id, account_id, name, description, color, is_default, is_archived, created_at
            FROM pockets
            $whereClause
            ORDER BY created_at ASC, id ASC
            """.trimIndent(),
        )

        if (accountId != null) {
            spec = spec.bind("accountId", accountId)
        }
        if (archived != null) {
            spec = spec.bind("archived", archived)
        }

        return spec.fetch()
            .all()
            .map(::toPocket)
            .collectList()
            .awaitSingle()
    }

    private fun toPocket(row: Map<String, Any?>): Pocket =
        Pocket(
            id = row.getValue("id") as String,
            accountId = row.getValue("account_id") as String,
            name = row.getValue("name") as String,
            description = row["description"] as String?,
            color = (row.getValue("color") as Number).toInt(),
            isDefault = row.getValue("is_default") as Boolean,
            isArchived = row.getValue("is_archived") as Boolean,
            createdAt = (row.getValue("created_at") as Number).toLong(),
        )
}
