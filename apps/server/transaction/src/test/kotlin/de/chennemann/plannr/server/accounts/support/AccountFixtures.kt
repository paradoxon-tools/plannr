package de.chennemann.plannr.server.accounts.support

import de.chennemann.plannr.server.accounts.api.dto.CreateAccountRequest
import de.chennemann.plannr.server.accounts.api.dto.UpdateAccountRequest
import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.service.CreateAccountCommand
import de.chennemann.plannr.server.accounts.service.UpdateAccountCommand

object AccountFixtures {
    const val DEFAULT_ID = "acc_123"
    const val DEFAULT_NAME = "Main Account"
    const val DEFAULT_INSTITUTION = "Demo Bank"
    const val DEFAULT_CURRENCY_CODE = "EUR"
    const val DEFAULT_WEEKEND_HANDLING = "MOVE_AFTER"
    const val DEFAULT_CREATED_AT = 1_710_000_000L

    fun account(
        id: String = DEFAULT_ID,
        name: String = DEFAULT_NAME,
        institution: String = DEFAULT_INSTITUTION,
        currencyCode: String = DEFAULT_CURRENCY_CODE,
        weekendHandling: String = DEFAULT_WEEKEND_HANDLING,
        isArchived: Boolean = false,
        createdAt: Long = DEFAULT_CREATED_AT,
    ): Account =
        Account(id, name, institution, currencyCode, weekendHandling, isArchived, createdAt)

    fun createAccountCommand(
        name: String = DEFAULT_NAME,
        institution: String = DEFAULT_INSTITUTION,
        currencyCode: String = DEFAULT_CURRENCY_CODE,
        weekendHandling: String = DEFAULT_WEEKEND_HANDLING,
    ): CreateAccountCommand =
        CreateAccountCommand(name, institution, currencyCode, weekendHandling)

    fun createAccountRequest(
        name: String = DEFAULT_NAME,
        institution: String = DEFAULT_INSTITUTION,
        currencyCode: String = DEFAULT_CURRENCY_CODE,
        weekendHandling: String = DEFAULT_WEEKEND_HANDLING,
    ): CreateAccountRequest =
        CreateAccountRequest(name, institution, currencyCode, weekendHandling)

    fun updateAccountCommand(
        id: String = DEFAULT_ID,
        name: String = DEFAULT_NAME,
        institution: String = DEFAULT_INSTITUTION,
        currencyCode: String = DEFAULT_CURRENCY_CODE,
        weekendHandling: String = DEFAULT_WEEKEND_HANDLING,
    ): UpdateAccountCommand =
        UpdateAccountCommand(id, name, institution, currencyCode, weekendHandling)

    fun updateAccountRequest(
        name: String = DEFAULT_NAME,
        institution: String = DEFAULT_INSTITUTION,
        currencyCode: String = DEFAULT_CURRENCY_CODE,
        weekendHandling: String = DEFAULT_WEEKEND_HANDLING,
    ): UpdateAccountRequest =
        UpdateAccountRequest(name, institution, currencyCode, weekendHandling)
}
