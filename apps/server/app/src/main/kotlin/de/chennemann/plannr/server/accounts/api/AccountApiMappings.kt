package de.chennemann.plannr.server.accounts.api

import de.chennemann.plannr.server.accounts.api.dto.AccountResponse
import de.chennemann.plannr.server.accounts.api.dto.CreateAccountRequest
import de.chennemann.plannr.server.accounts.api.dto.UpdateAccountRequest
import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.usecases.CreateAccount
import de.chennemann.plannr.server.accounts.usecases.UpdateAccount

internal fun CreateAccountRequest.toCommand(): CreateAccount.Command =
    CreateAccount.Command(
        name = name,
        institution = institution,
        currencyCode = currencyCode,
        weekendHandling = weekendHandling,
    )

internal fun UpdateAccountRequest.toCommand(id: String): UpdateAccount.Command =
    UpdateAccount.Command(
        id = id,
        name = name,
        institution = institution,
        currencyCode = currencyCode,
        weekendHandling = weekendHandling,
    )

internal fun Account.toResponse(): AccountResponse =
    AccountResponse(
        id = id,
        name = name,
        institution = institution,
        currencyCode = currencyCode,
        weekendHandling = weekendHandling,
        isArchived = isArchived,
        createdAt = createdAt,
    )
