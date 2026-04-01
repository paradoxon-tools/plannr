package de.chennemann.plannr.server.accounts.usecases

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.currencies.usecases.EnsureCurrencyExists
import org.springframework.stereotype.Component

interface UpdateAccount {
    suspend operator fun invoke(command: Command): Account

    data class Command(
        val id: String,
        val name: String,
        val institution: String,
        val currencyCode: String,
        val weekendHandling: String,
    )
}

@Component
internal class UpdateAccountUseCase(
    private val accountRepository: AccountRepository,
    private val ensureCurrencyExists: EnsureCurrencyExists,
) : UpdateAccount {
    override suspend fun invoke(command: UpdateAccount.Command): Account {
        val existing = accountRepository.findById(command.id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Account not found",
                details = mapOf("id" to command.id.trim()),
            )

        val currency = ensureCurrencyExists(command.currencyCode)
        val updated = Account(
            id = existing.id,
            name = command.name,
            institution = command.institution,
            currencyCode = currency.code,
            weekendHandling = command.weekendHandling,
            isArchived = existing.isArchived,
            createdAt = existing.createdAt,
        )

        return accountRepository.update(updated)
    }
}
