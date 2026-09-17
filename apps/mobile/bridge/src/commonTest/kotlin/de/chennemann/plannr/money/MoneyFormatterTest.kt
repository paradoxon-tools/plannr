package de.chennemann.plannr.money

import de.chennemann.plannr.data.Amount
import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyFormatterTest {
    @Test
    fun `formats euros with two decimal places and the euro symbol`() {
        assertEquals("0,00 €", MoneyFormatter.format(0, "EUR"))
        assertEquals("123,45 €", MoneyFormatter.format(12_345, "EUR"))
    }

    @Test
    fun `preserves the sign for negative euro amounts below one euro`() {
        assertEquals("-0,05 €", MoneyFormatter.format(-5, "EUR"))
        assertEquals("-1,23 €", MoneyFormatter.format(-123, "EUR"))
    }

    @Test
    fun `formats amount values and normalizes their currency code`() {
        assertEquals("12,34 €", MoneyFormatter.format(Amount(1_234, "eur")))
        assertEquals("12.34 USD", MoneyFormatter.format(Amount(1_234, "usd")))
    }
}
