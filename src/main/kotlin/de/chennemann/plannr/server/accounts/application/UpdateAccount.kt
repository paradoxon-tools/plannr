package de.chennemann.plannr.server.accounts.application

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.common.error.NotFoundException
import org.springframework.stereotype.Service

@Service
class UpdateAccount(
    private val accountRepository: AccountRepository,
    private val ensureCurrencyExists: EnsureCurrencyExists,
) {
    suspend operator fun invoke(command: Command): Account {
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

    data class Command(
        val id: String,
        val name: String,
        val institution: String,
        val currencyCode: String,
        val weekendHandling: String,
    )
}
