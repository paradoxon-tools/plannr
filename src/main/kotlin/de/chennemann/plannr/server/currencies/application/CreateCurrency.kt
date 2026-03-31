package de.chennemann.plannr.server.currencies.application

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.domain.CurrencyRepository
import org.springframework.stereotype.Service

@Service
class CreateCurrency(
    private val currencyRepository: CurrencyRepository,
) {
    suspend operator fun invoke(command: Command): Currency {
        val currency = Currency(
            code = command.code,
            name = command.name,
            symbol = command.symbol,
            decimalPlaces = command.decimalPlaces,
            symbolPosition = command.symbolPosition,
        )

        if (currencyRepository.findByCode(currency.code) != null) {
            throw ConflictException(
                code = "conflict",
                message = "Currency already exists",
                details = mapOf("code" to currency.code),
            )
        }

        return currencyRepository.save(currency)
    }

    data class Command(
        val code: String,
        val name: String,
        val symbol: String,
        val decimalPlaces: Int,
        val symbolPosition: String,
    )
}
