package de.chennemann.plannr.server.currencies.application

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.domain.CurrencyRepository
import org.springframework.stereotype.Service

@Service
class UpdateCurrency(
    private val currencyRepository: CurrencyRepository,
) {
    suspend operator fun invoke(command: Command): Currency {
        val pathCode = command.pathCode.trim().uppercase()
        val currency = Currency(
            code = command.code,
            name = command.name,
            symbol = command.symbol,
            decimalPlaces = command.decimalPlaces,
            symbolPosition = command.symbolPosition,
        )

        if (pathCode != currency.code) {
            throw ValidationException(
                code = "validation_error",
                message = "Path code must match body code",
                details = mapOf(
                    "pathCode" to pathCode,
                    "bodyCode" to currency.code,
                ),
            )
        }

        if (currencyRepository.findByCode(pathCode) == null) {
            throw NotFoundException(
                code = "not_found",
                message = "Currency not found",
                details = mapOf("code" to pathCode),
            )
        }

        return currencyRepository.update(currency)
    }

    data class Command(
        val pathCode: String,
        val code: String,
        val name: String,
        val symbol: String,
        val decimalPlaces: Int,
        val symbolPosition: String,
    )
}
