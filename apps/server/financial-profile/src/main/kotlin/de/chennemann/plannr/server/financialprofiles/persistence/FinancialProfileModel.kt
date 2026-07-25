package de.chennemann.plannr.server.financialprofiles.persistence

import de.chennemann.plannr.server.financialprofiles.api.dto.FinancialProfile
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("financial_profiles")
data class FinancialProfileModel(
    @Id
    val id: Long?,
    val name: String,
    val description: String?,
    @Column("is_default")
    val isDefault: Boolean,
    @Column("is_fallback")
    val isFallback: Boolean,
    @Column("is_archived")
    val isArchived: Boolean,
    @Column("created_at")
    val createdAt: Long,
)

fun FinancialProfileModel.toDTO(): FinancialProfile =
    FinancialProfile(
        id = requireNotNull(id) { "FinancialProfileModel.id must not be null when mapping to DTO" },
        name = name,
        description = description,
        isDefault = isDefault,
        isFallback = isFallback,
        isArchived = isArchived,
        createdAt = createdAt,
    )

fun FinancialProfile.toModel(): FinancialProfileModel =
    FinancialProfileModel(
        id = id,
        name = name,
        description = description,
        isDefault = isDefault,
        isFallback = isFallback,
        isArchived = isArchived,
        createdAt = createdAt,
    )
