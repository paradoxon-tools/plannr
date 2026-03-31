package de.chennemann.plannr.server.currencies.usecases

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.domain.CurrencyRepository
import org.springframework.stereotype.Component

interface CreateCurrency {
    suspend operator fun invoke(command: Command): Currency

    data class Command(
        val code: String,
        val name: String,
        val symbol: String,
        val decimalPlaces: Int,
        val symbolPosition: String,
    )
}

@Component
internal class CreateCurrencyUseCase(
    private val currencyRepository: CurrencyRepository,
) : CreateCurrency {
    override suspend fun invoke(command: CreateCurrency.Command): Currency {
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
}
