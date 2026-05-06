package de.chennemann.plannr.server.accounts.domain

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.persistence.AccountModel
import de.chennemann.plannr.server.accounts.persistence.toDomain
import de.chennemann.plannr.server.accounts.persistence.toModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface AccountRepository : CoroutineCrudRepository<AccountModel, Long> {
    fun findAllByOrderByCreatedAtAscIdAsc(): Flow<AccountModel>

    suspend fun findByNameAndInstitution(name: String, institution: String): AccountModel?
}

suspend fun AccountRepository.save(account: Account): Account =
    save(account.toModel()).toDomain()
