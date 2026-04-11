package de.chennemann.plannr.server.common.domain

import de.chennemann.plannr.server.common.error.ValidationException

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER,
}

enum class TransactionStatus {
    PENDING,
    CLEARED,
    RECONCILED,
}

enum class RecurrenceType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
}

enum class WeekendHandling {
    NO_SHIFT,
    MOVE_BEFORE,
    MOVE_AFTER,
}

enum class TransactionOrigin {
    MANUAL,
    RECURRING_MATERIALIZED,
    RECURRING_MODIFICATION,
}

internal fun normalizeTransactionType(value: String): String = normalizeEnumValue<TransactionType>(value, "Transaction type is invalid")

internal fun normalizeTransactionStatus(value: String): String = normalizeEnumValue<TransactionStatus>(value, "Transaction status is invalid")

internal fun normalizeRecurrenceType(value: String): String = normalizeEnumValue<RecurrenceType>(value, "Recurring transaction recurrence type is invalid")

internal fun normalizeWeekendHandling(value: String): String = normalizeEnumValue<WeekendHandling>(value, "Account weekend handling is invalid")

internal fun normalizeTransactionOrigin(value: String): String = normalizeEnumValue<TransactionOrigin>(value, "Transaction origin is invalid")

private inline fun <reified T : Enum<T>> normalizeEnumValue(value: String, message: String): String {
    val normalized = value.trim().uppercase()
    if (normalized.isBlank()) {
        throw ValidationException("validation_error", message)
    }
    return enumValues<T>().firstOrNull { it.name == normalized }?.name
        ?: throw ValidationException("validation_error", message)
}
