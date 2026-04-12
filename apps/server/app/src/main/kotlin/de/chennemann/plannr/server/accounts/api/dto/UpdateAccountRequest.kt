package de.chennemann.plannr.server.accounts.api.dto

import de.chennemann.plannr.server.accounts.usecases.UpdateAccount

data class UpdateAccountRequest(
    val name: String,
    val institution: String,
    val currencyCode: String,
    val weekendHandling: String,
) {
    fun toCommand(id: String): UpdateAccount.Command =
        UpdateAccount.Command(
            id = id,
            name = name,
            institution = institution,
            currencyCode = currencyCode,
            weekendHandling = weekendHandling,
        )
}
