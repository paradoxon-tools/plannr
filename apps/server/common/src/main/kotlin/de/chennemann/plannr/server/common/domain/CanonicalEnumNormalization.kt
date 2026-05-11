package de.chennemann.plannr.server.common.domain

import de.chennemann.plannr.server.common.error.ValidationException

fun normalizeTransactionType(value: String): String = normalizeEnumValue<TransactionType>(value, "Transaction type is invalid")

fun normalizeTransactionStatus(value: String): String = normalizeEnumValue<TransactionStatus>(value, "Transaction status is invalid")

fun normalizeRecurrenceType(value: String): String = normalizeEnumValue<RecurrenceType>(value, "Transaction template recurrence type is invalid")

fun normalizeWeekendHandling(value: String): String = normalizeEnumValue<WeekendHandling>(value, "Account weekend handling is invalid")

fun normalizeTransactionOrigin(value: String): String = normalizeEnumValue<TransactionOrigin>(value, "Transaction origin is invalid")

private inline fun <reified T : Enum<T>> normalizeEnumValue(value: String, message: String): String {
    val normalized = value.trim().uppercase()
    if (normalized.isBlank()) {
        throw ValidationException("validation_error", message)
    }
    return enumValues<T>().firstOrNull { it.name == normalized }?.name
        ?: throw ValidationException("validation_error", message)
}
