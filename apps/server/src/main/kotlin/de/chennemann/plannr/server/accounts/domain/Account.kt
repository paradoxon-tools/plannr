package de.chennemann.plannr.server.accounts.domain

import de.chennemann.plannr.server.common.error.ValidationException

data class Account private constructor(
    val id: String,
    val name: String,
    val institution: String,
    val currencyCode: String,
    val weekendHandling: String,
    val isArchived: Boolean,
    val createdAt: Long,
) {
    fun archive(): Account = copy(isArchived = true)

    fun unarchive(): Account = copy(isArchived = false)

    companion object {
        operator fun invoke(
            id: String,
            name: String,
            institution: String,
            currencyCode: String,
            weekendHandling: String,
            isArchived: Boolean,
            createdAt: Long,
        ): Account {
            val normalizedId = id.trim()
            val normalizedName = name.trim()
            val normalizedInstitution = institution.trim()
            val normalizedCurrencyCode = currencyCode.trim().uppercase()
            val normalizedWeekendHandling = weekendHandling.trim().lowercase()

            if (normalizedId.isBlank()) {
                throw ValidationException("validation_error", "Account id must not be blank")
            }
            if (normalizedName.isBlank()) {
                throw ValidationException("validation_error", "Account name must not be blank")
            }
            if (normalizedInstitution.isBlank()) {
                throw ValidationException("validation_error", "Account institution must not be blank")
            }
            if (normalizedCurrencyCode.isBlank()) {
                throw ValidationException("validation_error", "Account currency code must not be blank")
            }
            if (normalizedWeekendHandling.isBlank()) {
                throw ValidationException("validation_error", "Account weekend handling must not be blank")
            }

            return Account(
                id = normalizedId,
                name = normalizedName,
                institution = normalizedInstitution,
                currencyCode = normalizedCurrencyCode,
                weekendHandling = normalizedWeekendHandling,
                isArchived = isArchived,
                createdAt = createdAt,
            )
        }
    }
}
