package de.chennemann.plannr.server.contracts.persistence

import de.chennemann.plannr.server.contracts.domain.ContractRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class R2dbcContractRepository(
    private val databaseClient: DatabaseClient,
) : ContractRepository {
    override suspend fun save(contract: ContractModel): de.chennemann.plannr.server.contracts.domain.Contract {
        var spec = databaseClient.sql(
            if (contract.id == null) {
                """
                INSERT INTO contracts (pocket_id, partner_id, name, start_date, end_date, notes, is_archived, created_at)
                VALUES (:pocketId, :partnerId, :name, :startDate, :endDate, :notes, :isArchived, :createdAt)
                RETURNING id
                """.trimIndent()
            } else {
                """
                INSERT INTO contracts (id, pocket_id, partner_id, name, start_date, end_date, notes, is_archived, created_at)
                VALUES (:id, :pocketId, :partnerId, :name, :startDate, :endDate, :notes, :isArchived, :createdAt)
                RETURNING id
                """.trimIndent()
            },
        )
        if (contract.id != null) {
            spec = spec.bind("id", contract.id)
        }
        spec = spec
            .bind("pocketId", contract.pocketId)
            .bind("name", contract.name)
            .bind("startDate", contract.startDate)
            .bind("isArchived", contract.isArchived)
            .bind("createdAt", contract.createdAt)

        spec = bindNullableValues(spec, contract)
        val id = spec.fetch().one().map { it.getValue("id") as String }.awaitSingle()
        return findById(id)!!
    }

    override suspend fun update(contract: ContractModel): de.chennemann.plannr.server.contracts.domain.Contract {
        var spec = databaseClient.sql(
            """
            UPDATE contracts
            SET pocket_id = :pocketId,
                partner_id = :partnerId,
                name = :name,
                start_date = :startDate,
                end_date = :endDate,
                notes = :notes,
                is_archived = :isArchived
            WHERE id = :id
            RETURNING id
            """.trimIndent(),
        )
            .bind("id", requireNotNull(contract.id))
            .bind("pocketId", contract.pocketId)
            .bind("name", contract.name)
            .bind("startDate", contract.startDate)
            .bind("isArchived", contract.isArchived)

        spec = bindNullableValues(spec, contract)
        val id = spec.fetch().one().map { it.getValue("id") as String }.awaitSingle()
        return findById(id)!!
    }

    override suspend fun findById(id: String): de.chennemann.plannr.server.contracts.domain.Contract? =
        databaseClient.sql(
            selectSql("WHERE c.id = :id"),
        )
            .bind("id", id)
            .fetch()
            .one()
            .map(::toContract)
            .awaitSingleOrNull()

    override suspend fun findByPocketId(pocketId: String): de.chennemann.plannr.server.contracts.domain.Contract? =
        databaseClient.sql(
            selectSql("WHERE c.pocket_id = :pocketId"),
        )
            .bind("pocketId", pocketId)
            .fetch()
            .one()
            .map(::toContract)
            .awaitSingleOrNull()

    override suspend fun findAll(accountId: String?, archived: Boolean): List<de.chennemann.plannr.server.contracts.domain.Contract> {
        val conditions = mutableListOf<String>()
        if (accountId != null) {
            conditions += "p.account_id = :accountId"
        }
        conditions += "c.is_archived = :archived"
        val whereClause = "WHERE ${conditions.joinToString(" AND ")}"

        var spec = databaseClient.sql(selectSql(whereClause))
            .bind("archived", archived)

        if (accountId != null) {
            spec = spec.bind("accountId", accountId)
        }

        return spec.fetch()
            .all()
            .map(::toContract)
            .collectList()
            .awaitSingle()
    }

    private fun selectSql(whereClause: String) =
        """
        SELECT c.id, p.account_id, c.pocket_id, c.partner_id, c.name, c.start_date, c.end_date, c.notes, c.is_archived, c.created_at
        FROM contracts c
        JOIN pockets p ON p.id = c.pocket_id
        $whereClause
        ORDER BY c.created_at ASC, c.id ASC
        """.trimIndent()

    private fun bindNullableValues(
        spec: DatabaseClient.GenericExecuteSpec,
        contract: ContractModel,
    ): DatabaseClient.GenericExecuteSpec {
        var current = if (contract.partnerId != null) spec.bind("partnerId", contract.partnerId) else spec.bindNull("partnerId", String::class.java)
        current = if (contract.endDate != null) current.bind("endDate", contract.endDate) else current.bindNull("endDate", String::class.java)
        current = if (contract.notes != null) current.bind("notes", contract.notes) else current.bindNull("notes", String::class.java)
        return current
    }

    private fun toContract(row: Map<String, Any?>): de.chennemann.plannr.server.contracts.domain.Contract =
        de.chennemann.plannr.server.contracts.domain.Contract(
            id = row.getValue("id") as String,
            accountId = row.getValue("account_id") as String,
            pocketId = row.getValue("pocket_id") as String,
            partnerId = row["partner_id"] as String?,
            name = row.getValue("name") as String,
            startDate = row.getValue("start_date") as String,
            endDate = row["end_date"] as String?,
            notes = row["notes"] as String?,
            isArchived = row.getValue("is_archived") as Boolean,
            createdAt = (row.getValue("created_at") as Number).toLong(),
        )
}
