package de.chennemann.plannr.server.accounts.usecases

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransactionRepository
import org.springframework.stereotype.Component

interface UnarchiveAccount {
    suspend operator fun invoke(id: String): Account
}

@Component
internal class UnarchiveAccountUseCase(
    private val accountRepository: AccountRepository,
    private val pocketRepository: PocketRepository,
    private val contractRepository: ContractRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
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
        recurringTransactionRepository.findAll(accountId = accountId, archived = true)
            .forEach { recurringTransactionRepository.update(it.copy(isArchived = false)) }

        return updatedAccount
    }
}
