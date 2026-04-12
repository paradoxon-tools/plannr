package de.chennemann.plannr.server.query.accounts.api

import de.chennemann.plannr.server.query.accounts.domain.AccountQuery

data class AccountQueryResponse(
    val id: String,
    val name: String,
    val institution: String,
    val currencyCode: String,
    val weekendHandling: String,
    val isArchived: Boolean,
    val createdAt: Long,
    val currentBalance: Long,
) {
    companion object {
        fun from(accountQuery: AccountQuery): AccountQueryResponse = AccountQueryResponse(
            id = accountQuery.accountId,
            name = accountQuery.name,
            institution = accountQuery.institution,
            currencyCode = accountQuery.currencyCode,
            weekendHandling = accountQuery.weekendHandling,
            isArchived = accountQuery.isArchived,
            createdAt = accountQuery.createdAt,
            currentBalance = accountQuery.currentBalance,
        )
    }
}
