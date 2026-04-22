package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountQuery

interface AccountService {
    suspend fun create(command: CreateAccountCommand): Account
    suspend fun update(command: UpdateAccountCommand): Account
    suspend fun archive(id: String): Account
    suspend fun unarchive(id: String): Account
    suspend fun list(archived: Boolean? = null): List<Account>
    suspend fun getById(id: String): Account?
    suspend fun listQueries(archived: Boolean = false): List<AccountQuery>
    suspend fun getQuery(id: String): AccountQuery
}

data class CreateAccountCommand(
    val name: String,
    val institution: String,
    val currencyCode: String,
    val weekendHandling: String,
)

data class UpdateAccountCommand(
    val id: String,
    val name: String,
    val institution: String,
    val currencyCode: String,
    val weekendHandling: String,
)
