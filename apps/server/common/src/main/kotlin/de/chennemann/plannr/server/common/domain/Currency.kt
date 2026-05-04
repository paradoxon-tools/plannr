package de.chennemann.plannr.server.common.domain

import de.chennemann.plannr.server.common.error.ValidationException

enum class Currency(
    val symbol: String,
    val decimalPlaces: Int,
    val symbolPosition: String,
) {
    EUR(symbol = "€", decimalPlaces = 2, symbolPosition = "before"),
    USD(symbol = "$", decimalPlaces = 2, symbolPosition = "before"),
    ;

    companion object {
        fun from(value: String): Currency {
            val normalized = value.trim().uppercase()
            if (normalized.isBlank()) {
                throw ValidationException("validation_error", "Currency code is invalid")
            }
            return entries.firstOrNull { it.name == normalized }
                ?: throw ValidationException("validation_error", "Currency code is invalid")
        }
    }
}

fun normalizeCurrency(value: String): String = Currency.from(value).name
