package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.api.dto.CreateAccountCommand
import de.chennemann.plannr.server.accounts.api.dto.UpdateAccountCommand
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.domain.save
import de.chennemann.plannr.server.accounts.persistence.AccountModel
import de.chennemann.plannr.server.accounts.persistence.toDTO
import de.chennemann.plannr.server.common.domain.normalizeCurrency
import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.transactions.projection.service.TransactionProjectionChangeEvent
import de.chennemann.plannr.server.transactions.projection.service.TransactionProjectionEventQueue
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
internal class AccountServiceImpl(
    private val accountRepository: AccountRepository,
    private val pocketService: PocketService,
    private val timeProvider: TimeProvider,
    private val projectionEventQueue: TransactionProjectionEventQueue? = null,
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
        ).toDTO()
        pocketService.create(
            CreatePocketCommand(
                accountId = created.id,
                name = DEFAULT_POCKET_NAME,
                description = null,
                color = DEFAULT_POCKET_COLOR,
                isDefault = true,
            ),
        )
        enqueueProjectionChange(created.id)
        return created
    }

    override suspend fun update(command: UpdateAccountCommand): Account {
        val existing = existingAccount(command.id)
        ensureNameAvailable(name = command.name, institution = command.institution, currentAccountId = existing.id)
        val currencyCode = normalizeCurrency(command.currencyCode)
        val persisted = accountRepository.save(
            existing.copy(
                name = command.name,
                institution = command.institution,
                currencyCode = currencyCode,
                weekendHandling = command.weekendHandling,
            ),
        )
        enqueueProjectionChange(persisted.id)
        return persisted
    }

    override suspend fun archive(id: Long): Account {
        val existing = existingAccount(id)
        val updated = accountRepository.save(existing.copy(isArchived = true))
        pocketService.archiveForAccount(updated.id)
        enqueueProjectionChange(updated.id)
        return updated
    }

    override suspend fun unarchive(id: Long): Account {
        val existing = existingAccount(id)
        val updated = accountRepository.save(existing.copy(isArchived = false))
        pocketService.unarchiveForAccount(updated.id)
        enqueueProjectionChange(updated.id)
        return updated
    }

    override suspend fun delete(id: Long) {
        val normalizedId = existingAccount(id).id
        accountRepository.deleteById(normalizedId)
        enqueueProjectionChange(normalizedId)
    }

    override suspend fun list(archived: Boolean?): List<Account> =
        accountRepository.findAllByOrderByCreatedAtAscIdAsc()
            .toList()
            .map(AccountModel::toDTO)
            .filter { archived == null || it.isArchived == archived }

    override suspend fun getById(id: Long): Account? =
        accountRepository.findById(id)?.toDTO()

    private suspend fun existingAccount(id: Long): Account =
        accountRepository.findById(id)?.toDTO()
            ?: throw NotFoundException(
                code = "not_found",
                message = "Account not found",
                details = mapOf("id" to id),
            )

    private suspend fun ensureNameAvailable(name: String, institution: String, currentAccountId: Long?) {
        val existing = accountRepository.findByNameAndInstitution(name, institution)?.toDTO()
        if (existing != null && existing.id != currentAccountId) {
            throw ConflictException(
                code = "conflict",
                message = "Account already exists for institution with name",
                details = mapOf("name" to name, "institution" to institution),
            )
        }
    }

    private suspend fun enqueueProjectionChange(id: Long) {
        projectionEventQueue?.enqueue(
            TransactionProjectionChangeEvent.AccountChanged(id),
        )
    }

    private companion object {
        const val DEFAULT_POCKET_NAME = "Default"
        const val DEFAULT_POCKET_COLOR = 0
    }
}
