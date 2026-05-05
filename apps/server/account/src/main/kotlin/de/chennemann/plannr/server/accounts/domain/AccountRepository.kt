package de.chennemann.plannr.server.accounts.domain

import de.chennemann.plannr.server.accounts.persistence.AccountModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface AccountRepository : CoroutineCrudRepository<AccountModel, String> {
    fun findAllByOrderByCreatedAtAscIdAsc(): Flow<AccountModel>
}
