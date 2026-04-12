package de.chennemann.plannr.server.currencies.api.dto

data class CreateCurrencyRequest(
    val code: String,
    val name: String,
    val symbol: String,
    val decimalPlaces: Int,
    val symbolPosition: String,
)
