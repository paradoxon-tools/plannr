package de.chennemann.plannr.server.contracts.domain

import de.chennemann.plannr.server.common.error.ValidationException
import java.time.LocalDate
import java.time.format.DateTimeParseException

data class Contract private constructor(
    val id: String,
    val accountId: String,
    val pocketId: String,
    val partnerId: String?,
    val name: String,
    val startDate: String,
    val endDate: String?,
    val notes: String?,
    val isArchived: Boolean,
    val createdAt: Long,
) {
    fun archive(): Contract = copy(isArchived = true)

    fun unarchive(): Contract = copy(isArchived = false)

    companion object {
        operator fun invoke(
            id: String,
            accountId: String,
            pocketId: String,
            partnerId: String?,
            name: String,
            startDate: String,
            endDate: String?,
            notes: String?,
            isArchived: Boolean,
            createdAt: Long,
        ): Contract {
            val normalizedId = id.trim()
            val normalizedAccountId = accountId.trim()
            val normalizedPocketId = pocketId.trim()
            val normalizedPartnerId = partnerId?.trim()?.takeIf { it.isNotBlank() }
            val normalizedName = name.trim()
            val normalizedStartDate = startDate.trim()
            val normalizedEndDate = endDate?.trim()?.takeIf { it.isNotBlank() }
            val normalizedNotes = notes?.trim()?.takeIf { it.isNotBlank() }

            if (normalizedId.isBlank()) {
                throw ValidationException("validation_error", "Contract id must not be blank")
            }
            if (normalizedAccountId.isBlank()) {
                throw ValidationException("validation_error", "Contract account id must not be blank")
            }
            if (normalizedPocketId.isBlank()) {
                throw ValidationException("validation_error", "Contract pocket id must not be blank")
            }
            if (normalizedName.isBlank()) {
                throw ValidationException("validation_error", "Contract name must not be blank")
            }
            if (normalizedStartDate.isBlank()) {
                throw ValidationException("validation_error", "Contract start date must not be blank")
            }

            val parsedStartDate = parseDate(normalizedStartDate, "Contract start date must be a plain date")
            val parsedEndDate = normalizedEndDate?.let { parseDate(it, "Contract end date must be a plain date") }
            if (parsedEndDate != null && parsedEndDate.isBefore(parsedStartDate)) {
                throw ValidationException("validation_error", "Contract end date must not be before start date")
            }

            return Contract(
                id = normalizedId,
                accountId = normalizedAccountId,
                pocketId = normalizedPocketId,
                partnerId = normalizedPartnerId,
                name = normalizedName,
                startDate = normalizedStartDate,
                endDate = normalizedEndDate,
                notes = normalizedNotes,
                isArchived = isArchived,
                createdAt = createdAt,
            )
        }

        private fun parseDate(value: String, message: String): LocalDate =
            try {
                LocalDate.parse(value)
            } catch (_: DateTimeParseException) {
                throw ValidationException("validation_error", message)
            }
    }
}
