package de.chennemann.plannr.server.accounts.application

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import org.springframework.stereotype.Service

@Service
class CreateAccount(
    private val accountRepository: AccountRepository,
    private val ensureCurrencyExists: EnsureCurrencyExists,
    private val accountIdGenerator: AccountIdGenerator,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(command: Command): Account {
        val currency = ensureCurrencyExists(command.currencyCode)
        val account = Account(
            id = accountIdGenerator(),
            name = command.name,
            institution = command.institution,
            currencyCode = currency.code,
            weekendHandling = command.weekendHandling,
            isArchived = false,
            createdAt = timeProvider(),
        )

        return accountRepository.save(account)
    }

    data class Command(
        val name: String,
        val institution: String,
        val currencyCode: String,
        val weekendHandling: String,
    )
}
