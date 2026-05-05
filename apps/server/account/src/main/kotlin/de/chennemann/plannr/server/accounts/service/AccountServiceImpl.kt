package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.api.dto.CreateAccountCommand
import de.chennemann.plannr.server.accounts.api.dto.UpdateAccountCommand
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.persistence.AccountModel
import de.chennemann.plannr.server.accounts.persistence.toDomain
import de.chennemann.plannr.server.common.domain.normalizeCurrency
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.time.TimeProvider
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
internal class AccountServiceImpl(
    private val accountRepository: AccountRepository,
    private val archiveCascade: AccountArchiveCascade,
    private val timeProvider: TimeProvider,
) : AccountService {
    override suspend fun create(command: CreateAccountCommand): Account {
        val currencyCode = normalizeCurrency(command.currencyCode)
        val created = accountRepository.save(
            AccountModel(
                id = null,
                name = command.name,
                institution = command.institution,
                currencyCode = currencyCode,
                weekendHandling = command.weekendHandling,
                isArchived = false,
                createdAt = timeProvider(),
            ),
        ).toDomain()
        return created
    }

    override suspend fun update(command: UpdateAccountCommand): Account {
        val existing = existingAccount(command.id)
        val currencyCode = normalizeCurrency(command.currencyCode)
        val persisted = accountRepository.save(
            AccountModel(
                id = existing.id,
                name = command.name,
                institution = command.institution,
                currencyCode = currencyCode,
                weekendHandling = command.weekendHandling,
                isArchived = existing.isArchived,
                createdAt = existing.createdAt,
            ),
        ).toDomain()
        return persisted
    }

    override suspend fun archive(id: String): Account {
        val existing = existingAccount(id)
        val updated = accountRepository.save(
            AccountModel(
                id = existing.id,
                name = existing.name,
                institution = existing.institution,
                currencyCode = existing.currencyCode,
                weekendHandling = existing.weekendHandling,
                isArchived = true,
                createdAt = existing.createdAt,
            ),
        ).toDomain()
        archiveCascade.archiveFor(updated)
        return updated
    }

    override suspend fun unarchive(id: String): Account {
        val existing = existingAccount(id)
        val updated = accountRepository.save(
            AccountModel(
                id = existing.id,
                name = existing.name,
                institution = existing.institution,
                currencyCode = existing.currencyCode,
                weekendHandling = existing.weekendHandling,
                isArchived = false,
                createdAt = existing.createdAt,
            ),
        ).toDomain()
        archiveCascade.unarchiveFor(updated)
        return updated
    }

    override suspend fun delete(id: String) {
        val normalizedId = existingAccount(id).id
        accountRepository.deleteById(normalizedId)
    }

    override suspend fun list(archived: Boolean?): List<Account> =
        accountRepository.findAllByOrderByCreatedAtAscIdAsc()
            .toList()
            .map(AccountModel::toDomain)
            .filter { archived == null || it.isArchived == archived }

    override suspend fun getById(id: String): Account? =
        accountRepository.findById(id.trim())?.toDomain()

    private suspend fun existingAccount(id: String): Account =
        accountRepository.findById(id.trim())?.toDomain()
            ?: throw NotFoundException(
                code = "not_found",
                message = "Account not found",
                details = mapOf("id" to id.trim()),
            )
}
