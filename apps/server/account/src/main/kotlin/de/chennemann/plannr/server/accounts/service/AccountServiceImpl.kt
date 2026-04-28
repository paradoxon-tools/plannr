package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountQuery
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.events.AccountCreated
import de.chennemann.plannr.server.accounts.events.AccountUpdated
import de.chennemann.plannr.server.accounts.persistence.AccountModel
import de.chennemann.plannr.server.accounts.persistence.toModel
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.events.ApplicationEventBus
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.currencies.service.CurrencyService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
internal class AccountServiceImpl(
    private val accountRepository: AccountRepository,
    private val currencyService: CurrencyService,
    private val archiveCascade: AccountArchiveCascade,
    private val balanceProvider: AccountBalanceProvider,
    private val timeProvider: TimeProvider,
    private val applicationEventBus: ApplicationEventBus,
) : AccountService {
    override suspend fun create(command: CreateAccountCommand): Account {
        val currency = currencyService.ensureExists(command.currencyCode)
        val created = accountRepository.save(
            AccountModel(
                id = null,
                name = command.name,
                institution = command.institution,
                currencyCode = currency.code,
                weekendHandling = command.weekendHandling,
                isArchived = false,
                createdAt = timeProvider(),
            ),
        )
        applicationEventBus.publish(AccountCreated(created))
        return created
    }

    override suspend fun update(command: UpdateAccountCommand): Account {
        val existing = existingAccount(command.id)
        val currency = currencyService.ensureExists(command.currencyCode)
        val persisted = accountRepository.update(
            Account(
                id = existing.id,
                name = command.name,
                institution = command.institution,
                currencyCode = currency.code,
                weekendHandling = command.weekendHandling,
                isArchived = existing.isArchived,
                createdAt = existing.createdAt,
            ).toModel(),
        )
        applicationEventBus.publish(AccountUpdated(existing, persisted))
        return persisted
    }

    override suspend fun archive(id: String): Account {
        val existing = existingAccount(id)
        val updated = accountRepository.update(existing.archive().toModel())
        archiveCascade.archiveFor(updated)
        applicationEventBus.publish(AccountUpdated(existing, updated))
        return updated
    }

    override suspend fun unarchive(id: String): Account {
        val existing = existingAccount(id)
        val updated = accountRepository.update(existing.unarchive().toModel())
        archiveCascade.unarchiveFor(updated)
        applicationEventBus.publish(AccountUpdated(existing, updated))
        return updated
    }

    override suspend fun list(archived: Boolean?): List<Account> =
        accountRepository.findAll()
            .filter { archived == null || it.isArchived == archived }

    override suspend fun getById(id: String): Account? =
        accountRepository.findById(id.trim())

    override suspend fun listQueries(archived: Boolean): List<AccountQuery> =
        list(archived = archived).map { it.toQuery(balanceProvider.currentBalance(it.id)) }

    override suspend fun getQuery(id: String): AccountQuery =
        existingAccount(id).toQuery(balanceProvider.currentBalance(id.trim()))

    private suspend fun existingAccount(id: String): Account =
        accountRepository.findById(id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Account not found",
                details = mapOf("id" to id.trim()),
            )

    private fun Account.toQuery(currentBalance: Long): AccountQuery =
        AccountQuery(
            accountId = id,
            name = name,
            institution = institution,
            currencyCode = currencyCode,
            weekendHandling = weekendHandling,
            isArchived = isArchived,
            createdAt = createdAt,
            currentBalance = currentBalance,
        )
}
