package de.chennemann.plannr.server.currencies.service

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.domain.CurrencyRepository
import de.chennemann.plannr.server.currencies.domain.CurrencyTemplateCatalog
import org.springframework.stereotype.Component

@Component
internal class CurrencyServiceImpl(
    private val currencyRepository: CurrencyRepository,
    private val currencyTemplateCatalog: CurrencyTemplateCatalog,
) : CurrencyService {
    override suspend fun create(command: CreateCurrencyCommand): Currency {
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

    override suspend fun update(command: UpdateCurrencyCommand): Currency {
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

    override suspend fun list(): List<Currency> =
        currencyRepository.findAll()

    override suspend fun ensureExists(currencyCode: String): Currency {
        val normalizedCode = currencyCode.trim().uppercase()

        currencyRepository.findByCode(normalizedCode)?.let { return it }

        val template = currencyTemplateCatalog.findByCode(normalizedCode)
            ?: throw NotFoundException(
                code = "not_found",
                message = "Currency not found",
                details = mapOf("code" to normalizedCode),
            )

        return currencyRepository.save(template)
    }
}
