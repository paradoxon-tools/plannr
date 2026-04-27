package de.chennemann.plannr.server.pockets.persistence

import de.chennemann.plannr.server.pockets.domain.PocketRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class R2dbcPocketRepository(
    private val databaseClient: DatabaseClient,
) : PocketRepository {
    override suspend fun save(pocket: PocketModel): de.chennemann.plannr.server.pockets.domain.Pocket {
        var spec = databaseClient.sql(
            if (pocket.id == null) {
                """
                INSERT INTO pockets (account_id, name, description, color, is_default, is_archived, created_at)
                VALUES (:accountId, :name, :description, :color, :isDefault, :isArchived, :createdAt)
                RETURNING id, account_id, name, description, color, is_default, is_archived, created_at
                """.trimIndent()
            } else {
                """
                INSERT INTO pockets (id, account_id, name, description, color, is_default, is_archived, created_at)
                VALUES (:id, :accountId, :name, :description, :color, :isDefault, :isArchived, :createdAt)
                RETURNING id, account_id, name, description, color, is_default, is_archived, created_at
                """.trimIndent()
            },
        )
        if (pocket.id != null) {
            spec = spec.bind("id", pocket.id)
        }
        spec = spec
            .bind("accountId", pocket.accountId)
            .bind("name", pocket.name)
            .bind("color", pocket.color)
            .bind("isDefault", pocket.isDefault)
            .bind("isArchived", pocket.isArchived)
            .bind("createdAt", pocket.createdAt)

        val description = pocket.description
        spec = if (description != null) {
            spec.bind("description", description)
        } else {
            spec.bindNull("description", String::class.java)
        }

        return spec.fetch().one().map(::toPocket).awaitSingle()
    }

    override suspend fun update(pocket: PocketModel): de.chennemann.plannr.server.pockets.domain.Pocket {
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
            RETURNING id, account_id, name, description, color, is_default, is_archived, created_at
            """.trimIndent(),
        )
            .bind("id", requireNotNull(pocket.id))
            .bind("accountId", pocket.accountId)
            .bind("name", pocket.name)
            .bind("color", pocket.color)
            .bind("isDefault", pocket.isDefault)
            .bind("isArchived", pocket.isArchived)

        val description = pocket.description
        spec = if (description != null) {
            spec.bind("description", description)
        } else {
            spec.bindNull("description", String::class.java)
        }

        return spec.fetch().one().map(::toPocket).awaitSingle()
    }

    override suspend fun findById(id: String): de.chennemann.plannr.server.pockets.domain.Pocket? =
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

    override suspend fun findAll(accountId: String?, archived: Boolean?): List<de.chennemann.plannr.server.pockets.domain.Pocket> {
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

    private fun toPocket(row: Map<String, Any?>): de.chennemann.plannr.server.pockets.domain.Pocket =
        PocketModel(
            id = row.getValue("id") as String,
            accountId = row.getValue("account_id") as String,
            name = row.getValue("name") as String,
            description = row["description"] as String?,
            color = (row.getValue("color") as Number).toInt(),
            isDefault = row.getValue("is_default") as Boolean,
            isArchived = row.getValue("is_archived") as Boolean,
            createdAt = (row.getValue("created_at") as Number).toLong(),
        ).toDomain()
}
