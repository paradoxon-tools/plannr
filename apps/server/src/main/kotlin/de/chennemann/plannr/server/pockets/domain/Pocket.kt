package de.chennemann.plannr.server.pockets.domain

import de.chennemann.plannr.server.common.error.ValidationException

data class Pocket private constructor(
    val id: String,
    val accountId: String,
    val name: String,
    val description: String?,
    val color: Int,
    val isDefault: Boolean,
    val isArchived: Boolean,
    val createdAt: Long,
) {
    companion object {
        operator fun invoke(
            id: String,
            accountId: String,
            name: String,
            description: String?,
            color: Int,
            isDefault: Boolean,
            isArchived: Boolean,
            createdAt: Long,
        ): Pocket {
            val normalizedId = id.trim()
            val normalizedAccountId = accountId.trim()
            val normalizedName = name.trim()
            val normalizedDescription = description?.trim()?.takeIf { it.isNotBlank() }

            if (normalizedId.isBlank()) {
                throw ValidationException("validation_error", "Pocket id must not be blank")
            }
            if (normalizedAccountId.isBlank()) {
                throw ValidationException("validation_error", "Pocket account id must not be blank")
            }
            if (normalizedName.isBlank()) {
                throw ValidationException("validation_error", "Pocket name must not be blank")
            }
            if (color < 0) {
                throw ValidationException("validation_error", "Pocket color must not be negative")
            }

            return Pocket(
                id = normalizedId,
                accountId = normalizedAccountId,
                name = normalizedName,
                description = normalizedDescription,
                color = color,
                isDefault = isDefault,
                isArchived = isArchived,
                createdAt = createdAt,
            )
        }
    }
}
