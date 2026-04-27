package de.chennemann.plannr.server.partners.persistence

import de.chennemann.plannr.server.partners.domain.PartnerRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class R2dbcPartnerRepository(
    private val databaseClient: DatabaseClient,
) : PartnerRepository {
    override suspend fun save(partner: PartnerModel): de.chennemann.plannr.server.partners.domain.Partner =
        if (partner.id == null) {
            bindNullableFields(
                databaseClient.sql(
                    """
                    INSERT INTO partners (name, notes, is_archived, created_at)
                    VALUES (:name, :notes, :isArchived, :createdAt)
                    RETURNING id, name, notes, is_archived, created_at
                    """.trimIndent(),
                )
                    .bind("name", partner.name)
                    .bind("isArchived", partner.isArchived)
                    .bind("createdAt", partner.createdAt),
                partner.notes,
            )
                .fetch()
                .one()
                .map(::toPartner)
                .awaitSingle()
        } else {
            var spec = databaseClient.sql(
                """
                INSERT INTO partners (id, name, notes, is_archived, created_at)
                VALUES (:id, :name, :notes, :isArchived, :createdAt)
                RETURNING id, name, notes, is_archived, created_at
                """.trimIndent(),
            )
                .bind("id", partner.id)
                .bind("name", partner.name)
                .bind("isArchived", partner.isArchived)
                .bind("createdAt", partner.createdAt)

            spec = bindNullableFields(spec, partner.notes)
            spec.fetch().one().map(::toPartner).awaitSingle()
        }

    override suspend fun update(partner: PartnerModel): de.chennemann.plannr.server.partners.domain.Partner {
        var spec = databaseClient.sql(
            """
            UPDATE partners
            SET name = :name,
                notes = :notes,
                is_archived = :isArchived
            WHERE id = :id
            RETURNING id, name, notes, is_archived, created_at
            """.trimIndent(),
        )
            .bind("id", requireNotNull(partner.id))
            .bind("name", partner.name)
            .bind("isArchived", partner.isArchived)
        spec = bindNullableFields(spec, partner.notes)
        return spec.fetch().one().map(::toPartner).awaitSingle()
    }

    override suspend fun findById(id: String): de.chennemann.plannr.server.partners.domain.Partner? =
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

    override suspend fun findAll(query: String?, archived: Boolean): List<de.chennemann.plannr.server.partners.domain.Partner> {
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

    private fun bindNullableFields(
        spec: DatabaseClient.GenericExecuteSpec,
        notes: String?,
    ): DatabaseClient.GenericExecuteSpec =
        if (notes != null) spec.bind("notes", notes) else spec.bindNull("notes", String::class.java)

    private fun toPartner(row: Map<String, Any?>): de.chennemann.plannr.server.partners.domain.Partner =
        PartnerModel(
            id = row.getValue("id") as String,
            name = row.getValue("name") as String,
            notes = row["notes"] as String?,
            isArchived = row.getValue("is_archived") as Boolean,
            createdAt = (row.getValue("created_at") as Number).toLong(),
        ).toDomain()
}
