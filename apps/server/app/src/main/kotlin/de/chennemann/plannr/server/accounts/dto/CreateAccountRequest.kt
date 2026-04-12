package de.chennemann.plannr.server.accounts.dto

import de.chennemann.plannr.server.accounts.usecases.CreateAccount

data class CreateAccountRequest(
    val name: String,
    val institution: String,
    val currencyCode: String,
    val weekendHandling: String,
) {
    fun toCommand(): CreateAccount.Command =
        CreateAccount.Command(
            name = name,
            institution = institution,
            currencyCode = currencyCode,
            weekendHandling = weekendHandling,
        )
}
