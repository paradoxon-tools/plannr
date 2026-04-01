package de.chennemann.plannr.server.currencies.domain

import de.chennemann.plannr.server.common.error.ValidationException

data class Currency private constructor(
    val code: String,
    val name: String,
    val symbol: String,
    val decimalPlaces: Int,
    val symbolPosition: String,
) {
    companion object {
        operator fun invoke(
            code: String,
            name: String,
            symbol: String,
            decimalPlaces: Int,
            symbolPosition: String,
        ): Currency {
            val normalizedCode = code.trim().uppercase()
            val normalizedName = name.trim()
            val normalizedSymbol = symbol.trim()
            val normalizedSymbolPosition = symbolPosition.trim().lowercase()

            if (normalizedCode.isBlank()) {
                throw ValidationException("validation_error", "Currency code must not be blank")
            }
            if (normalizedName.isBlank()) {
                throw ValidationException("validation_error", "Currency name must not be blank")
            }
            if (normalizedSymbol.isBlank()) {
                throw ValidationException("validation_error", "Currency symbol must not be blank")
            }
            if (decimalPlaces < 0) {
                throw ValidationException(
                    "validation_error",
                    "Currency decimal places must be greater than or equal to 0",
                    mapOf("decimalPlaces" to decimalPlaces),
                )
            }
            if (normalizedSymbolPosition.isBlank()) {
                throw ValidationException("validation_error", "Currency symbol position must not be blank")
            }

            return Currency(
                code = normalizedCode,
                name = normalizedName,
                symbol = normalizedSymbol,
                decimalPlaces = decimalPlaces,
                symbolPosition = normalizedSymbolPosition,
            )
        }
    }
}
