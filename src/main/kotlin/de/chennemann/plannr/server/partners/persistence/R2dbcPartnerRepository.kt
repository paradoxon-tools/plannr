package de.chennemann.plannr.server.partners.persistence

import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.domain.PartnerRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class R2dbcPartnerRepository(
    private val databaseClient: DatabaseClient,
) : PartnerRepository {
    override suspend fun save(partner: Partner): Partner {
        var spec = databaseClient.sql(
            """
            INSERT INTO partners (id, name, notes, is_archived, created_at)
            VALUES (:id, :name, :notes, :isArchived, :createdAt)
            """.trimIndent(),
        )
            .bind("id", partner.id)
            .bind("name", partner.name)
            .bind("isArchived", partner.isArchived)
            .bind("createdAt", partner.createdAt)

        spec = if (partner.notes != null) spec.bind("notes", partner.notes) else spec.bindNull("notes", String::class.java)

        spec.fetch().rowsUpdated().awaitSingle()
        return partner
    }

    override suspend fun update(partner: Partner): Partner {
        var spec = databaseClient.sql(
            """
            UPDATE partners
            SET name = :name,
                notes = :notes,
                is_archived = :isArchived
            WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", partner.id)
            .bind("name", partner.name)
            .bind("isArchived", partner.isArchived)

        spec = if (partner.notes != null) spec.bind("notes", partner.notes) else spec.bindNull("notes", String::class.java)

        spec.fetch().rowsUpdated().awaitSingle()
        return partner
    }

    override suspend fun findById(id: String): Partner? =
        databaseClient.sql(
            """
            SELECT id, name, notes, is_archived, created_at
            FROM partners
            WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", id)
            .fetch()
            .one()
            .map(::toPartner)
            .awaitSingleOrNull()

    override suspend fun findAll(query: String?, archived: Boolean): List<Partner> {
        val conditions = mutableListOf<String>()
        if (query != null && query.isNotBlank()) {
            conditions += "LOWER(name) LIKE :query"
        }
        conditions += "is_archived = :archived"
        val whereClause = "WHERE ${conditions.joinToString(" AND ")}"

        var spec = databaseClient.sql(
            """
            SELECT id, name, notes, is_archived, created_at
            FROM partners
            $whereClause
            ORDER BY created_at ASC, id ASC
            """.trimIndent(),
        )
            .bind("archived", archived)

        if (query != null && query.isNotBlank()) {
            spec = spec.bind("query", "%${query.trim().lowercase()}%")
        }

        return spec.fetch()
            .all()
            .map(::toPartner)
            .collectList()
            .awaitSingle()
    }

    private fun toPartner(row: Map<String, Any?>): Partner =
        Partner(
            id = row.getValue("id") as String,
            name = row.getValue("name") as String,
            notes = row["notes"] as String?,
            isArchived = row.getValue("is_archived") as Boolean,
            createdAt = (row.getValue("created_at") as Number).toLong(),
        )
}
