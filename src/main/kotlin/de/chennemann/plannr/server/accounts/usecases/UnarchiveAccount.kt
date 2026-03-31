package de.chennemann.plannr.server.accounts.usecases

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import org.springframework.stereotype.Component

interface UnarchiveAccount {
    suspend operator fun invoke(id: String): Account
}

@Component
internal class UnarchiveAccountUseCase(
    private val accountRepository: AccountRepository,
    private val pocketRepository: PocketRepository,
    private val contractRepository: ContractRepository,
) : UnarchiveAccount {
    override suspend fun invoke(id: String): Account {
        val accountId = id.trim()
        val existing = accountRepository.findById(accountId)
            ?: throw NotFoundException(
                code = "not_found",
                message = "Account not found",
                details = mapOf("id" to accountId),
            )

        val updatedAccount = existing.copy(isArchived = false)
        accountRepository.update(updatedAccount)

        pocketRepository.findAll(accountId = accountId).forEach { pocket ->
            val updatedPocket = pocket.copy(isArchived = false)
            pocketRepository.update(updatedPocket)
            contractRepository.findByPocketId(updatedPocket.id)?.let { contractRepository.update(it.copy(isArchived = false)) }
        }

        return updatedAccount
    }
}
