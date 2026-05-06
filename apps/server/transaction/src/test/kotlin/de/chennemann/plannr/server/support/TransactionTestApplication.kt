package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.persistence.toDomain
import de.chennemann.plannr.server.accounts.service.AccountService
import de.chennemann.plannr.server.accounts.api.dto.CreateAccountCommand
import de.chennemann.plannr.server.accounts.api.dto.UpdateAccountCommand
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.partners.api.dto.Partner
import de.chennemann.plannr.server.partners.domain.PartnerRepository
import de.chennemann.plannr.server.partners.api.dto.CreatePartnerCommand
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.partners.api.dto.UpdatePartnerCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand
import kotlinx.coroutines.flow.toList
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication(
    scanBasePackages = [
        "de.chennemann.plannr.server.common",
        "de.chennemann.plannr.server.transactions",
        "de.chennemann.plannr.server.accounts.persistence",
        "de.chennemann.plannr.server.partners.persistence",
        "de.chennemann.plannr.server.pockets.persistence",
        "de.chennemann.plannr.server.contracts.persistence",
    ],
)
@EnableR2dbcRepositories(
    basePackages = [
        "de.chennemann.plannr.server.accounts.domain",
        "de.chennemann.plannr.server.partners.domain",
        "de.chennemann.plannr.server.pockets.domain",
        "de.chennemann.plannr.server.contracts.domain",
    ],
)
class TransactionTestApplication {
    @Bean
    fun accountService(accountRepository: AccountRepository): AccountService =
        object : AccountService {
            override suspend fun create(command: CreateAccountCommand): Account =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun update(command: UpdateAccountCommand): Account =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun archive(id: Long): Account =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun unarchive(id: Long): Account =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun delete(id: Long) =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun list(archived: Boolean?): List<Account> =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun getById(id: Long): Account? =
                accountRepository.findById(id)?.toDomain()
        }

    @Bean
    fun pocketService(pocketRepository: PocketRepository): PocketService =
        object : PocketService {
            override suspend fun create(command: CreatePocketCommand): Pocket =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun update(command: UpdatePocketCommand): Pocket =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun archive(id: String): Pocket =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun unarchive(id: String): Pocket =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun archiveForAccount(accountId: Long) =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun unarchiveForAccount(accountId: Long) =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun delete(id: String) =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun list(accountId: Long?, archived: Boolean?): List<Pocket> =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun getById(id: String): Pocket? =
                pocketRepository.findById(id.trim())?.let { model ->
                    Pocket(
                        id = requireNotNull(model.id),
                        accountId = model.accountId,
                        name = model.name,
                        description = model.description,
                        color = model.color,
                        isDefault = model.isDefault,
                        isArchived = model.isArchived,
                        createdAt = model.createdAt,
                    )
                }
        }

    @Bean
    fun partnerService(partnerRepository: PartnerRepository): PartnerService =
        object : PartnerService {
            override suspend fun create(command: CreatePartnerCommand): Partner =
                partnerRepository.insert(
                    id = null,
                    name = command.name,
                    notes = command.notes,
                    isArchived = false,
                    createdAt = 1L,
                ).let { Partner(requireNotNull(it.id), it.name, it.notes, it.isArchived, it.createdAt) }

            override suspend fun update(command: UpdatePartnerCommand): Partner {
                val existing = partnerRepository.findById(command.id.trim())
                    ?: throw NotFoundException("not_found", "Partner not found", mapOf("id" to command.id.trim()))
                return partnerRepository.update(existing.id!!, command.name, command.notes, existing.isArchived)
                    .let { Partner(requireNotNull(it.id), it.name, it.notes, it.isArchived, it.createdAt) }
            }

            override suspend fun archive(id: String): Partner =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun unarchive(id: String): Partner =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun delete(id: String) =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun list(query: String?, archived: Boolean): List<Partner> =
                partnerRepository.findAllByQueryAndArchived(query?.trim()?.takeIf { it.isNotBlank() }, archived)
                    .toList()
                    .map { Partner(requireNotNull(it.id), it.name, it.notes, it.isArchived, it.createdAt) }

            override suspend fun getById(id: String): Partner? =
                partnerRepository.findById(id.trim())
                    ?.let { Partner(requireNotNull(it.id), it.name, it.notes, it.isArchived, it.createdAt) }
        }
}
