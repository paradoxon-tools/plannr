package de.chennemann.plannr.money

import de.chennemann.plannr.data.Amount

object MoneyFormatter {
    fun format(amount: Amount): String = format(amount.amount, amount.currency)

    fun format(minorUnits: Long, currencyCode: String): String {
        val normalizedCurrencyCode = currencyCode.uppercase()
        val majorUnits = minorUnits / 100
        val minorUnitRemainder = minorUnits % 100
        val absoluteMajorUnits = if (majorUnits < 0) -majorUnits else majorUnits
        val absoluteMinorUnits = if (minorUnitRemainder < 0) -minorUnitRemainder else minorUnitRemainder
        val sign = if (minorUnits < 0) "-" else ""
        val decimalSeparator = if (normalizedCurrencyCode == "EUR") ',' else '.'
        val currencyDisplay = if (normalizedCurrencyCode == "EUR") "€" else normalizedCurrencyCode

        return "$sign$absoluteMajorUnits$decimalSeparator${absoluteMinorUnits.toString().padStart(2, '0')} $currencyDisplay"
    }
}
