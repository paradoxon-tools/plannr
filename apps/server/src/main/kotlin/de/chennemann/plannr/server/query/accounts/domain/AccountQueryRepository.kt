package de.chennemann.plannr.server.query.accounts.domain

interface AccountQueryRepository {
    suspend fun saveOrUpdate(accountQuery: AccountQuery): AccountQuery
    suspend fun findById(accountId: String): AccountQuery?
}
