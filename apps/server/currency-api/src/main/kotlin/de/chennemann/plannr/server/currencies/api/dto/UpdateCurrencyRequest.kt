package de.chennemann.plannr.server.currencies.api.dto

data class UpdateCurrencyRequest(
    val code: String,
    val name: String,
    val symbol: String,
    val decimalPlaces: Int,
    val symbolPosition: String,
)
