package de.chennemann.plannr.server.accounts.api

import de.chennemann.plannr.server.accounts.api.dto.AccountResponse
import de.chennemann.plannr.server.accounts.api.dto.CreateAccountRequest
import de.chennemann.plannr.server.accounts.api.dto.UpdateAccountRequest
import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.service.CreateAccountCommand
import de.chennemann.plannr.server.accounts.service.UpdateAccountCommand

internal fun CreateAccountRequest.toCommand(): CreateAccountCommand =
    CreateAccountCommand(
        name = name,
        institution = institution,
        currencyCode = currencyCode,
        weekendHandling = weekendHandling,
    )

internal fun UpdateAccountRequest.toCommand(id: String): UpdateAccountCommand =
    UpdateAccountCommand(
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
