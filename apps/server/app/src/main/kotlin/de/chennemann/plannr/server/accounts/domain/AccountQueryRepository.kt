package de.chennemann.plannr.server.accounts.domain

interface AccountQueryRepository {
    suspend fun saveOrUpdate(accountQuery: AccountQuery): AccountQuery
    suspend fun findById(accountId: String): AccountQuery?
    suspend fun findAll(archived: Boolean = false): List<AccountQuery>
}
