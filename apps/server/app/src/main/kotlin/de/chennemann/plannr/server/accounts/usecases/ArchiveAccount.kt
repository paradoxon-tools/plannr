package de.chennemann.plannr.server.accounts.usecases

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.events.AccountUpdated
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.events.ApplicationEventBus
import de.chennemann.plannr.server.common.events.NoOpApplicationEventBus
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransactionRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

interface ArchiveAccount {
    suspend operator fun invoke(id: String): Account
}

@Component
@Transactional
internal class ArchiveAccountUseCase(
    private val accountRepository: AccountRepository,
    private val pocketService: PocketService,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val applicationEventBus: ApplicationEventBus = NoOpApplicationEventBus,
) : ArchiveAccount {
    override suspend fun invoke(id: String): Account {
        val accountId = id.trim()
        val existing = accountRepository.findById(accountId)
            ?: throw NotFoundException(
                code = "not_found",
                message = "Account not found",
                details = mapOf("id" to accountId),
            )

        val updatedAccount = accountRepository.update(existing.archive())

        pocketService.list(accountId = accountId).forEach { pocketService.archive(it.id) }
        recurringTransactionRepository.findAll(accountId = accountId, archived = false)
            .forEach { recurringTransactionRepository.update(it.archive()) }

        applicationEventBus.publish(AccountUpdated(existing, updatedAccount))
        return updatedAccount
    }
}
