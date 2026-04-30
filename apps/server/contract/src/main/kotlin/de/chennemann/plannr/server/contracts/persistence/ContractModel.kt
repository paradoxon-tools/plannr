package de.chennemann.plannr.server.contracts.persistence

import de.chennemann.plannr.server.contracts.domain.Contract
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import kotlin.jvm.JvmName

@Table("contracts")
data class ContractModel(
    @field:Id
    @get:JvmName("getEntityId")
    val id: String?,
    @Column("account_id")
    val accountId: String,
    @Column("pocket_id")
    val pocketId: String,
    @Column("partner_id")
    val partnerId: String?,
    val name: String,
    @Column("start_date")
    val startDate: String,
    @Column("end_date")
    val endDate: String?,
    val notes: String?,
    @Column("is_archived")
    val isArchived: Boolean,
    @Column("created_at")
    val createdAt: Long,
    @Transient
    val persisted: Boolean = false,
) : Persistable<String> {
    @PersistenceCreator
    constructor(
        id: String?,
        accountId: String,
        pocketId: String,
        partnerId: String?,
        name: String,
        startDate: String,
        endDate: String?,
        notes: String?,
        isArchived: Boolean,
        createdAt: Long,
    ) : this(id, accountId, pocketId, partnerId, name, startDate, endDate, notes, isArchived, createdAt, persisted = true)

    override fun getId(): String? = id

    override fun isNew(): Boolean = !persisted

    fun persisted(): ContractModel = copy(persisted = true)
}

fun Contract.toModel(): ContractModel =
    ContractModel(
        id = id,
        accountId = accountId,
        pocketId = pocketId,
        partnerId = partnerId,
        name = name,
        startDate = startDate,
        endDate = endDate,
        notes = notes,
        isArchived = isArchived,
        createdAt = createdAt,
    )

fun Contract.toPersistedModel(): ContractModel = toModel().persisted()

fun ContractModel.toDomain(): Contract =
    Contract(
        id = requireNotNull(id),
        accountId = accountId,
        pocketId = pocketId,
        partnerId = partnerId,
        name = name,
        startDate = startDate,
        endDate = endDate,
        notes = notes,
        isArchived = isArchived,
        createdAt = createdAt,
    )
