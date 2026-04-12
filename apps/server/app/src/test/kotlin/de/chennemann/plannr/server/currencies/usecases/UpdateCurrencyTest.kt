package de.chennemann.plannr.server.currencies.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.currencies.support.CurrencyFixtures
import de.chennemann.plannr.server.currencies.support.InMemoryCurrencyRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateCurrencyTest {
    @Test
    fun `updates an existing currency`() = runTest {
        val repository = InMemoryCurrencyRepository()
        repository.save(CurrencyFixtures.currency())
        val updateCurrency = UpdateCurrencyUseCase(repository)

        val updated = updateCurrency(
            CurrencyFixtures.updateCurrencyCommand(
                pathCode = "eur",
                code = "eur",
                name = "Euro Updated",
                symbol = "EUR",
                decimalPlaces = 3,
                symbolPosition = "after",
            ),
        )

        assertEquals("EUR", updated.code)
        assertEquals("Euro Updated", updated.name)
        assertEquals("EUR", updated.symbol)
        assertEquals(3, updated.decimalPlaces)
        assertEquals("after", updated.symbolPosition)
    }

    @Test
    fun `normalizes updated currency code to uppercase`() = runTest {
        val repository = InMemoryCurrencyRepository()
        repository.save(CurrencyFixtures.currency())
        val updateCurrency = UpdateCurrencyUseCase(repository)

        val updated = updateCurrency(
            CurrencyFixtures.updateCurrencyCommand(
                pathCode = "eur",
                code = "eur",
                name = "Euro Canonical",
            ),
        )

        assertEquals("EUR", updated.code)
        assertEquals(updated, repository.findByCode("EUR"))
    }

    @Test
    fun `rejects code mismatch between path and body`() = runTest {
        val repository = InMemoryCurrencyRepository()
        repository.save(CurrencyFixtures.currency())
        val updateCurrency = UpdateCurrencyUseCase(repository)

        assertFailsWith<ValidationException> {
            updateCurrency(
                CurrencyFixtures.updateCurrencyCommand(
                    pathCode = "eur",
                    code = "usd",
                ),
            )
        }
    }

    @Test
    fun `returns not found when currency does not exist`() = runTest {
        val repository = InMemoryCurrencyRepository()
        val updateCurrency = UpdateCurrencyUseCase(repository)

        assertFailsWith<NotFoundException> {
            updateCurrency(
                CurrencyFixtures.updateCurrencyCommand(
                    pathCode = "eur",
                    code = "eur",
                    name = "Euro Updated",
                    symbol = "EUR",
                    decimalPlaces = 3,
                    symbolPosition = "after",
                ),
            )
        }
    }
}
