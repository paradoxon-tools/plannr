package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.api.dto.CreateAccountCommand
import de.chennemann.plannr.server.accounts.api.dto.UpdateAccountCommand
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.persistence.AccountModel
import de.chennemann.plannr.server.accounts.persistence.toDomain
import de.chennemann.plannr.server.common.domain.normalizeCurrency
import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.pockets.service.PocketService
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
internal class AccountServiceImpl(
    private val accountRepository: AccountRepository,
    private val pocketService: PocketService,
    private val timeProvider: TimeProvider,
) : AccountService {
    override suspend fun create(command: CreateAccountCommand): Account {
        ensureNameAvailable(name = command.name, institution = command.institution, currentAccountId = null)
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
        ensureNameAvailable(name = command.name, institution = command.institution, currentAccountId = existing.id)
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

    override suspend fun archive(id: Long): Account {
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
        pocketService.archiveForAccount(updated.id)
        return updated
    }

    override suspend fun unarchive(id: Long): Account {
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
        pocketService.unarchiveForAccount(updated.id)
        return updated
    }

    override suspend fun delete(id: Long) {
        val normalizedId = existingAccount(id).id
        accountRepository.deleteById(normalizedId)
    }

    override suspend fun list(archived: Boolean?): List<Account> =
        accountRepository.findAllByOrderByCreatedAtAscIdAsc()
            .toList()
            .map(AccountModel::toDomain)
            .filter { archived == null || it.isArchived == archived }

    override suspend fun getById(id: Long): Account? =
        accountRepository.findById(id)?.toDomain()

    private suspend fun existingAccount(id: Long): Account =
        accountRepository.findById(id)?.toDomain()
            ?: throw NotFoundException(
                code = "not_found",
                message = "Account not found",
                details = mapOf("id" to id),
            )

    private suspend fun ensureNameAvailable(name: String, institution: String, currentAccountId: Long?) {
        val existing = accountRepository.findByNameAndInstitution(name, institution)?.toDomain()
        if (existing != null && existing.id != currentAccountId) {
            throw ConflictException(
                code = "conflict",
                message = "Account already exists for institution with name",
                details = mapOf("name" to name, "institution" to institution),
            )
        }
    }
}
