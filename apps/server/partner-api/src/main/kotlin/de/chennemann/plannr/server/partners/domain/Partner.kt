package de.chennemann.plannr.server.partners.domain

import de.chennemann.plannr.server.common.error.ValidationException

data class Partner private constructor(
    val id: String,
    val name: String,
    val notes: String?,
    val isArchived: Boolean,
    val createdAt: Long,
) {
    fun archive(): Partner = copy(isArchived = true)

    fun unarchive(): Partner = copy(isArchived = false)

    companion object {
        operator fun invoke(
            id: String,
            name: String,
            notes: String?,
            isArchived: Boolean,
            createdAt: Long,
        ): Partner {
            val normalizedId = id.trim()
            val normalizedName = name.trim()
            val normalizedNotes = notes?.trim()?.takeIf { it.isNotBlank() }

            if (normalizedId.isBlank()) {
                throw ValidationException("validation_error", "Partner id must not be blank")
            }
            if (normalizedName.isBlank()) {
                throw ValidationException("validation_error", "Partner name must not be blank")
            }

            return Partner(
                id = normalizedId,
                name = normalizedName,
                notes = normalizedNotes,
                isArchived = isArchived,
                createdAt = createdAt,
            )
        }
    }
}
