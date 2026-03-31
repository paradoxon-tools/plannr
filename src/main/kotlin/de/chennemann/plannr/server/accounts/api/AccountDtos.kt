package de.chennemann.plannr.server.accounts.api

import de.chennemann.plannr.server.accounts.usecases.CreateAccount
import de.chennemann.plannr.server.accounts.usecases.UpdateAccount
import de.chennemann.plannr.server.accounts.domain.Account

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

data class AccountResponse(
    val id: String,
    val name: String,
    val institution: String,
    val currencyCode: String,
    val weekendHandling: String,
    val isArchived: Boolean,
    val createdAt: Long,
) {
    companion object {
        fun from(account: Account): AccountResponse =
            AccountResponse(
                id = account.id,
                name = account.name,
                institution = account.institution,
                currencyCode = account.currencyCode,
                weekendHandling = account.weekendHandling,
                isArchived = account.isArchived,
                createdAt = account.createdAt,
            )
    }
}
