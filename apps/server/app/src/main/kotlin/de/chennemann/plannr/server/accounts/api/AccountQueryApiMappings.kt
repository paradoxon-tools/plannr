package de.chennemann.plannr.server.accounts.api

import de.chennemann.plannr.server.query.accounts.api.dto.AccountQueryResponse
import de.chennemann.plannr.server.accounts.domain.AccountQuery

internal fun AccountQuery.toResponse(): AccountQueryResponse =
    AccountQueryResponse(
        id = accountId,
        name = name,
        institution = institution,
        currencyCode = currencyCode,
        weekendHandling = weekendHandling,
        isArchived = isArchived,
        createdAt = createdAt,
        currentBalance = currentBalance,
    )
