package de.chennemann.plannr.server.accounts.usecases

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.support.AccountIdGenerator
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.currencies.usecases.EnsureCurrencyExists
import org.springframework.stereotype.Component

interface CreateAccount {
    suspend operator fun invoke(command: Command): Account

    data class Command(
        val name: String,
        val institution: String,
        val currencyCode: String,
        val weekendHandling: String,
    )
}

@Component
internal class CreateAccountUseCase(
    private val accountRepository: AccountRepository,
    private val ensureCurrencyExists: EnsureCurrencyExists,
    private val accountIdGenerator: AccountIdGenerator,
    private val timeProvider: TimeProvider,
) : CreateAccount {
    override suspend fun invoke(command: CreateAccount.Command): Account {
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
}
