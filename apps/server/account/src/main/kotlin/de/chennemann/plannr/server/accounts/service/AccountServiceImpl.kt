package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountQuery
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.events.AccountCreated
import de.chennemann.plannr.server.accounts.events.AccountUpdated
import de.chennemann.plannr.server.accounts.persistence.AccountModel
import de.chennemann.plannr.server.accounts.persistence.toDomain
import de.chennemann.plannr.server.common.domain.normalizeCurrency
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.events.ApplicationEventBus
import de.chennemann.plannr.server.common.time.TimeProvider
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
internal class AccountServiceImpl(
    private val accountRepository: AccountRepository,
    private val archiveCascade: AccountArchiveCascade,
    private val balanceProvider: AccountBalanceProvider,
    private val timeProvider: TimeProvider,
    private val applicationEventBus: ApplicationEventBus,
) : AccountService {
    override suspend fun create(command: CreateAccountCommand): Account {
        val currencyCode = normalizeCurrency(command.currencyCode)
        val created = accountRepository.insert(
            id = null,
            name = command.name,
            institution = command.institution,
            currencyCode = currencyCode,
            weekendHandling = command.weekendHandling,
            isArchived = false,
            createdAt = timeProvider(),
        ).toDomain()
        applicationEventBus.publish(AccountCreated(created))
        return created
    }

    override suspend fun update(command: UpdateAccountCommand): Account {
        val existing = existingAccount(command.id)
        val currencyCode = normalizeCurrency(command.currencyCode)
        val persisted = accountRepository.update(
            id = existing.id,
            name = command.name,
            institution = command.institution,
            currencyCode = currencyCode,
            weekendHandling = command.weekendHandling,
            isArchived = existing.isArchived,
        ).toDomain()
        applicationEventBus.publish(AccountUpdated(existing, persisted))
        return persisted
    }

    override suspend fun archive(id: String): Account {
        val existing = existingAccount(id)
        val updated = accountRepository.update(
            id = existing.id,
            name = existing.name,
            institution = existing.institution,
            currencyCode = existing.currencyCode,
            weekendHandling = existing.weekendHandling,
            isArchived = true,
        ).toDomain()
        archiveCascade.archiveFor(updated)
        applicationEventBus.publish(AccountUpdated(existing, updated))
        return updated
    }

    override suspend fun unarchive(id: String): Account {
        val existing = existingAccount(id)
        val updated = accountRepository.update(
            id = existing.id,
            name = existing.name,
            institution = existing.institution,
            currencyCode = existing.currencyCode,
            weekendHandling = existing.weekendHandling,
            isArchived = false,
        ).toDomain()
        archiveCascade.unarchiveFor(updated)
        applicationEventBus.publish(AccountUpdated(existing, updated))
        return updated
    }

    override suspend fun list(archived: Boolean?): List<Account> =
        accountRepository.findAllByOrderByCreatedAtAscIdAsc()
            .toList()
            .map(AccountModel::toDomain)
            .filter { archived == null || it.isArchived == archived }

    override suspend fun getById(id: String): Account? =
        accountRepository.findById(id.trim())?.toDomain()

    override suspend fun listQueries(archived: Boolean): List<AccountQuery> =
        list(archived = archived).map { it.toQuery(balanceProvider.currentBalance(it.id)) }

    override suspend fun getQuery(id: String): AccountQuery =
        existingAccount(id).toQuery(balanceProvider.currentBalance(id.trim()))

    private suspend fun existingAccount(id: String): Account =
        accountRepository.findById(id.trim())?.toDomain()
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
