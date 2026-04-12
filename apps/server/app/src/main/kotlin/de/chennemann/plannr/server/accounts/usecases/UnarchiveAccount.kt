package de.chennemann.plannr.server.accounts.usecases

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.events.AccountUpdated
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.events.ApplicationEventBus
import de.chennemann.plannr.server.common.events.NoOpApplicationEventBus
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.events.PocketUpdated
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransactionRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

interface UnarchiveAccount {
    suspend operator fun invoke(id: String): Account
}

@Component
@Transactional
internal class UnarchiveAccountUseCase(
    private val accountRepository: AccountRepository,
    private val pocketRepository: PocketRepository,
    private val contractRepository: ContractRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val applicationEventBus: ApplicationEventBus = NoOpApplicationEventBus,
) : UnarchiveAccount {
    override suspend fun invoke(id: String): Account {
        val accountId = id.trim()
        val existing = accountRepository.findById(accountId)
            ?: throw NotFoundException(
                code = "not_found",
                message = "Account not found",
                details = mapOf("id" to accountId),
            )

        val updatedAccount = accountRepository.update(existing.unarchive())

        pocketRepository.findAll(accountId = accountId).forEach { pocket ->
            val updatedPocket = pocketRepository.update(pocket.unarchive())
            contractRepository.findByPocketId(updatedPocket.id)?.let { contractRepository.update(it.unarchive()) }
            applicationEventBus.publish(PocketUpdated(pocket, updatedPocket))
        }
        recurringTransactionRepository.findAll(accountId = accountId, archived = true)
            .forEach { recurringTransactionRepository.update(it.unarchive()) }

        applicationEventBus.publish(AccountUpdated(existing, updatedAccount))
        return updatedAccount
    }
}
