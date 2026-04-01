package de.chennemann.plannr.server.currencies.domain

import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.currencies.support.CurrencyFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CurrencyTest {
    @Test
    fun `normalizes currency code and symbol position`() {
        val currency = CurrencyFixtures.currency(
            code = " eur ",
            symbolPosition = " BEFORE ",
        )

        assertEquals("EUR", currency.code)
        assertEquals("before", currency.symbolPosition)
    }

    @Test
    fun `trims name and symbol`() {
        val currency = CurrencyFixtures.currency(
            name = " Euro ",
            symbol = " € ",
        )

        assertEquals("Euro", currency.name)
        assertEquals("€", currency.symbol)
    }

    @Test
    fun `rejects blank code`() {
        assertFailsWith<ValidationException> {
            CurrencyFixtures.currency(code = "   ")
        }
    }

    @Test
    fun `rejects blank name`() {
        assertFailsWith<ValidationException> {
            CurrencyFixtures.currency(name = "   ")
        }
    }

    @Test
    fun `rejects blank symbol`() {
        assertFailsWith<ValidationException> {
            CurrencyFixtures.currency(symbol = "   ")
        }
    }

    @Test
    fun `rejects blank symbol position`() {
        assertFailsWith<ValidationException> {
            CurrencyFixtures.currency(symbolPosition = "   ")
        }
    }

    @Test
    fun `rejects negative decimal places`() {
        assertFailsWith<ValidationException> {
            CurrencyFixtures.currency(decimalPlaces = -1)
        }
    }
}
